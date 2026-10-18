package com.takumistudios.morphmod.client.animation;

import com.google.gson.*;
import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.animation.*;
import com.takumistudios.morphmod.character.FragmentAssembly;
import com.takumistudios.morphmod.network.AnimationPayload;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

/** Serialized IO validates entire catalog revisions before publishing to the render thread. */
public final class ClientAnimationProfiles {
    private record Entry(String id, String hash, int size) { }
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "Morph-AnimationCache-IO"); t.setDaemon(true); return t; });
    private static final Path CACHE = FabricLoader.getInstance().getGameDir().resolve(".morphmod-animation-cache");
    private static volatile Map<String, AnimationProfile> profiles = Map.of();
    private static final Map<UUID, String> assignments = new HashMap<>();
    private static volatile long generation;
    private static boolean managed;
    private record Phase(String hash, String action, double start) { }
    private static final Map<UUID, Phase> PHASES = new HashMap<>();
    public static double phase(UUID player, String hash, String action, double clock) {
        Phase phase = PHASES.get(player);
        if (phase == null || !phase.hash.equals(hash) || !phase.action.equals(action) || clock < phase.start) {
            if (PHASES.size() >= 4096) PHASES.clear();
            phase = new Phase(hash, action, clock); PHASES.put(player, phase);
        }
        return clock - phase.start;
    }
    private static final Set<String> FAILED = new HashSet<>();
    public static boolean failed(String hash) { return FAILED.contains(hash); }
    public static void fail(String hash, Throwable error) { if (FAILED.add(hash)) MorphMod.LOGGER.warn("Animation profile {} disabled; using vanilla", hash, error); }
    public static void failProfile(String id, Throwable error) {
        var local = preview; var profile = local != null && local.id().equals(id) ? local : get(id);
        if (profile != null) fail(profile.hash(), error);
    }
    // Local-only preview of a profile folder; never sent to the server and cleared on disconnect.
    private static volatile AnimationProfile preview;
    private static volatile String previewName;
    public static AnimationProfile preview() { return preview; }
    public static String previewName() { return previewName; }
    public static CompletableFuture<AnimationProfile> loadPreview(String name) {
        if (!name.matches("[a-z0-9_.-]{1,64}") || name.contains("..")) return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid folder name"));
        Path folder = FabricLoader.getInstance().getConfigDir().resolve("morphmod/animations").resolve(name);
        return CompletableFuture.supplyAsync(() -> {
            try { return AnimationCatalog.loadDirectory(folder); }
            catch (IOException e) { throw new CompletionException(e); }
        }, IO).thenApplyAsync(profile -> { preview = profile; previewName = name; FAILED.remove(profile.hash()); return profile; }, Minecraft.getInstance());
    }
    public static void clearPreview() { preview = null; previewName = null; }
    // IO-thread-owned state below.
    private static String revision = "";
    private static Map<String, Entry> offered = Map.of();
    private static final Map<String, AnimationProfile> pending = new HashMap<>();
    private static final Map<String, FragmentAssembly> assemblies = new HashMap<>();
    private static boolean failed;
    public static boolean managed() { return managed; }
    public static String selected(UUID player) { return assignments.getOrDefault(player, "morphmod:vanilla"); }
    public static AnimationProfile get(String id) { return profiles.get(id); }
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(AnimationPayload.TYPE, (payload, context) -> {
            if (payload.stage() == 5) {
                try {
                    JsonObject json = JsonParser.parseString(new String(payload.data(), StandardCharsets.UTF_8)).getAsJsonObject();
                    UUID player = UUID.fromString(json.get("player").getAsString()); String id = AnimationProfile.validId(json.get("profile").getAsString());
                    if (assignments.size() < 4096 || assignments.containsKey(player)) assignments.put(player, id);
                } catch (RuntimeException e) { MorphMod.LOGGER.debug("Ignored invalid animation assignment", e); }
                return;
            }
            if (payload.stage() == 0) managed = true;
            long token = generation;
            IO.execute(() -> receive(payload, token, value -> context.client().execute(() -> {
                if (token == generation && ClientPlayNetworking.canSend(AnimationPayload.TYPE)) ClientPlayNetworking.send(value);
            })));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> reset());
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.level != null && mc.level.getGameTime() % 200 == 0) {
                Set<UUID> visible = new HashSet<>(); mc.level.players().forEach(p -> visible.add(p.getUUID())); assignments.keySet().retainAll(visible); PHASES.keySet().retainAll(visible);
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> IO.shutdown());
    }
    public static void reset() {
        generation++; profiles = Map.of(); assignments.clear(); managed = false; FAILED.clear(); PHASES.clear(); clearPreview();
        IO.execute(() -> { revision = ""; offered = Map.of(); pending.clear(); assemblies.clear(); });
    }
    private static void receive(AnimationPayload payload, long token, Consumer<AnimationPayload> reply) {
        if (token != generation) return;
        try {
            if (payload.stage() == 0) {
                revision = payload.hash(); failed = false; pending.clear(); assemblies.clear();
                JsonArray entries = JsonParser.parseString(new String(payload.data(), StandardCharsets.UTF_8)).getAsJsonArray();
                if (entries.size() > 128) throw new IOException("Too many animation profiles");
                Map<String, Entry> expected = new HashMap<>(); long total = 0; JsonArray cached = new JsonArray(); Files.createDirectories(CACHE);
                for (JsonElement value : entries) {
                    JsonObject json = value.getAsJsonObject(); String id = AnimationProfile.validId(json.get("id").getAsString()), hash = json.get("hash").getAsString(); int size = json.get("size").getAsInt();
                    if (!hash.matches("[a-f0-9]{64}") || size < 1 || size > 16 * 1024 * 1024 || AnimationProfile.builtin(id)) throw new IOException("Invalid animation metadata");
                    Entry entry = new Entry(id, hash, size);
                    if (expected.put(id, entry) != null || (total += size) > 64 * 1024 * 1024) throw new IOException("Animation catalog exceeds limits");
                    Path path = CACHE.resolve(hash + ".json");
                    if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && Files.size(path) == size) {
                        try { AnimationProfile profile = checked(entry, Files.readAllBytes(path)); pending.put(id, profile); cached.add(hash); }
                        catch (IOException | RuntimeException corrupt) { Files.deleteIfExists(path); MorphMod.LOGGER.debug("Discarded corrupt animation cache {}", hash); }
                    }
                }
                offered = Map.copyOf(expected); reply.accept(AnimationPayload.signal(4, revision, cached.toString().getBytes(StandardCharsets.UTF_8)));
            } else if (payload.stage() == 1 || payload.stage() == 2) {
                if (failed) return;
                Entry entry = offered.get(payload.id());
                if (entry == null || !entry.hash.equals(payload.hash()) || entry.size != payload.size() || pending.containsKey(entry.id)) throw new IOException("Unexpected animation fragment");
                if (payload.stage() == 1) assemblies.computeIfAbsent(entry.id, key -> new FragmentAssembly(entry.size)).accept(payload.index(), payload.data());
                else {
                    FragmentAssembly assembly = assemblies.remove(entry.id); if (assembly == null) throw new IOException("Missing animation fragments");
                    AnimationProfile profile = checked(entry, assembly.finish()); pending.put(entry.id, profile);
                    Path tmp = Files.createTempFile(CACHE, "profile-", ".tmp");
                    try {
                        Files.write(tmp, profile.data());
                        try { Files.move(tmp, CACHE.resolve(profile.hash() + ".json"), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
                        catch (AtomicMoveNotSupportedException e) { Files.move(tmp, CACHE.resolve(profile.hash() + ".json"), StandardCopyOption.REPLACE_EXISTING); }
                    } finally { Files.deleteIfExists(tmp); }
                }
            } else if (payload.stage() == 3 && payload.hash().equals(revision) && !failed) {
                if (!pending.keySet().equals(offered.keySet())) throw new IOException("Incomplete animation catalog");
                Map<String, AnimationProfile> ready = Map.copyOf(pending);
                Minecraft.getInstance().execute(() -> { if (token == generation) { profiles = ready; FAILED.clear(); } }); prune();
            }
        } catch (IOException | RuntimeException | LinkageError e) {
            if (!failed) MorphMod.LOGGER.warn("Animation profile catalog rejected; retaining previous revision", e);
            failed = true; pending.clear(); assemblies.clear();
        }
    }
    private static AnimationProfile checked(Entry entry, byte[] data) throws IOException {
        AnimationProfile profile = AnimationProfile.parse(data);
        if (!entry.id.equals(profile.id()) || !entry.hash.equals(profile.hash()) || entry.size != data.length) throw new IOException("Animation profile checksum mismatch");
        return profile;
    }
    private static void prune() throws IOException {
        try (var stream = Files.list(CACHE)) {
            List<Path> files = stream.filter(p -> p.getFileName().toString().matches("[a-f0-9]{64}\\.json") && Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)).sorted(Comparator.comparingLong(p -> p.toFile().lastModified())).toList();
            long total = 0; for (Path file : files) total += Files.size(file);
            for (Path file : files) { if (total <= 64 * 1024 * 1024) break; total -= Files.size(file); Files.deleteIfExists(file); }
        }
    }
    private ClientAnimationProfiles() { }
}
