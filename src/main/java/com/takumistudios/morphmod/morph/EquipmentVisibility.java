package com.takumistudios.morphmod.morph;

import com.takumistudios.morphmod.character.CharacterPermissions;
import com.takumistudios.morphmod.data.MorphAttachments;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server side of the inventory toggle that shows armor and held items on a player's morph.
 * Hidden by default; only players with {@link #PERMISSION} (or operators) can turn it on.
 */
public final class EquipmentVisibility {
	public static final String PERMISSION = "morphmod.equipment.toggle";

	public static boolean canToggle(ServerPlayer player) {
		boolean fallback = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(player.createCommandSourceStack());
		return CharacterPermissions.check(player, PERMISSION, fallback);
	}

	/** Syncs the permission to the owner and hides the equipment of players who lost it. */
	public static boolean refresh(ServerPlayer player) {
		boolean allowed = canToggle(player);
		if (!Boolean.valueOf(allowed).equals(player.getAttached(MorphAttachments.CAN_TOGGLE_EQUIPMENT))) {
			player.setAttached(MorphAttachments.CAN_TOGGLE_EQUIPMENT, allowed);
		}
		if (!allowed && player.hasAttached(MorphAttachments.SHOW_EQUIPMENT)) {
			player.removeAttached(MorphAttachments.SHOW_EQUIPMENT);
		}
		return allowed;
	}

	/** Flips the setting; returns false (and changes nothing) without permission. */
	public static boolean toggle(ServerPlayer player) {
		if (!refresh(player)) {
			return false;
		}
		if (MorphAttachments.showsEquipment(player)) {
			player.removeAttached(MorphAttachments.SHOW_EQUIPMENT);
		} else {
			player.setAttached(MorphAttachments.SHOW_EQUIPMENT, true);
		}
		return true;
	}

	private EquipmentVisibility() {
	}
}
