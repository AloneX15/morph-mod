package io.github.jmarc.morph.network;

import io.github.jmarc.morph.MorphMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client → server: "turn me into this entity type". */
public record SelectMorphPayload(Identifier entityType) implements CustomPacketPayload {
	public static final Type<SelectMorphPayload> TYPE = new Type<>(MorphMod.id("select_morph"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SelectMorphPayload> CODEC =
		Identifier.STREAM_CODEC.map(SelectMorphPayload::new, SelectMorphPayload::entityType).cast();

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
