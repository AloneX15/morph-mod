package com.takumistudios.morphmod.character;

import com.takumistudios.morphmod.MorphMod;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import net.fabricmc.loader.api.FabricLoader;

/** Disk work is performed on a dedicated executor; publication is an immutable snapshot. */
public final class CharacterCatalog {
    private static ExecutorService io;
    private static synchronized ExecutorService executor() {
        if (io == null || io.isShutdown()) io = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "Morph-Characters-IO"); t.setDaemon(true); return t; });
        return io;
    }
    private static volatile Map<String, CharacterBundle> bundles = Map.of();
    public static Map<String, CharacterBundle> bundles() { return bundles; }
    public static CharacterDefinition get(String id) { CharacterBundle b = bundles.get(id); return b == null ? null : b.definition(); }
    public static CompletableFuture<Map<String, CharacterBundle>> load() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path root = FabricLoader.getInstance().getConfigDir().resolve("morphmod/characters");
                Files.createDirectories(root);
                Path example = root.resolve("otter");
                if (!Files.exists(example)) {
                    Files.createDirectories(example);
                    for (String name : List.of("character.json", "otter.geo.json", "otter.animation.json", "otter.png")) {
                        try (InputStream stream = CharacterCatalog.class.getResourceAsStream("/characters/otter/" + name)) {
                            if (stream == null) throw new IOException("Missing bundled template " + name);
                            Files.copy(stream, example.resolve(name));
                        }
                    }
                }
                TreeMap<String, CharacterBundle> result = new TreeMap<>(); long size = 0;
                try (var dirs = Files.list(root)) {
                    for (Path dir : dirs.filter(p -> Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)).sorted().toList()) {
                        Map<String, byte[]> files = new HashMap<>(); long characterSize = 0;
                        try (var paths = Files.walk(dir)) {
                            for (Path file : paths.filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)).toList()) {
                                if (!file.toRealPath().startsWith(dir.toRealPath())) throw new IOException("Resource escapes character directory");
                                characterSize += Files.size(file);
                                if (characterSize > CharacterBundle.MAX_CHARACTER || files.size() >= 64) throw new IOException("Character exceeds limit");
                                files.put(dir.relativize(file).toString().replace('\\', '/'), Files.readAllBytes(file));
                            }
                        }
                        CharacterBundle bundle = CharacterBundle.create(files);
                        if (result.putIfAbsent(bundle.definition().id(), bundle) != null) throw new IOException("Duplicate character id");
                        size += bundle.archive().length;
                        if (size > CharacterBundle.MAX_CATALOG || result.size() > 256) throw new IOException("Catalog exceeds limit");
                    }
                }
                com.google.gson.JsonObject access = new com.google.gson.JsonObject();
                com.google.gson.JsonArray offer = new com.google.gson.JsonArray();
                for (var bundle : result.values()) {
                    var entry = new com.google.gson.JsonObject();
                    entry.addProperty("id", bundle.definition().id()); entry.addProperty("hash", bundle.hash());
                    entry.addProperty("size", bundle.archive().length); offer.add(entry);
                    var value = new com.google.gson.JsonObject(); value.addProperty("allowed", false);
                    var emotes = new com.google.gson.JsonArray(); bundle.definition().emotes().keySet().forEach(emotes::add);
                    value.add("emotes", emotes); access.add(bundle.definition().id(), value);
                }
                if (access.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > CharacterBundle.FRAGMENT)
                    throw new IOException("Catalog permission metadata exceeds limit");
                if (offer.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > CharacterBundle.FRAGMENT)
                    throw new IOException("Catalog transfer metadata exceeds limit");
                return Map.copyOf(result);
            } catch (IOException | RuntimeException e) { throw new CompletionException(e); }
        }, executor());
    }
    public static void publish(Map<String, CharacterBundle> snapshot) { bundles = snapshot; }
    public static void shutdown() {
        ExecutorService running = io; if (running == null) return;
        running.shutdown();
        try { if (!running.awaitTermination(5, TimeUnit.SECONDS)) running.shutdownNow(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); MorphMod.LOGGER.warn("Interrupted while closing character IO", e); }
    }
    private CharacterCatalog() { }
}
