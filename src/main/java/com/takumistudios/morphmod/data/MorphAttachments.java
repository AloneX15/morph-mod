package com.takumistudios.morphmod.data;

import com.takumistudios.morphmod.MorphMod;
import java.util.List;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

/**
 * Per-player data stored with the Fabric Data Attachment API.
 */
public final class MorphAttachments {
	public static final AttachmentType<Identifier> CURRENT_CHARACTER = AttachmentRegistry.create(MorphMod.id("current_character"),
		builder -> builder.persistent(Identifier.CODEC).syncWith(Identifier.STREAM_CODEC, AttachmentSyncPredicate.all()));
	public static final AttachmentType<String> CURRENT_EMOTE = AttachmentRegistry.create(MorphMod.id("current_emote"),
		builder -> builder.syncWith(ByteBufCodecs.STRING_UTF8, AttachmentSyncPredicate.all()));
	public static final AttachmentType<List<String>> UNLOCKED_CHARACTERS = AttachmentRegistry.create(MorphMod.id("unlocked_characters"),
		builder -> builder.initializer(List::of).persistent(com.mojang.serialization.Codec.STRING.listOf()).copyOnDeath());
	public static final int MAX_FORMS = 4096;
	/**
	 * Entity type id of the current morph. Absent means the player is in human form.
	 * Synced to every client so they can render the disguise. Not copied on death.
	 */
	public static final AttachmentType<Identifier> CURRENT_MORPH = AttachmentRegistry.create(
		MorphMod.id("current_morph"),
		builder -> builder
			.persistent(Identifier.CODEC)
			.syncWith(Identifier.STREAM_CODEC, AttachmentSyncPredicate.all())
	);

	/** Forms this player has unlocked. Only synced to the owner (used by the morph menu). */
	public static final AttachmentType<List<Identifier>> UNLOCKED_MORPHS = AttachmentRegistry.create(
		MorphMod.id("unlocked_morphs"),
		builder -> builder
			.initializer(List::of)
			.persistent(Identifier.CODEC.listOf())
			.copyOnDeath()
			.syncWith(Identifier.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_FORMS)), AttachmentSyncPredicate.targetOnly())
	);

	/**
	 * Whether this player's morph shows their armor and held items. Absent means hidden (default).
	 * Synced to every client because all of them render the disguise.
	 */
	public static final AttachmentType<Boolean> SHOW_EQUIPMENT = AttachmentRegistry.create(
		MorphMod.id("show_equipment"),
		builder -> builder
			.persistent(com.mojang.serialization.Codec.BOOL)
			.copyOnDeath()
			.syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.all())
	);

	/** Server-computed permission for the inventory equipment toggle. Only synced to the owner. */
	public static final AttachmentType<Boolean> CAN_TOGGLE_EQUIPMENT = AttachmentRegistry.create(
		MorphMod.id("can_toggle_equipment"),
		builder -> builder.syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.targetOnly())
	);

	public static boolean showsEquipment(net.minecraft.world.entity.player.Player player) {
		return Boolean.TRUE.equals(player.getAttached(SHOW_EQUIPMENT));
	}

	private MorphAttachments() {
	}

	public static void init() {
		// Forces class loading so the attachment types are registered during mod init.
	}
}
