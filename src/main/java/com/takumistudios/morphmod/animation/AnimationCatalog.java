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
                for (String bundled : BUNDLED.keySet()) install(root.resolve(bundled), bundled);
                Map<String, AnimationProfile> profiles = new TreeMap<>(); long size = 0;
                try (var directories = Files.list(root)) {
                    for (Path directory : directories.filter(p -> Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)).sorted().toList()) {
                        AnimationProfile profile = loadDirectory(directory);
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
    /** Bundled profiles and the SHA-256 of every file version previously shipped, per file. */
    private static final Map<String, Map<String, Set<String>>> BUNDLED = Map.of(
        "dedsafio_3", Map.of("profile.json", Set.of(), "dedsafio_3.animation.json", Set.of()),
        "dedsafio_4", Map.of(
            "profile.json", Set.of("5342fade5887aafb1b510258c54cc20eddfb7d8e0336ff1af3f44fd9b5156762"),
            "dedsafio_4.animation.json", Set.of("a1a7c83693712895f9b9bf67c6ac5e8d2f513823c0f1c18a32eec656f19adab0", "09373447fd3db751d4660eb3fc3854b3a8e81201906ca2ac18e57800f925ac7e")));
    /** Copies a missing bundled profile, or refreshes it only when every file is an unedited earlier release. */
    static void install(Path directory, String bundled) throws IOException {
        Map<String, Set<String>> files = BUNDLED.get(bundled);
        if (Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
            if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) return;
            for (var file : files.entrySet()) {
                Path path = directory.resolve(file.getKey());
                if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.size(path) > 16 * 1024 * 1024) return;
                String hash = sha256(Files.readAllBytes(path));
                if (!file.getValue().contains(hash) && !hash.equals(sha256(resource(bundled, file.getKey())))) return;
            }
        } else Files.createDirectories(directory);
        for (String name : files.keySet()) {
            byte[] data = resource(bundled, name); Path target = directory.resolve(name);
            if (!Files.exists(target) || !Arrays.equals(Files.readAllBytes(target), data)) atomicWrite(target, new String(data, StandardCharsets.UTF_8));
        }
    }
    private static byte[] resource(String bundled, String name) throws IOException {
        try (InputStream input = AnimationCatalog.class.getResourceAsStream("/animations/" + bundled + "/" + name)) {
            if (input == null) throw new IOException("Missing bundled animation " + name);
            return input.readAllBytes();
        }
    }
    private static String sha256(byte[] data) {
        try { return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(data)); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    /** Reads and validates one profile folder (profile.json plus its animation file). */
    public static AnimationProfile loadDirectory(Path directory) throws IOException {
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) throw new IOException("Missing animation profile folder " + directory.getFileName());
        byte[] metadata = read(directory, directory.resolve("profile.json"));
        JsonObject json = JsonParser.parseString(new String(metadata, StandardCharsets.UTF_8)).getAsJsonObject();
        String filename = CharacterDefinition.path(json.get("animations").getAsString());
        return AnimationProfile.parse(AnimationProfile.document(metadata, read(directory, directory.resolve(filename))));
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
