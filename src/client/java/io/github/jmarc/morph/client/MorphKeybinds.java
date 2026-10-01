package io.github.jmarc.morph.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.jmarc.morph.MorphMod;
import io.github.jmarc.morph.ability.AbilitySlot;
import io.github.jmarc.morph.client.screen.MorphSelectScreen;
import io.github.jmarc.morph.morph.MorphManager;
import io.github.jmarc.morph.network.DemorphPayload;
import io.github.jmarc.morph.network.UseAbilityPayload;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class MorphKeybinds {
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(MorphMod.id("morph"));

	public static final KeyMapping OPEN_MENU = KeyMappingHelper.registerKeyMapping(
		new KeyMapping("key.morphmod.menu", InputConstants.KEY_M, CATEGORY));
	public static final KeyMapping PRIMARY = KeyMappingHelper.registerKeyMapping(
		new KeyMapping("key.morphmod.primary", InputConstants.KEY_R, CATEGORY));
	public static final KeyMapping SECONDARY = KeyMappingHelper.registerKeyMapping(
		new KeyMapping("key.morphmod.secondary", InputConstants.KEY_G, CATEGORY));
	public static final KeyMapping DEMORPH = KeyMappingHelper.registerKeyMapping(
		new KeyMapping("key.morphmod.demorph", InputConstants.KEY_N, CATEGORY));

	private MorphKeybinds() {
	}

	public static void init() {
		// Forces class loading so the key mappings are registered during client init.
	}

	public static KeyMapping forSlot(AbilitySlot slot) {
		return slot == AbilitySlot.PRIMARY ? PRIMARY : SECONDARY;
	}

	public static void handle(Minecraft minecraft) {
		if (minecraft.player == null) {
			return;
		}
		while (OPEN_MENU.consumeClick()) {
			minecraft.gui.setScreen(new MorphSelectScreen());
		}
		boolean morphed = MorphManager.current(minecraft.player).isPresent();
		while (PRIMARY.consumeClick()) {
			if (morphed) {
				ClientPlayNetworking.send(new UseAbilityPayload(AbilitySlot.PRIMARY.ordinal()));
			}
		}
		while (SECONDARY.consumeClick()) {
			if (morphed) {
				ClientPlayNetworking.send(new UseAbilityPayload(AbilitySlot.SECONDARY.ordinal()));
			}
		}
		while (DEMORPH.consumeClick()) {
			if (morphed) {
				ClientPlayNetworking.send(DemorphPayload.INSTANCE);
			}
		}
	}
}
