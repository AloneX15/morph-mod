package com.takumistudios.morphmod.animation;

import com.google.gson.*;
import java.util.*;
import java.util.function.Predicate;

public record AnimationSettings(String defaultProfile, List<Rule> rules, Map<UUID, String> overrides) {
    public record Rule(String permission, String profile, int priority) { }
    public record Selection(String profile, String source) { }
    public static AnimationSettings defaults() { return new AnimationSettings("morphmod:legacy_walk", List.of(), Map.of()); }
    public Selection resolve(UUID player, Predicate<String> permitted, Predicate<String> exists, String forced) {
        if (forced != null && exists.test(forced)) return new Selection(forced, "force");
        String individual = overrides.get(player);
        if (individual != null && exists.test(individual)) return new Selection(individual, "player");
        Rule matched = rules.stream().filter(r -> exists.test(r.profile) && permitted.test(r.permission))
            .sorted(Comparator.comparingInt(Rule::priority).reversed().thenComparing(Rule::profile).thenComparing(Rule::permission)).findFirst().orElse(null);
        if (matched != null) return new Selection(matched.profile, "luckperms:" + matched.permission);
        return new Selection(exists.test(defaultProfile) ? defaultProfile : "morphmod:vanilla", "default");
    }
    public static AnimationSettings parse(JsonObject json) {
        if (!json.get("schemaVersion").toString().equals("1")) throw new IllegalArgumentException("Unsupported animation settings schema");
        String global = AnimationProfile.validId(json.get("defaultProfile").getAsString());
        List<Rule> rules = new ArrayList<>(); Map<UUID, String> overrides = new HashMap<>();
        for (JsonElement value : json.getAsJsonArray("rules")) {
            JsonObject rule = value.getAsJsonObject(); String node = rule.get("permission").getAsString();
            if (node.length() > 160 || !node.matches("[a-z0-9_.-]+")) throw new IllegalArgumentException("Invalid permission node");
            rules.add(new Rule(node, AnimationProfile.validId(rule.get("profile").getAsString()), rule.get("priority").getAsInt()));
        }
        if (rules.size() > 256 || json.getAsJsonObject("overrides").size() > 100000) throw new IllegalArgumentException("Animation assignments exceed limit");
        json.getAsJsonObject("overrides").entrySet().forEach(e -> overrides.put(UUID.fromString(e.getKey()), AnimationProfile.validId(e.getValue().getAsString())));
        return new AnimationSettings(global, List.copyOf(rules), Map.copyOf(overrides));
    }
    public JsonObject json() {
        JsonObject json = new JsonObject(); json.addProperty("schemaVersion", 1); json.addProperty("defaultProfile", defaultProfile);
        JsonArray list = new JsonArray();
        rules.forEach(r -> { JsonObject entry = new JsonObject(); entry.addProperty("permission", r.permission); entry.addProperty("profile", r.profile); entry.addProperty("priority", r.priority); list.add(entry); });
        json.add("rules", list); JsonObject players = new JsonObject();
        overrides.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> players.addProperty(e.getKey().toString(), e.getValue()));
        json.add("overrides", players); return json;
    }
    public AnimationSettings assign(Collection<UUID> players, String profile) {
        Map<UUID, String> result = new HashMap<>(overrides);
        players.forEach(id -> { if (profile == null) result.remove(id); else result.put(id, profile); });
        return new AnimationSettings(defaultProfile, rules, Map.copyOf(result));
    }
}
