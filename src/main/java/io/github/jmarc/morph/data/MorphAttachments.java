package io.github.jmarc.morph.data;

import io.github.jmarc.morph.MorphMod;
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
			.syncWith(Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), AttachmentSyncPredicate.targetOnly())
	);

	private MorphAttachments() {
	}

	public static void init() {
		// Forces class loading so the attachment types are registered during mod init.
	}
}
