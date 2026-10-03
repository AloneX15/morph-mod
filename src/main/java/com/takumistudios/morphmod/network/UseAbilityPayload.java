package com.takumistudios.morphmod.network;

import com.takumistudios.morphmod.MorphMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client → server: "use the ability in this slot" (0 = primary, 1 = secondary). */
public record UseAbilityPayload(int slot) implements CustomPacketPayload {
	public static final Type<UseAbilityPayload> TYPE = new Type<>(MorphMod.id("use_ability"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UseAbilityPayload> CODEC =
		ByteBufCodecs.VAR_INT.map(UseAbilityPayload::new, UseAbilityPayload::slot).cast();

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
