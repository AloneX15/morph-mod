package com.takumistudios.morphmod.test;

import com.takumistudios.morphmod.ability.AbilitySlot;
import com.takumistudios.morphmod.client.ClientMorphState;
import com.takumistudios.morphmod.network.UseAbilityPayload;
import com.takumistudios.morphmod.network.ProtocolPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import com.mojang.blaze3d.platform.InputConstants;
import com.takumistudios.morphmod.client.MorphKeybinds;
import com.takumistudios.morphmod.client.render.DisguiseManager;
import com.takumistudios.morphmod.client.screen.MorphSelectScreen;
import com.takumistudios.morphmod.morph.MorphManager;
import com.takumistudios.morphmod.morph.Passive;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.player.LocalPlayer;
import com.takumistudios.morphmod.compat.EntityTypes;
import net.minecraft.world.entity.LivingEntity;

/**
 * End-to-end check in a real client + integrated server: morph, stats, hitbox, rendering,
 * ability cooldown, menu and demorph. Screenshots land in run/screenshots.
 */
public class MorphClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
        if (System.getProperty("morphmod.test.observer") != null) { CharacterMultiplayerGameTest.observe(context); return; }
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			//? if >=26.2 {
			world.getConnection().waitForChunksRender();
			//?} else {
			/*world.getClientLevel().waitForChunksRender();
			*///?}
			world.getServer().runCommand("time set day");
			world.getServer().runCommand("gamemode survival @a");
			context.waitFor(mc -> com.takumistudios.morphmod.client.character.ClientCharacters.get("morphmod:otter") != null, 600);
			context.waitFor(mc -> com.takumistudios.morphmod.client.character.ClientCharacters.allowed("morphmod:otter"));
			if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("luckperms")) {
				world.getServer().runCommand("lp group default permission set morphmod.character.use.morphmod.otter false");
				context.waitFor(mc -> !com.takumistudios.morphmod.client.character.ClientCharacters.allowed("morphmod:otter"), 400);
				world.getServer().runCommand("lp user Player0 permission set morphmod.character.use.morphmod.otter true");
				context.waitFor(mc -> com.takumistudios.morphmod.client.character.ClientCharacters.allowed("morphmod:otter"), 400);
				world.getServer().runCommand("lp user Player0 permission unset morphmod.character.use.morphmod.otter");
				world.getServer().runCommand("lp group default permission unset morphmod.character.use.morphmod.otter");
				context.waitFor(mc -> com.takumistudios.morphmod.client.character.ClientCharacters.allowed("morphmod:otter"), 400);
			}
			context.setScreen(() -> new com.takumistudios.morphmod.client.screen.CharacterScreen(false));
			click(context, "Otter");
			click(context, net.minecraft.network.chat.Component.translatable("screen.morphmod.morph").getString());
			context.waitFor(mc -> com.takumistudios.morphmod.character.CharacterManager.selected(mc.player).equals("morphmod:otter"));
			context.takeScreenshot("morph-otter-menu");
			context.setScreen(() -> null);
			context.runOnClient(mc -> {
				var proxy = com.takumistudios.morphmod.client.character.CharacterRenderManager.proxy(mc.player);
				check(proxy != null && proxy.definition.id().equals("morphmod:otter"), "custom character proxy");
				check(mc.player.getMaxHealth() == 20, "character uses human stats");
				check(Math.abs(mc.player.getBbHeight() - 1.8F) < 0.01F, "character keeps human dimensions");
				var renderer = (com.takumistudios.morphmod.client.character.CharacterRenderer)mc.getEntityRenderDispatcher().getRenderer(proxy);
				var state = renderer.createRenderState(proxy, 0);
				var model = renderer.getGeoModel().getBakedModel(renderer.getGeoModel().getModelResource(state));
				check(model.getBone("tail").isPresent(), "actual otter geometry loaded, not missing model");
				check(renderer.getGeoModel().getBakedAnimation(proxy, "idle") != null, "idle baked");
				check(renderer.getGeoModel().getBakedAnimation(proxy, "__morph_overlay_demo_dance") != null, "masked overlay baked");
				mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
			});
			context.waitTicks(5);
			context.takeScreenshot("morph-otter-third-person");
			context.setScreen(() -> new com.takumistudios.morphmod.client.screen.CharacterScreen(true));
			click(context, "demo_dance");
			click(context, net.minecraft.network.chat.Component.translatable("screen.morphmod.emote_mode.full").getString());
			click(context, net.minecraft.network.chat.Component.translatable("screen.morphmod.play").getString());
			context.waitFor(mc -> com.takumistudios.morphmod.character.CharacterManager.emote(mc.player) != null);
			context.takeScreenshot("morph-otter-emotes");
			context.setScreen(() -> null);
			context.getInput().holdKeyFor(options -> options.keyUp, 10);
			context.runOnClient(mc -> check(com.takumistudios.morphmod.character.CharacterManager.emote(mc.player) != null, "overlay continues while walking"));
			context.takeScreenshot("morph-otter-overlay");
			context.runOnClient(mc -> ClientPlayNetworking.send(new com.takumistudios.morphmod.network.CharacterActionPayload("stop", "", "")));
			context.waitFor(mc -> com.takumistudios.morphmod.character.CharacterManager.emote(mc.player) == null);
			context.waitTicks(10); // Let walking inertia settle before a stationary full-body emote.
			context.runOnClient(mc -> ClientPlayNetworking.send(new com.takumistudios.morphmod.network.CharacterActionPayload("play", "demo_dance", "full")));
			context.waitFor(mc -> com.takumistudios.morphmod.character.CharacterManager.emote(mc.player) != null);
			context.getInput().holdKeyFor(options -> options.keyUp, 10);
			context.waitFor(mc -> com.takumistudios.morphmod.character.CharacterManager.emote(mc.player) == null);
			world.getServer().runCommand("item replace entity @a armor.head with minecraft:diamond_helmet");
			world.getServer().runCommand("item replace entity @a armor.chest with minecraft:elytra");
			world.getServer().runCommand("item replace entity @a weapon.mainhand with minecraft:diamond_sword");
			context.waitTicks(5);
			context.takeScreenshot("morph-otter-equipment");
			context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
			context.waitTicks(15);
			context.takeScreenshot("morph-otter-first-person");
			world.getServer().runCommand("item replace entity @a weapon.mainhand with minecraft:air");
			context.waitTicks(15);
			context.takeScreenshot("morph-otter-first-person-arm");
			world.getServer().runCommand("clear @a");
			context.runOnClient(mc -> ClientPlayNetworking.send(new com.takumistudios.morphmod.network.CharacterActionPayload("clear", "", "")));
			context.waitFor(mc -> com.takumistudios.morphmod.character.CharacterManager.selected(mc.player).isEmpty());

			// --- Warden: stats + hitbox -------------------------------------------------------
			world.getServer().runCommand("morph into minecraft:warden @a");
			context.waitTicks(10);
			context.runOnClient(minecraft -> {
				check(ClientMorphState.protocolReady(), "protocol negotiated");
				MorphKeybinds.DEMORPH.setKey(InputConstants.getKey("key.keyboard.f9"));
				net.minecraft.client.KeyMapping.resetMapping();
				LocalPlayer player = minecraft.player;
				check(MorphManager.current(player).map(def -> def.type() == EntityTypes.WARDEN).orElse(false), "client sees warden morph");
				check(Math.abs(player.getBbHeight() - 2.9F) < 0.01F, "warden hitbox height, was " + player.getBbHeight());
				check(player.getMaxHealth() == 500.0F, "warden max health, was " + player.getMaxHealth());
				check(MorphManager.has(player, Passive.VIBRATION_SENSE), "warden passives");
				LivingEntity disguise = DisguiseManager.disguiseFor(player);
				check(disguise != null && disguise.getType() == EntityTypes.WARDEN, "warden disguise exists");
				minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
			});
			context.waitTicks(10);
			context.takeScreenshot("morph-warden-third-person");

			// --- Ability: R sends the request and the server answers with a cooldown ----------
			context.runOnClient(minecraft -> {
				ClientPlayNetworking.send(new ProtocolPayload(999));
				ClientPlayNetworking.send(new UseAbilityPayload(0));
			});
			context.waitTicks(3);
			context.runOnClient(minecraft -> check(ClientMorphState.cooldownProgress(AbilitySlot.PRIMARY) == 0, "incompatible protocol is ignored"));
			context.runOnClient(minecraft -> ClientPlayNetworking.send(new ProtocolPayload(ProtocolPayload.VERSION)));
			context.waitTicks(3);
			context.runOnClient(minecraft -> ClientPlayNetworking.send(new UseAbilityPayload(-1)));
			context.waitTicks(2);
			context.runOnClient(minecraft -> check(ClientMorphState.cooldownProgress(AbilitySlot.PRIMARY) == 0, "invalid slot does not activate an ability"));
			context.getInput().pressKey(MorphKeybinds.PRIMARY);
			context.waitTicks(5);
			context.runOnClient(minecraft ->
				check(ClientMorphState.cooldownProgress(AbilitySlot.PRIMARY) > 0, "sonic boom cooldown started"));

			// --- Menu ---------------------------------------------------------------------------
			world.getServer().runCommand("morph unlock all @a");
			context.waitTicks(5);
			context.getInput().pressKey(MorphKeybinds.OPEN_MENU);
			context.waitForScreen(MorphSelectScreen.class);
			context.takeScreenshot("morph-menu");
			context.runOnClient(minecraft -> {
				// Exercise the smallest supported GUI layout independently of desktop resolution.
				//? if >=26.2 {
				var screen = minecraft.gui.screen();
				//?} else {
				/*var screen = minecraft.screen;
				*///?}
				screen.resize(320, 180);
				for (var child : screen.children()) {
					if (child instanceof net.minecraft.client.gui.components.AbstractWidget widget) {
						check(widget.getX() >= 0 && widget.getRight() <= 320, "widgets fit small GUI width");
						check(widget.getY() >= 0 && widget.getBottom() <= 180, "widgets fit small GUI height");
					}
				}
			});
			context.takeScreenshot("morph-menu-small");
			context.setScreen(() -> null);
			var reload = context.computeOnClient(minecraft -> {
				minecraft.getLanguageManager().setSelected("es_es");
				minecraft.options.languageCode = "es_es";
				return minecraft.reloadResourcePacks();
			});
			context.waitFor(minecraft -> reload.isDone(), 600);
			reload.join();
			// Resource completion precedes the loading overlay's fade-out.
			//? if >=26.2 {
			context.waitFor(minecraft -> minecraft.gui.overlay() == null, 600);
			//?} else {
			/*context.waitFor(minecraft -> minecraft.getOverlay() == null, 600);
			*///?}
			context.setScreen(MorphSelectScreen::new);
			context.waitTicks(2);
			context.takeScreenshot("morph-menu-spanish");
			context.setScreen(() -> null);

			// --- Bat: flight -------------------------------------------------------------------
			world.getServer().runCommand("morph into minecraft:bat @a");
			context.waitTicks(10);
			context.runOnClient(minecraft -> {
				LocalPlayer player = minecraft.player;
				check(player.getAbilities().mayfly, "bat can fly in survival");
				check(player.getMaxHealth() == 6.0F, "bat health synced after resource reload");
				check(ClientMorphState.cooldownProgress(AbilitySlot.PRIMARY) == 0, "changing forms clears the old client cooldown");
				check(player.getBbHeight() < 1.0F, "bat hitbox is small, was " + player.getBbHeight());
			});
			context.takeScreenshot("morph-bat-third-person");

			// --- Demorph: everything back to normal ------------------------------------------
			context.getInput().pressKey(MorphKeybinds.DEMORPH);
			context.waitTicks(10);
			context.runOnClient(minecraft -> {
				LocalPlayer player = minecraft.player;
				check(MorphManager.current(player).isEmpty(), "demorphed");
				check(player.getMaxHealth() == 20.0F, "max health restored, was " + player.getMaxHealth());
				check(Math.abs(player.getBbHeight() - 1.8F) < 0.01F, "hitbox restored, was " + player.getBbHeight());
				check(!player.getAbilities().mayfly, "flight removed");
				minecraft.options.setCameraType(CameraType.FIRST_PERSON);
				MorphKeybinds.DEMORPH.setKey(InputConstants.UNKNOWN);
				net.minecraft.client.KeyMapping.resetMapping();
				check(MorphKeybinds.DEMORPH.isUnbound(), "unbound key uses version-correct UNKNOWN");
			});
		}
	}

	private static void check(boolean condition, String what) {
		if (!condition) {
			throw new AssertionError("Failed: " + what);
		}
	}
	private static void click(ClientGameTestContext context, String label) {
		context.runOnClient(mc -> {
			//? if >=26.2 {
			var screen = mc.gui.screen();
			//?} else {
			/*var screen = mc.screen;
			*///?}
			var button = screen.children().stream().filter(w -> w instanceof net.minecraft.client.gui.components.Button)
				.map(w -> (net.minecraft.client.gui.components.Button)w).filter(b -> b.visible && b.active && b.getMessage().getString().equals(label)).findFirst().orElseThrow();
			button.onPress(new net.minecraft.client.input.KeyEvent(257, 0, 0));
		});
		context.waitTick();
	}
}
