package com.takumistudios.morphmod.animation;

import com.google.gson.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnimationProfilesTest {
    private AnimationProfile bundled() throws Exception {
        try (var manifest = getClass().getResourceAsStream("/animations/dedsafio_4/profile.json");
             var animation = getClass().getResourceAsStream("/animations/dedsafio_4/dedsafio_4.animation.json")) {
            assertNotNull(manifest); assertNotNull(animation);
            return AnimationProfile.parse(AnimationProfile.document(manifest.readAllBytes(), animation.readAllBytes()));
        }
    }
    @Test void originalFileImportsAllActionsWithoutGeometryAndReportsUnsupportedBone() throws Exception {
        var profile = bundled(); assertEquals("morphmod:dedsafio_4", profile.id());
        for (String action : List.of("walk", "run", "sneak", "sneak_walk", "swim", "elytra", "sleep", "sit", "swing.main_hand", "bow_rightArm", "shield_leftArm"))
            assertNotNull(profile.resolve(action), action);
        assertTrue(profile.warnings().stream().anyMatch(w -> w.contains("tire_inflator")));
        assertNull(profile.resolve("jump")); // Missing actions retain vanilla, never an unrelated idle pose.
        var copy = AnimationProfile.parse(profile.data()); assertEquals(profile.hash(), copy.hash());
    }
    @Test void isolatedQueriesUseDegreesAndGeckoSpeedUnits() {
        var value = AnimationMath.compile("math.sin(q.anim_time * 600) * math.clamp(query.ground_speed * 100, 0, 40)");
        var first = new AnimationMath.Context().set("anim_time", 0.15).set("ground_speed", 0.2);
        var second = new AnimationMath.Context().set("anim_time", 0).set("ground_speed", 1);
        assertEquals(20, first.evaluate(value), 0.00001); assertEquals(0, second.evaluate(value), 0.00001);
        assertEquals(20, first.evaluate(value), 0.00001); // Independent render-state contexts.
        assertThrows(IllegalArgumentException.class, () -> AnimationMath.compile("query.unknown * 2"));
        assertThrows(IllegalArgumentException.class, () -> AnimationMath.compile("variable.x = 1; return variable.x"));
        assertThrows(IllegalArgumentException.class, () -> first.set("anim_time", Double.NaN));
    }
    @Test void linearDiscontinuousAndCatmullKeyframes() {
        var context = new AnimationMath.Context();
        assertEquals(0.00001F, AnimationChannel.parse(JsonParser.parseString("[1e-5,0,0]")).sample(0, 0, context), 0.0000001F);
        var channel = AnimationChannel.parse(JsonParser.parseString("{\"0\":[0,0,0],\"1\":{\"pre\":[10,0,0],\"post\":[20,0,0]},\"2\":[30,0,0]}"));
        assertEquals(5, channel.sample(0.5, 0, context), 0.00001);
        assertEquals(20, channel.sample(1, 0, context), 0.00001);
        assertEquals(25, channel.sample(1.5, 0, context), 0.00001);
        var smooth = AnimationChannel.parse(JsonParser.parseString("{\"0\":[0,0,0],\"1\":{\"post\":[10,0,0],\"lerp_mode\":\"catmullrom\"},\"2\":[20,0,0],\"3\":[30,0,0]}"));
        assertEquals(4.375, smooth.sample(0.5, 0, context), 0.00001);
        assertThrows(IllegalArgumentException.class, () -> AnimationChannel.parse(JsonParser.parseString("{\"NaN\":[0,0,0]}")));
        assertThrows(IllegalArgumentException.class, () -> AnimationChannel.parse(JsonParser.parseString("{\"0\":[0,0,0],\"0.0\":[0,0,0]}")));
    }
    @Test void invalidBindingsAliasesAndQueriesRejectEntireProfile() throws Exception {
        JsonObject document = JsonParser.parseString(new String(bundled().data(), StandardCharsets.UTF_8)).getAsJsonObject();
        document.getAsJsonObject("manifest").getAsJsonObject("bindings").addProperty("walk", "missing");
        assertThrows(IllegalArgumentException.class, () -> AnimationProfile.parse(document.toString().getBytes(StandardCharsets.UTF_8)));
        document.getAsJsonObject("manifest").getAsJsonObject("bindings").addProperty("walk", "idle");
        document.getAsJsonObject("manifest").getAsJsonObject("boneAliases").addProperty("something", "missing");
        assertThrows(IllegalArgumentException.class, () -> AnimationProfile.parse(document.toString().getBytes(StandardCharsets.UTF_8)));
    }
    @Test void loopAndHoldTiming() {
        assertEquals(0.5, new AnimationProfile.Clip(1, true, false, Map.of()).time(2.5));
        assertEquals(1, new AnimationProfile.Clip(1, false, true, Map.of()).time(2.5));
    }
    @Test void explicitLookQueriesPreventAddingVanillaLookTwice() {
        assertEquals(1, AnimationChannel.parse(JsonParser.parseString("[\"-65-query.pitch\",0,0]")).lookQueries());
        assertEquals(3, AnimationChannel.parse(JsonParser.parseString("[\"90+query.pitch\",\"query.yaw\",0]")).lookQueries());
        assertEquals(0, AnimationChannel.parse(JsonParser.parseString("[0,0,\"math.sin(query.anim_time)\"]")).lookQueries());
    }
    @Test void priorityIndividualForceAndRemovedProfiles() {
        UUID player = UUID.randomUUID();
        var settings = new AnimationSettings("test:global", List.of(new AnimationSettings.Rule("group.low", "test:low", 1), new AnimationSettings.Rule("group.high", "test:high", 100)), Map.of(player, "test:individual"));
        assertEquals("test:forced", settings.resolve(player, node -> true, id -> true, "test:forced").profile());
        assertEquals("test:individual", settings.resolve(player, node -> true, id -> true, null).profile());
        settings = settings.assign(List.of(player), null);
        assertEquals("test:high", settings.resolve(player, node -> true, id -> true, null).profile());
        assertEquals("test:low", settings.resolve(player, node -> !node.equals("group.high"), id -> true, null).profile());
        assertEquals("test:global", settings.resolve(player, node -> false, id -> true, null).profile());
        assertEquals("test:low", settings.resolve(player, node -> true, id -> !id.equals("test:high"), null).profile());
        assertEquals("morphmod:vanilla", settings.resolve(player, node -> true, id -> false, null).profile());
    }
    @Test void deterministicTiesAndPersistentUuidOverrides() {
        UUID player = UUID.randomUUID();
        var settings = new AnimationSettings("morphmod:legacy_walk", List.of(new AnimationSettings.Rule("rank.z", "test:z", 10), new AnimationSettings.Rule("rank.a", "test:a", 10)), Map.of());
        assertEquals("test:a", settings.resolve(player, node -> true, id -> true, null).profile());
        settings = settings.assign(List.of(player), "morphmod:vanilla");
        assertEquals(settings, AnimationSettings.parse(settings.json()));
        assertEquals("morphmod:vanilla", AnimationSettings.parse(settings.json()).resolve(player, node -> true, id -> true, null).profile());
    }
}
