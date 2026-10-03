package com.takumistudios.morphmod.client.character;

import com.google.gson.*;
import com.geckolib.loading.math.MathParser;
import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.character.*;
import com.takumistudios.morphmod.network.CharacterPayload;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.*;

/** Transactional server catalog and content-addressed cache. No server path is extracted. */
public final class ClientCharacters {
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "Morph-Client-Assets"); t.setDaemon(true); return t; });
    private static final Path CACHE = FabricLoader.getInstance().getGameDir().resolve(".morphmod-cache");
    private static volatile Map<String, CharacterBundle> current = Map.of();
    private static volatile Path packPath;
    private static volatile JsonObject access = new JsonObject();
    private static volatile long revision;
    public static long revision() { return revision; }
    private static Map<String, Offer> offers = Map.of();
    private static final Map<String, CharacterBundle> pending = new HashMap<>();
    private static final Map<String, FragmentAssembly> assemblies = new HashMap<>();
    private static CompletableFuture<Void> work = CompletableFuture.completedFuture(null);
    private static int generation;
    private static boolean failed;
    private record Offer(String hash, int size) { }
    public static Map<String, CharacterBundle> bundles() { return current; }
    public static CharacterDefinition get(String id) { var bundle = current.get(id); return bundle == null ? null : bundle.definition(); }
    public static boolean allowed(String id) { return access.has(id) && access.getAsJsonObject(id).get("allowed").getAsBoolean(); }
    public static boolean allowedEmote(String id, String emote) {
        if (!allowed(id)) return false;
        for (var e : access.getAsJsonObject(id).getAsJsonArray("emotes")) if (e.getAsString().equals(emote)) return true;
        return false;
    }
    public static Identifier resource(CharacterDefinition d, String kind) {
        String base = d.id().replace(':', '/');
        String path = switch (kind) { case "model" -> "geckolib/models/" + base + ".geo.json";
            case "animation" -> "geckolib/animations/" + base + ".animation.json"; default -> "textures/characters/" + base + ".png"; };
        return MorphMod.id(path);
    }
    public static void init() {
        ClientConfigurationNetworking.registerGlobalReceiver(CharacterPayload.TYPE, (p, c) -> receive(p, c.client(), ClientConfigurationNetworking::send));
        ClientPlayNetworking.registerGlobalReceiver(CharacterPayload.TYPE, (p, c) -> receive(p, c.client(), ClientPlayNetworking::send));
    }
    private static synchronized void receive(CharacterPayload payload, Minecraft minecraft, Consumer<CharacterPayload> reply) {
        if (payload.stage() == 6) {
            try { access = JsonParser.parseString(new String(payload.data(), StandardCharsets.UTF_8)).getAsJsonObject(); revision++; }
            catch (RuntimeException e) { MorphMod.LOGGER.warn("Invalid character access metadata", e); }
            return;
        }
        if (payload.stage() == 0) {
            generation++; int token = generation;
            work = work.handle((v, e) -> null).thenRunAsync(() -> {
                try {
                    pending.clear(); assemblies.clear(); failed = false;
                    Map<String, Offer> list = new TreeMap<>(); long total = 0;
                    JsonArray entries = JsonParser.parseString(new String(payload.data(), StandardCharsets.UTF_8)).getAsJsonArray();
                    if (entries.size() > 256) throw new IOException("Catalog too large");
                    JsonArray cached = new JsonArray();
                    for (JsonElement entry : entries) {
                        JsonObject e = entry.getAsJsonObject(); String id = e.get("id").getAsString(), hash = e.get("hash").getAsString(); int size = e.get("size").getAsInt();
                        Identifier.parse(id);
                        if (!hash.matches("[a-f0-9]{64}") || size < 1 || size > CharacterBundle.MAX_CHARACTER || list.putIfAbsent(id, new Offer(hash, size)) != null)
                            throw new IOException("Invalid catalog offer");
                        total += size; if (total > CharacterBundle.MAX_CATALOG) throw new IOException("Catalog exceeds budget");
                        Path file = CACHE.resolve(hash + ".zip");
                        if (Files.isRegularFile(file) && Files.size(file) == size) {
                            try {
                                var bundle = CharacterBundle.decode(Files.readAllBytes(file), hash);
                                if (!bundle.definition().id().equals(id)) throw new IOException("Cache ID mismatch");
                                pending.put(id, bundle); cached.add(hash);
                            } catch (IOException | RuntimeException error) { MorphMod.LOGGER.debug("Ignoring invalid cached character", error); }
                        }
                    }
                    offers = Map.copyOf(list);
                    pruneCache();
                    minecraft.execute(() -> { if (token == generation) reply.accept(CharacterPayload.signal(4, cached.toString().getBytes(StandardCharsets.UTF_8))); });
                } catch (IOException | RuntimeException e) { failed = true; MorphMod.LOGGER.warn("Invalid character catalog", e); minecraft.execute(() -> reply.accept(CharacterPayload.signal(5, new byte[0]))); }
            }, IO);
            return;
        }
        int token = generation;
        work = work.thenRunAsync(() -> {
            if (token != generation || failed) return;
            try {
                if (payload.stage() == 1 || payload.stage() == 2) {
                    Offer offer = offers.get(payload.id());
                    if (offer == null || !offer.hash.equals(payload.hash()) || offer.size != payload.size()) throw new IOException("Unrequested resources");
                    if (payload.stage() == 1) assemblies.computeIfAbsent(payload.id(), id -> new FragmentAssembly(offer.size)).accept(payload.index(), payload.data());
                    else {
                        FragmentAssembly assembly = assemblies.remove(payload.id());
                        if (assembly == null) throw new IOException("Missing character transfer");
                        byte[] bytes = assembly.finish(); var bundle = CharacterBundle.decode(bytes, offer.hash);
                        if (!bundle.definition().id().equals(payload.id())) throw new IOException("Transferred ID mismatch");
                        validateExpressions(bundle);
                        pending.put(payload.id(), bundle); Files.createDirectories(CACHE);
                        Path target = CACHE.resolve(offer.hash + ".zip"), tmp = CACHE.resolve(offer.hash + ".tmp");
                        Files.write(tmp, bytes); Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    }
                } else if (payload.stage() == 3) {
                    if (pending.size() != offers.size()) throw new IOException("Incomplete catalog");
                    for (var b : pending.values()) validateExpressions(b);
                    Map<String, CharacterBundle> snapshot = Map.copyOf(pending);
                    Path prepared = preparePack(snapshot);
                    minecraft.execute(() -> {
                        if (token != generation) return;
                        Path previousPath = packPath; Map<String, CharacterBundle> previous = current;
                        packPath = prepared;
                        minecraft.reloadResourcePacks().whenComplete((v, error) -> minecraft.execute(() -> {
                            if (token != generation) return;
                            if (error == null) { current = snapshot; revision++; CharacterRenderManager.clear(); }
                            else { packPath = previousPath; current = previous; MorphMod.LOGGER.warn("Character resource reload failed", error); }
                            reply.accept(CharacterPayload.signal(5, new byte[0]));
                        }));
                    });
                }
            } catch (IOException | RuntimeException e) { failed = true; assemblies.clear(); MorphMod.LOGGER.warn("Character assets rejected; previous catalog retained", e); minecraft.execute(() -> reply.accept(CharacterPayload.signal(5, new byte[0]))); }
        }, IO);
    }
    private static final Pattern QUERIES = Pattern.compile("(?:query|q)\\.([a-zA-Z_][a-zA-Z_0-9]*)");
    private static final Set<String> SUPPORTED = Set.of("anim_time", "ground_speed", "pitch", "yaw", "right_hand_swing", "left_hand_swing", "is_sneaking", "is_sprinting", "is_swimming", "is_on_ground", "life_time", "head_x_rotation", "head_y_rotation", "limb_swing", "limb_swing_amount");
    private static void validateExpressions(CharacterBundle bundle) {
        JsonElement json = JsonParser.parseString(new String(bundle.files().get(bundle.definition().animations()), StandardCharsets.UTF_8));
        json.getAsJsonObject().getAsJsonObject("animations").entrySet().forEach(e -> {
            JsonObject clip = e.getValue().getAsJsonObject(); if (clip.has("bones")) validateStrings(clip.get("bones"));
        });
    }
    private static void validateStrings(JsonElement value) {
        if (value.isJsonObject()) value.getAsJsonObject().entrySet().forEach(e -> validateStrings(e.getValue()));
        else if (value.isJsonArray()) value.getAsJsonArray().forEach(ClientCharacters::validateStrings);
        else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            String text = value.getAsString();
            if (text.equals("hold_on_last_frame") || text.equals("catmullrom") || text.equals("linear")) return;
            var matcher = QUERIES.matcher(text);
            while (matcher.find()) if (!SUPPORTED.contains(matcher.group(1))) throw new IllegalArgumentException("Unsupported MoLang query: " + matcher.group());
            MathParser.create().compileMolang(text);
        }
    }
    private static Path preparePack(Map<String, CharacterBundle> bundles) throws IOException {
        Files.createDirectories(CACHE);
        pruneCache();
        Path root = Files.createTempDirectory(CACHE, "pack-");
        int major = SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES).major();
        Files.writeString(root.resolve("pack.mcmeta"), "{\"pack\":{\"description\":\"Morph characters\",\"min_format\":" + major + ",\"max_format\":" + major + "}}", StandardCharsets.UTF_8);
        for (CharacterBundle b : bundles.values()) for (String kind : List.of("model", "animation", "texture")) {
            String source = switch (kind) { case "model" -> b.definition().model(); case "animation" -> b.definition().animations(); default -> b.definition().texture(); };
            Path target = root.resolve("assets/morphmod/" + resource(b.definition(), kind).getPath());
            Files.createDirectories(target.getParent());
            byte[] data = b.files().get(source);
            if (kind.equals("animation")) {
                JsonObject document = JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();
                JsonObject clips = document.getAsJsonObject("animations");
                b.definition().emotes().forEach((id, emote) -> {
                    if (emote.modes().contains("overlay")) {
                        JsonObject copy = clips.getAsJsonObject(emote.animation()).deepCopy();
                        if (copy.has("bones")) copy.getAsJsonObject("bones").keySet().removeIf(bone -> !emote.bones().contains(bone));
                        clips.add("__morph_overlay_" + id, copy);
                    }
                });
                data = document.toString().getBytes(StandardCharsets.UTF_8);
            }
            Files.write(target, data);
        }
        return root;
    }
    public static Pack pack() {
        Path path = packPath; if (path == null) return null;
        return Pack.readMetaAndCreate(new PackLocationInfo("morphmod:characters", Component.translatable("pack.morphmod.characters"), PackSource.SERVER, Optional.empty()),
                new PathPackResources.PathResourcesSupplier(path), PackType.CLIENT_RESOURCES, new PackSelectionConfig(true, Pack.Position.TOP, true));
    }
    /** Only internally named cache files are eligible, and symbolic links are never traversed. */
    private static void pruneCache() throws IOException {
        if (!Files.isDirectory(CACHE)) return;
        Set<String> retained = new HashSet<>(); offers.values().forEach(o -> retained.add(o.hash + ".zip")); current.values().forEach(b -> retained.add(b.hash() + ".zip"));
        try (var paths = Files.list(CACHE)) {
            List<Path> archives = new ArrayList<>(); long bytes = 0;
            for (Path path : paths.toList()) {
                String name = path.getFileName().toString();
                if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && name.matches("[a-f0-9]{64}\\.zip")) { archives.add(path); bytes += Files.size(path); }
                else if (name.startsWith("pack-") && !path.equals(packPath) && Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    try (var tree = Files.walk(path)) {
                        for (Path entry : tree.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(entry);
                    }
                }
            }
            archives.sort(Comparator.comparingLong(p -> p.toFile().lastModified()));
            for (Path path : archives) if (bytes > 4L * CharacterBundle.MAX_CATALOG && !retained.contains(path.getFileName().toString())) {
                bytes -= Files.size(path); Files.deleteIfExists(path);
            }
        }
    }
    public static synchronized void reset() { generation++; current = Map.of(); access = new JsonObject(); revision++; packPath = null; CharacterRenderManager.clear(); }
    private ClientCharacters() { }
}
