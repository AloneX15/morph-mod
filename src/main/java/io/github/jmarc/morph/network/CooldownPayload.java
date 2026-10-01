package io.github.jmarc.morph.network;

import io.github.jmarc.morph.MorphMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server → client: an ability went on cooldown, so the HUD can draw it. */
public record CooldownPayload(int slot, int ticks) implements CustomPacketPayload {
	public static final Type<CooldownPayload> TYPE = new Type<>(MorphMod.id("cooldown"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CooldownPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, CooldownPayload::slot,
		ByteBufCodecs.VAR_INT, CooldownPayload::ticks,
		CooldownPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
