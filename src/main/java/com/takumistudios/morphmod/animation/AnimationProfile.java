package com.takumistudios.morphmod.animation;

import com.google.gson.*;
import com.takumistudios.morphmod.character.CharacterDefinition;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/** Immutable import, validation report and content-addressed wire document. */
public record AnimationProfile(String id, String name, Map<String, String> bindings, Map<String, Clip> clips,
                               List<String> warnings, byte[] data, String hash, boolean hipPivot, boolean firstPerson) {
    public static final Set<String> BONES = Set.of("root", "waist", "body", "body_upper", "head", "left_arm", "right_arm", "left_leg", "right_leg", "left_foot", "right_foot");
    public record Bone(AnimationChannel rotation, AnimationChannel position, AnimationChannel scale) { }
    public record Clip(double length, boolean loop, boolean hold, Map<String, Bone> bones) {
        public double time(double elapsed) { return length <= 0 ? elapsed : loop ? elapsed % length : Math.min(elapsed, length); }
    }
    public static AnimationProfile parse(byte[] data) {
        if (data.length > 16 * 1024 * 1024) throw new IllegalArgumentException("Animation profile exceeds 16 MiB");
        JsonObject document = JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();
        JsonObject manifest = document.getAsJsonObject("manifest"), animation = document.getAsJsonObject("animation");
        if (!manifest.get("schemaVersion").toString().equals("1")) throw new IllegalArgumentException("Unsupported profile schema");
        String id = validId(manifest.get("id").getAsString()), name = manifest.get("name").getAsString();
        if (name.isBlank() || name.length() > 128) throw new IllegalArgumentException("Invalid profile name");
        Map<String, String> bindings = strings(manifest, "bindings"), aliases = strings(manifest, "boneAliases");
        if (bindings.size() > 128 || aliases.size() > 128) throw new IllegalArgumentException("Too many profile mappings");
        aliases.values().forEach(b -> { if (!BONES.contains(b)) throw new IllegalArgumentException("Unknown canonical bone " + b); });
        String pivot = option(manifest, "bodyPivot", "neck", JsonPrimitive::isString).getAsString();
        if (!pivot.equals("neck") && !pivot.equals("hip")) throw new IllegalArgumentException("Invalid bodyPivot " + pivot);
        boolean firstPerson = option(manifest, "firstPerson", false, JsonPrimitive::isBoolean).getAsBoolean();
        List<String> warnings = new ArrayList<>(); Map<String, Clip> clips = new TreeMap<>();
        JsonObject source = animation.getAsJsonObject("animations");
        if (source.size() > 512) throw new IllegalArgumentException("Too many clips");
        for (var entry : source.entrySet()) {
            if (entry.getKey().isBlank() || entry.getKey().length() > 160) throw new IllegalArgumentException("Invalid clip name");
            JsonObject clip = entry.getValue().getAsJsonObject();
            double length = clip.has("animation_length") ? clip.get("animation_length").getAsDouble() : 0;
            if (!Double.isFinite(length) || length < 0 || length > 3600) throw new IllegalArgumentException("Invalid clip length");
            JsonElement loop = clip.get("loop");
            boolean looping = loop != null && loop.isJsonPrimitive() && loop.getAsJsonPrimitive().isBoolean() && loop.getAsBoolean();
            boolean hold = loop != null && loop.isJsonPrimitive() && loop.getAsString().equals("hold_on_last_frame");
            if (loop != null && !loop.getAsJsonPrimitive().isBoolean() && !hold) throw new IllegalArgumentException("Unsupported loop mode");
            Map<String, Bone> bones = new HashMap<>(); JsonObject channels = clip.getAsJsonObject("bones");
            if (channels != null) {
                if (channels.size() > 512) throw new IllegalArgumentException("Too many bone channels");
                for (var channel : channels.entrySet()) {
                    if (channel.getKey().length() > 128) throw new IllegalArgumentException("Bone name exceeds limit");
                    String bone = aliases.getOrDefault(channel.getKey(), channel.getKey()); JsonObject transforms = channel.getValue().getAsJsonObject();
                    // Validate even omitted channels: unknown bones cannot hide unsafe expressions.
                    AnimationChannel rotation = channel(transforms, "rotation"), position = channel(transforms, "position"), scale = channel(transforms, "scale");
                    if (!BONES.contains(bone)) { warn(warnings, entry.getKey() + ": omitted bone " + channel.getKey() + aliasHint(channel.getKey())); continue; }
                    if (bones.put(bone, new Bone(rotation, position, scale)) != null) throw new IllegalArgumentException("Multiple channels map to " + bone);
                    if (bone.endsWith("_foot")) warn(warnings, entry.getKey() + ": " + bone + " merged into vanilla leg");
                    if ((bone.equals("root") || bone.equals("waist") || bone.equals("body_upper")) && (rotation != null || position != null || scale != null))
                        warn(warnings, entry.getKey() + ": " + bone + " adapted through virtual humanoid parent; first-person root/waist offsets omitted");
                    if (scale != null && rotation != null) warn(warnings, entry.getKey() + ": rotated non-uniform scale is approximated without shear");
                }
            }
            if (!clip.has("animation_length")) for (Bone bone : bones.values()) {
                if (bone.rotation != null) length = Math.max(length, bone.rotation.keys().getLast().time());
                if (bone.position != null) length = Math.max(length, bone.position.keys().getLast().time());
                if (bone.scale != null) length = Math.max(length, bone.scale.keys().getLast().time());
            }
            for (String unsupported : List.of("sound_effects", "particle_effects", "timeline")) if (clip.has(unsupported)) warn(warnings, entry.getKey() + ": omitted " + unsupported);
            clips.put(entry.getKey(), new Clip(length, looping, hold, Map.copyOf(bones)));
        }
        bindings.values().forEach(value -> { if (!clips.containsKey(value)) throw new IllegalArgumentException("Unknown bound clip " + value); });
        for (String action : List.of("swim", "elytra", "sleep")) {
            String clipName = clipName(bindings, clips.keySet(), action); if (clipName == null) continue;
            for (String bone : List.of("root", "waist")) {
                Bone channels = clips.get(clipName).bones().get(bone);
                if (channels == null || channels.rotation() == null) continue;
                if (action.equals("sleep")) warn(warnings, clipName + ": " + bone + " rotation is added to the vanilla bed rotation; remove it");
                else if ((channels.rotation().lookQueries() & 1) == 0)
                    warn(warnings, clipName + ": " + bone + " rotation ignores where the player looks; use query.pitch, for example \"90+query.pitch\"");
            }
        }
        try { return new AnimationProfile(id, name, Map.copyOf(bindings), Map.copyOf(clips), List.copyOf(warnings), data.clone(), HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data)), pivot.equals("hip"), firstPerson); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private static void warn(List<String> warnings, String warning) {
        if (warnings.size() < 128) warnings.add(warning);
        else if (warnings.size() == 128) warnings.add("Additional import warnings omitted (limit 128)");
    }
    private static JsonPrimitive option(JsonObject manifest, String key, Object fallback, java.util.function.Predicate<JsonPrimitive> type) {
        if (!manifest.has(key)) return fallback instanceof Boolean value ? new JsonPrimitive(value) : new JsonPrimitive((String)fallback);
        JsonElement value = manifest.get(key);
        if (!value.isJsonPrimitive() || !type.test(value.getAsJsonPrimitive())) throw new IllegalArgumentException("Invalid " + key);
        return value.getAsJsonPrimitive();
    }
    /** Suggests the canonical bone an unknown name probably wraps, such as head_scale to head. */
    private static String aliasHint(String name) {
        return BONES.stream().filter(b -> name.startsWith(b + "_") || name.endsWith("_" + b))
            .max(Comparator.comparingInt(String::length).thenComparing(Comparator.reverseOrder()))
            .map(b -> "; map it with boneAliases {\"" + name + "\": \"" + b + "\"}").orElse("");
    }
    private static String clipName(Map<String, String> bindings, Set<String> clips, String action) {
        if (bindings.containsKey(action)) return bindings.get(action);
        if (clips.contains(action)) return action;
        for (String alias : ALIASES.getOrDefault(action, List.of())) if (clips.contains(alias)) return alias;
        return null;
    }
    private static AnimationChannel channel(JsonObject object, String key) { return object.has(key) ? AnimationChannel.parse(object.get(key)) : null; }
    private static Map<String, String> strings(JsonObject json, String key) {
        Map<String, String> result = new HashMap<>();
        if (json.has(key)) json.getAsJsonObject(key).entrySet().forEach(e -> {
            String value = e.getValue().getAsString();
            if (e.getKey().length() > 160 || value.length() > 160) throw new IllegalArgumentException("Profile mapping exceeds limit");
            result.put(e.getKey(), value);
        });
        return result;
    }
    public static String validId(String id) {
        if (id.length() > 160 || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || id.contains("..")) throw new IllegalArgumentException("Invalid profile id");
        return id;
    }
    public Clip resolve(String action) {
        String bound = bindings.get(action);
        if (bound != null) return clips.get(bound);
        Clip direct = clips.get(action); if (direct != null) return direct;
        for (String alias : ALIASES.getOrDefault(action, List.of())) { direct = clips.get(alias); if (direct != null) return direct; }
        if (action.startsWith("specialpose.")) {
            String base = action.substring("specialpose.".length());
            direct = clips.get("special_pose." + base); if (direct != null) return direct;
            for (String alias : ALIASES.getOrDefault(base, List.of())) { direct = clips.get("specialpose." + alias); if (direct != null) return direct; }
        }
        if (action.equals("run")) return resolve("walk");
        if (action.equals("sneak_walk")) return resolve("sneak");
        if (action.equals("walk")) return clips.get("idle");
        if (action.equals("crawl")) return resolve("swim");
        if (action.equals("climb_idle")) return clips.get("climb");
        return null;
    }
    private static final Map<String, List<String>> ALIASES = Map.ofEntries(
        Map.entry("swim", List.of("swimming", "swim_sprint")), Map.entry("elytra", List.of("elytras", "elytra_fly")),
        Map.entry("sneak", List.of("sneaking")), Map.entry("sneak_walk", List.of("sneaking.walk")),
        Map.entry("run", List.of("sprint")), Map.entry("climb_idle", List.of("climb.idle")));
    public static byte[] document(byte[] manifest, byte[] animation) {
        JsonObject json = new JsonObject();
        json.add("manifest", JsonParser.parseString(new String(manifest, StandardCharsets.UTF_8)));
        json.add("animation", JsonParser.parseString(new String(animation, StandardCharsets.UTF_8)));
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }
    private static final Set<String> RESERVED = Set.of("morphmod:vanilla", "morphmod:legacy_walk");
    public static boolean builtin(String id) { return RESERVED.contains(id); }
}
