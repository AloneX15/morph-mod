package com.takumistudios.morphmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.ability.AbilitySlot;
import com.takumistudios.morphmod.client.screen.MorphSelectScreen;
import com.takumistudios.morphmod.morph.MorphManager;
import com.takumistudios.morphmod.network.DemorphPayload;
import com.takumistudios.morphmod.network.UseAbilityPayload;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class MorphKeybinds {
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(MorphMod.id("morph"));

	public static final KeyMapping OPEN_MENU = KeyMappingHelper.registerKeyMapping(
		new KeyMapping("key.morphmod.menu", InputConstants.getKey("key.keyboard.j").getValue(), CATEGORY));
	public static final KeyMapping PRIMARY = KeyMappingHelper.registerKeyMapping(
		new KeyMapping("key.morphmod.primary", InputConstants.KEY_R, CATEGORY));
	public static final KeyMapping SECONDARY = KeyMappingHelper.registerKeyMapping(
		new KeyMapping("key.morphmod.secondary", InputConstants.getKey("key.keyboard.k").getValue(), CATEGORY));
	public static final KeyMapping DEMORPH = KeyMappingHelper.registerKeyMapping(
		new KeyMapping("key.morphmod.demorph", InputConstants.UNKNOWN.getValue(), CATEGORY));
	public static final KeyMapping EMOTES = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.morphmod.emotes", InputConstants.UNKNOWN.getValue(), CATEGORY));

	private MorphKeybinds() {
	}
	private static boolean checkedConflicts;
	private static int nextRequestTick;
	private static final KeyMapping[] OWN = {OPEN_MENU, PRIMARY, SECONDARY, DEMORPH, EMOTES};

	/** Only migrate defaults; respect any binding explicitly chosen by the user. */
	public static void resolveConflicts(Minecraft minecraft) {
		for (KeyMapping mapping : OWN) {
			if (!mapping.isDefault() || mapping.isUnbound() || !conflicts(mapping, minecraft.options.keyMappings)) continue;
			boolean found = false;
			for (String letter : new String[]{"j", "k", "r"}) {
				mapping.setKey(InputConstants.getKey("key.keyboard." + letter));
				if (!conflicts(mapping, minecraft.options.keyMappings)) { found = true; break; }
			}
			if (!found) mapping.setKey(InputConstants.UNKNOWN);
			MorphMod.LOGGER.debug("Moved conflicting default {} to {}", mapping.getName(), mapping.saveString());
		}
		KeyMapping.resetMapping();
	}
	private static boolean conflicts(KeyMapping mapping, KeyMapping[] all) {
		for (KeyMapping other : all) if (other != mapping && !other.isUnbound() && mapping.same(other)) return true;
		return false;
	}
	private static boolean requestReady() {
		int now = Minecraft.getInstance().player.tickCount;
		if (now < nextRequestTick && nextRequestTick - now <= 3) return false;
		nextRequestTick = now + 3;
		return true;
	}

	public static void init() {
		// Forces class loading so the key mappings are registered during client init.
	}

	public static KeyMapping forSlot(AbilitySlot slot) {
		return slot == AbilitySlot.PRIMARY ? PRIMARY : SECONDARY;
	}

	public static void handle(Minecraft minecraft) {
		if (!checkedConflicts) { resolveConflicts(minecraft); checkedConflicts = true; }
		if (minecraft.player == null || !ClientMorphState.protocolReady()) {
			return;
		}
		while (OPEN_MENU.consumeClick()) {
			//? if >=26.2 {
			minecraft.gui.setScreen(new MorphSelectScreen());
			//?} else {
			/*minecraft.setScreen(new MorphSelectScreen());
			*///?}
		}
		boolean morphed = MorphManager.current(minecraft.player).isPresent();
		while (EMOTES.consumeClick()) com.takumistudios.morphmod.client.screen.CharacterScreen.open(new com.takumistudios.morphmod.client.screen.CharacterScreen(true));
		while (PRIMARY.consumeClick()) {
			if (morphed && ClientPlayNetworking.canSend(UseAbilityPayload.TYPE) && requestReady()) {
				ClientPlayNetworking.send(new UseAbilityPayload(AbilitySlot.PRIMARY.ordinal()));
			}
		}
		while (SECONDARY.consumeClick()) {
			if (morphed && ClientPlayNetworking.canSend(UseAbilityPayload.TYPE) && requestReady()) {
				ClientPlayNetworking.send(new UseAbilityPayload(AbilitySlot.SECONDARY.ordinal()));
			}
		}
		while (DEMORPH.consumeClick()) {
			if ((morphed || !com.takumistudios.morphmod.character.CharacterManager.selected(minecraft.player).isEmpty()) && ClientPlayNetworking.canSend(DemorphPayload.TYPE) && requestReady()) {
				ClientPlayNetworking.send(DemorphPayload.INSTANCE);
			}
		}
	}
}
