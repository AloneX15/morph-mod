package com.takumistudios.morphmod.character;

import java.util.*;

/** Pure naming and fallback policy; inputs come from vanilla player state. */
public final class AnimationResolver {
    private static final Map<String, List<String>> ALIASES = Map.ofEntries(
        Map.entry("swim", List.of("swimming", "swim_sprint")), Map.entry("elytra", List.of("elytras", "elytra_fly")),
        Map.entry("sneak_walk", List.of("sneaking.walk")), Map.entry("sneak", List.of("sneaking")),
        Map.entry("climb_idle", List.of("climb.idle")), Map.entry("run", List.of("sprint")),
        Map.entry("walk", List.of("walk")), Map.entry("idle", List.of("idle")));
    public static String resolve(CharacterDefinition definition, String action) {
        String explicit = definition.bindings().get(action);
        if (explicit != null) return explicit;
        List<String> candidates = new ArrayList<>();
        candidates.add(action); candidates.addAll(ALIASES.getOrDefault(action, List.of()));
        boolean right = action.endsWith("_rightArm");
        if (right || action.endsWith("_leftArm")) {
            String base = action.substring(0, action.lastIndexOf('_'));
            switch (base) {
                case "bow" -> candidates.add(right ? "bow_aim" : "bow_aim.offhand");
                case "crossbow", "crossbow_charge" -> candidates.add("crossbow_aim");
                case "shield" -> candidates.add("block");
                case "trident" -> candidates.add("trident_aim");
                case "eat" -> { candidates.add(right ? "eat" : "eat.offhand"); candidates.add(right ? "eat.main_hand" : "eat.off_hand"); }
                case "item" -> { candidates.add(right ? "hand_right_interact" : "hand_left_interact"); candidates.add(right ? "use_item" : "use_item.offhand"); }
                default -> { }
            }
        }
        if (action.startsWith("specialpose.")) {
            String base = action.substring("specialpose.".length());
            candidates.addAll(ALIASES.getOrDefault(base, List.of(base)).stream().map(s -> "specialpose." + s).toList());
            candidates.add("special_pose." + base);
        }
        for (String candidate : candidates) if (definition.animationNames().contains(candidate)) return candidate;
        return "";
    }
    public static String movement(CharacterDefinition definition, String action, boolean special) {
        String clip = special ? resolve(definition, "specialpose." + action) : "";
        if (clip.isEmpty()) clip = resolve(definition, action);
        if (clip.isEmpty() && action.equals("run")) clip = resolve(definition, "walk");
        if (clip.isEmpty() && action.equals("sneak_walk")) clip = resolve(definition, "sneak");
        if (clip.isEmpty() && action.equals("climb_idle")) clip = resolve(definition, "climb");
        if (clip.isEmpty() && action.equals("crawl")) clip = resolve(definition, "swim");
        return clip.isEmpty() ? resolve(definition, "idle") : clip;
    }
    public static String swing(CharacterDefinition definition, boolean mainHand, boolean mainRight) {
        boolean logicalMain = definition.profile().equals("forge-1.20.1");
        boolean first = logicalMain ? mainHand : mainHand == mainRight;
        return resolve(definition, first ? "swing.main_hand" : "swing.off_hand");
    }
    public static String arm(CharacterDefinition definition, String action, boolean right, boolean main, boolean active) {
        String physical = action + (right ? "_rightArm" : "_leftArm");
        if (definition.bindings().containsKey(physical)) return definition.bindings().get(physical);
        if (definition.animationNames().contains(physical)) return physical;
        if (!active && !action.equals("crossbow")) return "";
        List<String> legacy = switch (action) {
            case "bow" -> List.of(main ? "bow_aim" : "bow_aim.offhand");
            case "crossbow", "crossbow_charge" -> List.of("crossbow_aim");
            case "shield" -> List.of("block");
            case "trident" -> List.of("trident_aim");
            case "eat" -> List.of(main ? "eat.main_hand" : "eat.off_hand", main ? "eat" : "eat.offhand");
            case "item" -> List.of(right ? "hand_right_interact" : "hand_left_interact", main ? "use_item" : "use_item.offhand");
            default -> List.of();
        };
        for (String candidate : legacy) { String clip = resolve(definition, candidate); if (!clip.isEmpty()) return clip; }
        return "";
    }
    private AnimationResolver() { }
}
