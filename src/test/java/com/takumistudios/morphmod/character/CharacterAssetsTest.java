package com.takumistudios.morphmod.character;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CharacterAssetsTest {
    private Map<String, byte[]> files() throws IOException {
        Map<String, byte[]> files = new HashMap<>();
        for (String name : List.of("character.json", "otter.geo.json", "otter.animation.json", "otter.png")) {
            try (var input = getClass().getResourceAsStream("/characters/otter/" + name)) { assertNotNull(input); files.put(name, input.readAllBytes()); }
        }
        return files;
    }
    private static void edit(Map<String, byte[]> files, String path, java.util.function.Consumer<JsonObject> action) {
        JsonObject json = JsonParser.parseString(new String(files.get(path), StandardCharsets.UTF_8)).getAsJsonObject();
        action.accept(json); files.put(path, json.toString().getBytes(StandardCharsets.UTF_8));
    }
    @Test void templateRoundTripsWithStableHashAndMaskedDance() throws Exception {
        var bundle = CharacterBundle.create(files()); var restored = CharacterBundle.decode(bundle.archive(), bundle.hash());
        assertEquals("morphmod:otter", restored.definition().id()); assertEquals("", restored.definition().mob());
        assertEquals(bundle.hash(), CharacterBundle.create(files()).hash());
        assertTrue(restored.definition().emotes().get("demo_dance").modes().contains("overlay"));
    }
    @Test void corruptHashIsRejected() throws Exception {
        var bundle = CharacterBundle.create(files()); byte[] bytes = bundle.archive().clone(); bytes[5] ^= 1;
        assertThrows(IOException.class, () -> CharacterBundle.decode(bytes, bundle.hash()));
    }
    @Test void truncatedTextureIsRejectedEvenWithValidHeader() throws Exception {
        var input = files(); input.put("otter.png", Arrays.copyOf(input.get("otter.png"), 24));
        assertThrows(IOException.class, () -> CharacterBundle.create(input));
    }
    @Test void unsafePathsAndMissingResourcesAreRejected() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> CharacterDefinition.path("../secret"));
        assertThrows(IllegalArgumentException.class, () -> CharacterDefinition.path("C:/secret"));
        var files = files(); files.remove("otter.png"); assertThrows(IOException.class, () -> CharacterBundle.create(files));
    }
    @Test void unknownBoneIsRejected() throws Exception {
        var files = files(); edit(files, "otter.animation.json", j -> j.getAsJsonObject("animations").getAsJsonObject("idle").getAsJsonObject("bones").add("nonexistent", new JsonObject()));
        assertThrows(IllegalArgumentException.class, () -> CharacterBundle.create(files));
    }
    @Test void boneCyclesAreRejected() throws Exception {
        var files = files(); edit(files, "otter.geo.json", j -> j.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones").get(0).getAsJsonObject().addProperty("parent", "body"));
        assertThrows(IllegalArgumentException.class, () -> CharacterBundle.create(files));
    }
    @Test void schemaAndNonFiniteScaleAreRejected() throws Exception {
        var files = files(); edit(files, "character.json", j -> j.addProperty("schemaVersion", 2));
        assertThrows(IllegalArgumentException.class, () -> CharacterBundle.create(files));
        var invalid = files(); edit(invalid, "character.json", j -> j.addProperty("scale", "NaN"));
        assertThrows(IllegalArgumentException.class, () -> CharacterBundle.create(invalid));
    }
    @Test void overlayRequiresExplicitBones() throws Exception {
        var files = files(); edit(files, "character.json", j -> j.getAsJsonObject("emotes").getAsJsonObject("demo_dance").remove("bones"));
        assertThrows(IllegalArgumentException.class, () -> CharacterBundle.create(files));
    }
    @Test void aliasesAndFallbacksUseAvailableAnimations() throws Exception {
        var d = CharacterBundle.create(files()).definition();
        assertEquals("swimming", AnimationResolver.movement(d, "swim", false));
        assertEquals("elytras", AnimationResolver.movement(d, "elytra", false));
        assertEquals("idle", AnimationResolver.movement(d, "run", false));
        assertEquals("sneaking", AnimationResolver.movement(d, "sneak_walk", false));
    }
    @Test void profilesDistinguishLogicalHandFromPhysicalArm() throws Exception {
        var files = files(); edit(files, "otter.animation.json", j -> { var a = j.getAsJsonObject("animations"); a.add("swing.main_hand", new JsonObject()); a.add("swing.off_hand", new JsonObject()); });
        var fabric = CharacterBundle.create(files).definition();
        assertEquals("swing.off_hand", AnimationResolver.swing(fabric, true, false));
        edit(files, "character.json", j -> j.addProperty("profile", "forge-1.20.1"));
        var forge = CharacterBundle.create(files).definition(); assertEquals("swing.main_hand", AnimationResolver.swing(forge, true, false));
    }
    @Test void legacyItemActionsRespectLogicalHandsAndActiveUse() throws Exception {
        var input = files(); edit(input, "otter.animation.json", j -> {
            var clips = j.getAsJsonObject("animations"); clips.add("bow_aim", new JsonObject()); clips.add("bow_aim.offhand", new JsonObject()); clips.add("use_item", new JsonObject());
            clips.remove("bow_leftArm"); clips.remove("bow_rightArm"); clips.remove("item_leftArm"); clips.remove("item_rightArm");
            clips.remove("hand_left_interact"); clips.remove("hand_right_interact");
        });
        var d = CharacterBundle.create(input).definition();
        assertEquals("bow_aim", AnimationResolver.arm(d, "bow", false, true, true));
        assertEquals("bow_aim.offhand", AnimationResolver.arm(d, "bow", true, false, true));
        assertEquals("", AnimationResolver.arm(d, "item", true, true, false));
        assertEquals("use_item", AnimationResolver.arm(d, "item", true, true, true));
    }
    @Test void fragmentsRejectDuplicatesNegativeOffsetsAndTruncation() throws Exception {
        var assembly = new FragmentAssembly(CharacterBundle.FRAGMENT + 3);
        assertThrows(IOException.class, () -> assembly.accept(-1, new byte[3]));
        assertThrows(IOException.class, () -> assembly.accept(0, new byte[3]));
        assembly.accept(0, new byte[CharacterBundle.FRAGMENT]);
        assertThrows(IOException.class, () -> assembly.accept(0, new byte[CharacterBundle.FRAGMENT]));
        assertThrows(IOException.class, assembly::finish);
        assembly.accept(1, new byte[3]); assertEquals(CharacterBundle.FRAGMENT + 3, assembly.finish().length);
    }
    @Test void oversizedTransferIsRejectedBeforeAllocation() {
        assertThrows(IllegalArgumentException.class, () -> new FragmentAssembly(CharacterBundle.MAX_CHARACTER + 1));
        assertThrows(IllegalArgumentException.class, () -> new FragmentAssembly(-1));
    }
}
