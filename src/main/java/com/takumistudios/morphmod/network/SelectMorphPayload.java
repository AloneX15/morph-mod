package com.takumistudios.morphmod.network;

import com.takumistudios.morphmod.MorphMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client → server: "turn me into this entity type". */
public record SelectMorphPayload(Identifier entityType) implements CustomPacketPayload {
	public static final Type<SelectMorphPayload> TYPE = new Type<>(MorphMod.id("select_morph"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SelectMorphPayload> CODEC =
		ByteBufCodecs.stringUtf8(256).map(text -> new SelectMorphPayload(Identifier.tryParse(text)), payload -> payload.entityType().toString()).cast();

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
