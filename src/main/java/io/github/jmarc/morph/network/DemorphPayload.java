package io.github.jmarc.morph.network;

import io.github.jmarc.morph.MorphMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client → server: "go back to human form". */
public record DemorphPayload() implements CustomPacketPayload {
	public static final DemorphPayload INSTANCE = new DemorphPayload();
	public static final Type<DemorphPayload> TYPE = new Type<>(MorphMod.id("demorph"));
	public static final StreamCodec<RegistryFriendlyByteBuf, DemorphPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
