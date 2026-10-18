package com.takumistudios.morphmod.animation;

import com.google.gson.*;
import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.character.CharacterDefinition;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import net.fabricmc.loader.api.FabricLoader;

public final class AnimationCatalog {
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().create();
    public record Snapshot(Map<String, AnimationProfile> profiles, AnimationSettings settings) { }
    private static volatile Snapshot current = new Snapshot(Map.of(), AnimationSettings.defaults());
    private static ExecutorService io;
    private static synchronized ExecutorService executor() {
        if (io == null || io.isShutdown()) io = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "Morph-Animations-IO"); t.setDaemon(true); return t; });
        return io;
    }
    public static Snapshot current() { return current; }
    public static boolean exists(String id) { return AnimationProfile.builtin(id) || current.profiles.containsKey(id); }
    private static Path root() { return FabricLoader.getInstance().getConfigDir().resolve("morphmod/animations"); }
    private static Path settingsFile() { return root().resolveSibling("animations.json"); }
    public static CompletableFuture<Snapshot> load() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path root = root(); Files.createDirectories(root);
                Path example = root.resolve("dedsafio_4");
                if (!Files.exists(example)) {
                    Files.createDirectories(example);
                    for (String name : List.of("profile.json", "dedsafio_4.animation.json")) {
                        try (InputStream input = AnimationCatalog.class.getResourceAsStream("/animations/dedsafio_4/" + name)) {
                            if (input == null) throw new IOException("Missing bundled animation " + name);
                            Files.copy(input, example.resolve(name));
                        }
                    }
                }
                Map<String, AnimationProfile> profiles = new TreeMap<>(); long size = 0;
                try (var directories = Files.list(root)) {
                    for (Path directory : directories.filter(p -> Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)).sorted().toList()) {
                        Path manifest = directory.resolve("profile.json");
                        byte[] metadata = read(directory, manifest);
                        JsonObject json = JsonParser.parseString(new String(metadata, StandardCharsets.UTF_8)).getAsJsonObject();
                        String filename = CharacterDefinition.path(json.get("animations").getAsString());
                        AnimationProfile profile = AnimationProfile.parse(AnimationProfile.document(metadata, read(directory, directory.resolve(filename))));
                        if (AnimationProfile.builtin(profile.id()) || profiles.putIfAbsent(profile.id(), profile) != null) throw new IOException("Reserved or duplicate animation id " + profile.id());
                        size += profile.data().length;
                        if (profiles.size() > 128 || size > 64 * 1024 * 1024) throw new IOException("Animation catalog exceeds limit");
                    }
                }
                if (com.takumistudios.morphmod.network.AnimationNetworking.metadata(profiles).length > 32768) throw new IOException("Animation catalog metadata exceeds limit");
                Path file = settingsFile();
                if (!Files.exists(file)) atomicWrite(file, PRETTY.toJson(AnimationSettings.defaults().json()));
                if (Files.size(file) > 16 * 1024 * 1024) throw new IOException("Animation settings exceed limit");
                AnimationSettings settings;
                try { settings = AnimationSettings.parse(JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject()); }
                catch (RuntimeException invalid) { Files.copy(file, file.resolveSibling(file.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING); throw invalid; }
                return new Snapshot(Map.copyOf(profiles), settings);
            } catch (IOException | RuntimeException e) { throw new CompletionException(e); }
        }, executor());
    }
    private static byte[] read(Path directory, Path file) throws IOException {
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) || !file.toRealPath().startsWith(directory.toRealPath()) || Files.size(file) > 16 * 1024 * 1024)
            throw new IOException("Invalid animation resource " + file.getFileName());
        return Files.readAllBytes(file);
    }
    public static void publish(Snapshot snapshot) { current = snapshot; }
    public static CompletableFuture<Void> save(AnimationSettings settings) {
        return CompletableFuture.runAsync(() -> {
            try { atomicWrite(settingsFile(), PRETTY.toJson(settings.json())); }
            catch (IOException e) { throw new CompletionException(e); }
        }, executor());
    }
    public static void settings(AnimationSettings settings) { current = new Snapshot(current.profiles, settings); }
    private static void atomicWrite(Path target, String data) throws IOException {
        Files.createDirectories(target.getParent()); Path tmp = Files.createTempFile(target.getParent(), "animations-", ".tmp");
        try {
            Files.writeString(tmp, data, StandardCharsets.UTF_8);
            try { Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(tmp); }
    }
    public static void shutdown() {
        ExecutorService running = io; if (running == null) return; running.shutdown();
        try { if (!running.awaitTermination(10, TimeUnit.SECONDS)) running.shutdownNow(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); MorphMod.LOGGER.warn("Interrupted while closing animation IO", e); }
    }
    private AnimationCatalog() { }
}
