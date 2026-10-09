package com.takumistudios.morphmod.character;

import com.google.gson.*;
import java.util.*;
import java.util.regex.Pattern;

/** Versioned, immutable character metadata shared by the server and client. */
public record CharacterDefinition(String id, String name, boolean free, float scale, String mob,
        String model, String texture, String animations, String profile,
        Map<String, String> bindings, Map<String, Anchor> anchors, Map<String, Emote> emotes,
        Set<String> animationNames, Set<String> bones) {
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    public record Anchor(String bone, float[] position, float[] rotation, float[] scale) { }
    public record Emote(String animation, boolean loop, Set<String> modes, Set<String> bones, boolean free, double seconds) { }

    public static CharacterDefinition parse(byte[] manifest, byte[] geometry, byte[] animation) {
        JsonObject json = JsonParser.parseString(new String(manifest, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        if (json.get("schemaVersion").getAsInt() != 1) throw new IllegalArgumentException("Unsupported character schema");
        String id = json.get("id").getAsString();
        if (id.length() > 160 || !ID.matcher(id).matches() || id.contains("..")) throw new IllegalArgumentException("Invalid character id");
        float scale = json.has("scale") ? json.get("scale").getAsFloat() : 1;
        if (!Float.isFinite(scale) || scale < 0.1F || scale > 4) throw new IllegalArgumentException("Scale must be 0.1..4");
        String name = json.get("name").getAsString();
        if (name.isBlank() || name.length() > 128) throw new IllegalArgumentException("Invalid character name");
        Set<String> bones = new HashSet<>();
        JsonObject geo = JsonParser.parseString(new String(geometry, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        JsonArray geometries = geo.getAsJsonArray("minecraft:geometry");
        if (geometries.size() != 1) throw new IllegalArgumentException("Exactly one geometry required");
        JsonArray boneArray = geometries.get(0).getAsJsonObject().getAsJsonArray("bones");
        if (boneArray.size() > 512) throw new IllegalArgumentException("Too many bones");
        for (JsonElement element : boneArray) {
            String bone = element.getAsJsonObject().get("name").getAsString();
            if (!bones.add(bone)) throw new IllegalArgumentException("Duplicate bone: " + bone);
        }
        Map<String, String> parents = new HashMap<>();
        for (JsonElement element : boneArray) {
            JsonObject b = element.getAsJsonObject();
            if (b.has("parent")) {
                String parent = b.get("parent").getAsString();
                if (!bones.contains(parent)) throw new IllegalArgumentException("Unknown parent: " + parent);
                parents.put(b.get("name").getAsString(), parent);
            }
        }
        for (String bone : bones) {
            Set<String> seen = new HashSet<>();
            for (String p = bone; p != null; p = parents.get(p))
                if (!seen.add(p)) throw new IllegalArgumentException("Bone cycle: " + bone);
        }
        JsonObject anim = JsonParser.parseString(new String(animation, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("animations");
        if (anim.size() > 512) throw new IllegalArgumentException("Too many animations");
        for (var entry : anim.entrySet()) {
            JsonObject channels = entry.getValue().getAsJsonObject().getAsJsonObject("bones");
            if (channels != null) for (String bone : channels.keySet())
                if (!bones.contains(bone)) throw new IllegalArgumentException(entry.getKey() + ": unknown bone " + bone);
        }
        Map<String, String> bindings = new HashMap<>();
        if (json.has("bindings")) json.getAsJsonObject("bindings").entrySet().forEach(e -> bindings.put(e.getKey(), e.getValue().getAsString()));
        for (String value : bindings.values()) if (!anim.has(value)) throw new IllegalArgumentException("Unknown bound animation: " + value);
        Map<String, Anchor> anchors = new HashMap<>();
        if (json.has("anchors")) json.getAsJsonObject("anchors").entrySet().forEach(e -> {
            JsonObject a = e.getValue().getAsJsonObject();
            String bone = a.get("bone").getAsString();
            // Missing anchors are omitted deliberately; equipment continues to work.
            if (bones.contains(bone)) anchors.put(e.getKey(), new Anchor(bone, vector(a, "position", 0, 128), vector(a, "rotation", 0, 360), vector(a, "scale", 1, 128)));
        });
        Map<String, Emote> emotes = new HashMap<>();
        if (json.has("emotes")) json.getAsJsonObject("emotes").entrySet().forEach(e -> {
            if (e.getKey().length() > 64 || !e.getKey().matches("[a-z0-9_.-]+")) throw new IllegalArgumentException("Invalid emote id");
            JsonObject a = e.getValue().getAsJsonObject();
            String clip = a.get("animation").getAsString();
            if (!anim.has(clip)) throw new IllegalArgumentException("Unknown emote animation: " + clip);
            Set<String> mask = strings(a, "bones", Set.of());
            if (!bones.containsAll(mask)) throw new IllegalArgumentException("Unknown emote mask bone");
            Set<String> modes = strings(a, "modes", Set.of("full"));
            if (modes.isEmpty() || !Set.of("full", "overlay").containsAll(modes)) throw new IllegalArgumentException("Invalid emote mode");
            if (modes.contains("overlay") && mask.isEmpty()) throw new IllegalArgumentException("Overlay emote requires a bone mask");
            double seconds = a.has("seconds") ? a.get("seconds").getAsDouble() : 5;
            if (!Double.isFinite(seconds) || seconds <= 0 || seconds > 120) throw new IllegalArgumentException("Invalid emote duration");
            emotes.put(e.getKey(), new Emote(clip, a.has("loop") && a.get("loop").getAsBoolean(), modes, mask, !a.has("free") || a.get("free").getAsBoolean(), seconds));
        });
        String mob = json.has("mob") ? json.get("mob").getAsString() : "";
        if (!mob.isEmpty() && !ID.matcher(mob).matches()) throw new IllegalArgumentException("Invalid mob id");
        String profile = json.has("profile") ? json.get("profile").getAsString() : "fabric-1.21.1";
        if (!Set.of("fabric-1.21.1", "fabric-1.20.4", "forge-1.20.1").contains(profile)) throw new IllegalArgumentException("Unknown animation profile");
        return new CharacterDefinition(id, name, !json.has("free") || json.get("free").getAsBoolean(), scale, mob,
                path(json.get("model").getAsString()), path(json.get("texture").getAsString()), path(json.get("animations").getAsString()),
                profile, Map.copyOf(bindings), Map.copyOf(anchors), Map.copyOf(emotes), Set.copyOf(anim.keySet()), Set.copyOf(bones));
    }
    public static String path(String path) {
        if (path.isBlank() || path.length() > 160 || !path.matches("[a-z0-9_./-]+") || path.startsWith("/") || path.contains(".."))
            throw new IllegalArgumentException("Unsafe resource path: " + path);
        return path;
    }
    private static float[] vector(JsonObject json, String key, float fallback, float limit) {
        float[] v = {fallback, fallback, fallback};
        if (json.has(key)) {
            JsonArray array = json.getAsJsonArray(key);
            if (array.size() != 3) throw new IllegalArgumentException("Vector must have 3 values");
            for (int i = 0; i < 3; i++) {
                v[i] = array.get(i).getAsFloat();
                if (!Float.isFinite(v[i]) || Math.abs(v[i]) > limit) throw new IllegalArgumentException("Invalid anchor vector");
            }
        }
        return v;
    }
    private static Set<String> strings(JsonObject json, String key, Set<String> fallback) {
        if (!json.has(key)) return fallback;
        Set<String> values = new HashSet<>();
        json.getAsJsonArray(key).forEach(e -> values.add(e.getAsString()));
        return Set.copyOf(values);
    }
    public String permission() { return "morphmod.character.use." + id.replace(':', '.').replace('/', '.'); }
    public String emotePermission(String emote) { return "morphmod.emote.use." + id.replace(':', '.').replace('/', '.') + "." + emote; }
}
