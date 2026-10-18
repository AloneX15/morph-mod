package com.takumistudios.morphmod.test;

import com.google.gson.JsonParser;
import com.takumistudios.morphmod.animation.AnimationCatalog;
import com.takumistudios.morphmod.client.animation.*;
import com.takumistudios.morphmod.client.render.PlayerWalkState;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import net.fabricmc.fabric.api.client.gametest.v1.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CameraType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;

public final class AnimationClientGameTest implements FabricClientGameTest {
    private static final String ID = "morphmod:dedsafio_4";
    @Override public void runTest(ClientGameTestContext context) {
        if (System.getProperty("morphmod.test.observer") != null) return;
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            context.waitFor(mc -> mc.player != null && mc.player.onGround() && ClientAnimationProfiles.get(ID) != null, 600);
            world.getServer().runCommand("time set day");
            world.getServer().runCommand("morph animation set @a " + ID);
            context.waitFor(mc -> ClientAnimationProfiles.selected(mc.player.getUUID()).equals(ID));
            context.runOnClient(mc -> {
                var renderer = (AvatarRenderer<LocalPlayer>)mc.getEntityRenderDispatcher().getRenderer(mc.player);
                var state = renderer.createRenderState(mc.player, 0); var animation = ((PlayerWalkState)state).morphmod$animationPose();
                check(animation.active && !animation.legacy && animation.managed, "server selects imported full profile");
                renderer.getModel().setupAnim(state);
                check(Math.abs(renderer.getModel().rightArm.xRot - (float)Math.toRadians(-2.5)) < 0.01F, "imported idle overrides vanilla baseline");
                float previousPitch = mc.player.getXRot(); var previousPose = mc.player.getPose();
                try {
                    mc.player.setXRot(20); mc.player.setSwimming(true); mc.player.setPose(net.minecraft.world.entity.Pose.SWIMMING);
                    var swimState = renderer.createRenderState(mc.player, 0);
                    check(((PlayerWalkState)swimState).morphmod$animationPose().replacesBodyOrientation(), "swimming waist replaces global pitch");
                    renderer.getModel().setupAnim(swimState);
                    check(Math.abs(renderer.getModel().head.xRot - (float)Math.toRadians(25)) < 0.01F, "swimming pitch is not added twice through waist and head");
                    var wings = new net.minecraft.client.model.object.equipment.ElytraModel(net.minecraft.client.model.object.equipment.ElytraModel.createLayer().bakeRoot());
                    wings.setupAnim(swimState);
                    check(Math.abs(wings.root().xRot - (float)Math.toRadians(110)) < 0.01F, "elytra follows imported waist orientation");
                } finally { mc.player.setSwimming(false); mc.player.setPose(previousPose); mc.player.setXRot(previousPitch); }
                mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            });
            context.getInput().holdKey(options -> options.keyUp); context.waitTicks(10); context.takeScreenshot("animation-profile-walk");
            context.getInput().holdKey(options -> options.keyShift); context.waitTicks(8);
            context.runOnClient(mc -> {
                check(mc.player.isCrouching(), "actual crouch input is active");
                var renderer = (AvatarRenderer<LocalPlayer>)mc.getEntityRenderDispatcher().getRenderer(mc.player);
                var state = renderer.createRenderState(mc.player, 0); renderer.getModel().setupAnim(state);
                check(Math.abs(renderer.getModel().body.xRot - (float)Math.toRadians(28)) < 0.01F, "imported crouch leans forward in model space");
            });
            context.takeScreenshot("animation-profile-crouch");
            context.getInput().releaseKey(options -> options.keyShift); context.getInput().releaseKey(options -> options.keyUp);
            context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
            context.waitTicks(5); context.takeScreenshot("animation-profile-first-person-empty");
            context.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
            world.getServer().runCommand("item replace entity @a armor.chest with minecraft:diamond_chestplate");
            world.getServer().runCommand("item replace entity @a weapon.mainhand with minecraft:diamond_pickaxe");
            world.getServer().runCommand("item replace entity @a weapon.offhand with minecraft:shield");
            context.waitTicks(10); context.takeScreenshot("animation-profile-armor-items");
            context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
            context.waitTicks(5); context.takeScreenshot("animation-profile-first-person-items");
            context.getInput().holdKey(options -> options.keyUse); context.waitTicks(10); context.takeScreenshot("animation-profile-first-person-shield");
            context.getInput().releaseKey(options -> options.keyUse);
            world.getServer().runCommand("item replace entity @a weapon.offhand with minecraft:air");
            world.getServer().runCommand("item replace entity @a weapon.mainhand with minecraft:bow");
            world.getServer().runCommand("give @a minecraft:arrow 16");
            context.waitTicks(5); context.getInput().holdKey(options -> options.keyUse); context.waitTicks(25);
            context.runOnClient(mc -> {
                check(mc.player.isUsingItem(), "bow use unaffected by profile");
                var renderer = (AvatarRenderer<LocalPlayer>)mc.getEntityRenderDispatcher().getRenderer(mc.player);
                var state = renderer.createRenderState(mc.player, 0); var pose = ((PlayerWalkState)state).morphmod$animationPose();
                check(pose.hasArm(true) && pose.hasArm(false), "bow clip poses both arms");
                check(Math.abs(pose.armValue(true, 0) + 75 - mc.player.getXRot()) < 0.01F, "bow query.pitch evaluated");
                renderer.getModel().setupAnim(state);
                check(Math.abs(renderer.getModel().rightArm.xRot - (float)Math.toRadians(-75 + mc.player.getXRot())) < 0.01F, "imported bow points forward, not behind the body");
            });
            context.takeScreenshot("animation-profile-first-person-bow"); context.getInput().releaseKey(options -> options.keyUse);
            context.waitTicks(15);
            context.runOnClient(mc -> {
                var main = mc.player.getMainHandItem(); var off = mc.player.getOffhandItem();
                try {
                    var crossbow = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CROSSBOW);
                    crossbow.set(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES, net.minecraft.world.item.component.ChargedProjectiles.of(new net.minecraft.world.item.ItemStackTemplate(net.minecraft.world.item.Items.ARROW)));
                    mc.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));
                    mc.player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, crossbow);
                    var renderer = (AvatarRenderer<LocalPlayer>)mc.getEntityRenderDispatcher().getRenderer(mc.player);
                    var pose = ((PlayerWalkState)renderer.createRenderState(mc.player, 0)).morphmod$animationPose();
                    check(Math.abs(pose.armValue(true, 0) + 60.349F) < 0.01F, "offhand charged crossbow has priority over main-hand generic item");
                } finally { mc.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, main); mc.player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, off); }
            });
            context.runOnClient(mc -> { mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT); mc.player.setMainArm(net.minecraft.world.entity.HumanoidArm.LEFT); });
            context.waitTicks(5); context.takeScreenshot("animation-profile-left-handed");
            context.runOnClient(mc -> mc.player.setMainArm(net.minecraft.world.entity.HumanoidArm.RIGHT));
            world.getServer().runCommand("morph animation force morphmod:vanilla");
            context.waitFor(mc -> ClientAnimationProfiles.selected(mc.player.getUUID()).equals("morphmod:vanilla"));
            context.runOnClient(mc -> {
                var renderer = (AvatarRenderer<LocalPlayer>)mc.getEntityRenderDispatcher().getRenderer(mc.player);
                var pose = ((PlayerWalkState)renderer.createRenderState(mc.player, 0)).morphmod$animationPose(); check(!pose.active && !pose.legacy, "forced vanilla disables both animation systems");
            });
            world.getServer().runCommand("morph animation force off"); context.waitFor(mc -> ClientAnimationProfiles.selected(mc.player.getUUID()).equals(ID));
            reload(context, world);
            world.getServer().runCommand("morph animation reset @a");
            context.waitFor(mc -> ClientAnimationProfiles.selected(mc.player.getUUID()).equals("morphmod:legacy_walk"));
            context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
        }
    }
    private void reload(ClientGameTestContext context, TestSingleplayerContext world) {
        Path manifest = FabricLoader.getInstance().getConfigDir().resolve("morphmod/animations/dedsafio_4/profile.json");
        try {
            byte[] original = Files.readAllBytes(manifest); Path animationFile = manifest.resolveSibling("dedsafio_4.animation.json"); byte[] originalAnimation = Files.readAllBytes(animationFile);
            var old = ClientAnimationProfiles.get(ID); var serverProfile = AnimationCatalog.current().profiles().get(ID);
            try {
                Files.writeString(manifest, "{invalid"); world.getServer().runCommand("morph animation reload"); context.waitTicks(30);
                check(AnimationCatalog.current().profiles().get(ID) == serverProfile && ClientAnimationProfiles.get(ID) == old, "failed reload retains both catalogs");
                var json = JsonParser.parseString(new String(original, StandardCharsets.UTF_8)).getAsJsonObject(); json.addProperty("name", "Reloaded Dedsafio"); Files.writeString(manifest, json.toString());
                world.getServer().runCommand("morph animation reload"); context.waitFor(mc -> ClientAnimationProfiles.get(ID).name().equals("Reloaded Dedsafio"), 600);
                check(!ClientAnimationProfiles.get(ID).hash().equals(old.hash()), "hot reload publishes new content hash");
                var document = JsonParser.parseString(new String(originalAnimation, StandardCharsets.UTF_8)).getAsJsonObject();
                document.getAsJsonObject("animations").remove("bow_rightArm"); document.getAsJsonObject("animations").remove("bow_leftArm");
                Files.writeString(animationFile, document.toString()); world.getServer().runCommand("morph animation reload");
                context.waitFor(mc -> ClientAnimationProfiles.get(ID).resolve("bow_rightArm") == null, 600);
                context.getInput().holdKey(options -> options.keyUse); context.waitTicks(15);
                context.runOnClient(mc -> {
                    check(mc.player.isUsingItem(), "bow is active for missing-clip fallback");
                    var renderer = (AvatarRenderer<LocalPlayer>)mc.getEntityRenderDispatcher().getRenderer(mc.player);
                    var state = renderer.createRenderState(mc.player, 0); var pose = ((PlayerWalkState)state).morphmod$animationPose();
                    check(!pose.hasArm(true) && !pose.hasArm(false), "missing bow clip preserves vanilla first-person arms");
                    renderer.getModel().setupAnim(state);
                    check(Math.abs(renderer.getModel().rightArm.xRot + (float)Math.PI / 2 - renderer.getModel().head.xRot) < 0.1F, "missing bow clip preserves vanilla third-person bow");
                });
                context.getInput().releaseKey(options -> options.keyUse);
            } finally { Files.write(manifest, original); Files.write(animationFile, originalAnimation); }
            world.getServer().runCommand("morph animation reload"); context.waitFor(mc -> ClientAnimationProfiles.get(ID).hash().equals(old.hash()), 600);
        } catch (java.io.IOException e) { throw new AssertionError(e); }
    }
    private static void check(boolean condition, String label) { if (!condition) throw new AssertionError(label); }
}
