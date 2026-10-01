package io.github.jmarc.morph.test;

import io.github.jmarc.morph.ability.AbilitySlot;
import io.github.jmarc.morph.client.ClientMorphState;
import io.github.jmarc.morph.client.MorphKeybinds;
import io.github.jmarc.morph.client.render.DisguiseManager;
import io.github.jmarc.morph.client.screen.MorphSelectScreen;
import io.github.jmarc.morph.morph.MorphManager;
import io.github.jmarc.morph.morph.Passive;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;

/**
 * End-to-end check in a real client + integrated server: morph, stats, hitbox, rendering,
 * ability cooldown, menu and demorph. Screenshots land in run/screenshots.
 */
public class MorphClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getConnection().waitForChunksRender();
			world.getServer().runCommand("time set day");
			world.getServer().runCommand("gamemode survival @a");

			// --- Warden: stats + hitbox -------------------------------------------------------
			world.getServer().runCommand("morph into minecraft:warden @a");
			context.waitTicks(10);
			context.runOnClient(minecraft -> {
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
			context.setScreen(() -> null);

			// --- Bat: flight -------------------------------------------------------------------
			world.getServer().runCommand("morph into minecraft:bat @a");
			context.waitTicks(10);
			context.runOnClient(minecraft -> {
				LocalPlayer player = minecraft.player;
				check(player.getAbilities().mayfly, "bat can fly in survival");
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
			});
		}
	}

	private static void check(boolean condition, String what) {
		if (!condition) {
			throw new AssertionError("Failed: " + what);
		}
	}
}
