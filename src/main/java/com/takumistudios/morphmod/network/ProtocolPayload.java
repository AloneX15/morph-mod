package com.takumistudios.morphmod.network;

import com.takumistudios.morphmod.MorphMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server advertises the protocol; the client acknowledges the same version. */
public record ProtocolPayload(int version) implements CustomPacketPayload {
	public static final int VERSION = 2;
	public static final Type<ProtocolPayload> TYPE = new Type<>(MorphMod.id("protocol"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ProtocolPayload> CODEC =
		ByteBufCodecs.VAR_INT.map(ProtocolPayload::new, ProtocolPayload::version).cast();
	@Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
