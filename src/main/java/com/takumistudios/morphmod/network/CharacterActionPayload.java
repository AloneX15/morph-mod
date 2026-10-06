package com.takumistudios.morphmod.network;

import com.takumistudios.morphmod.MorphMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Actions: select, clear, play, stop, equipment (toggle). IDs and modes are validated on the server. */
public record CharacterActionPayload(String action, String id, String mode) implements CustomPacketPayload {
    public static final Type<CharacterActionPayload> TYPE = new Type<>(MorphMod.id("character_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CharacterActionPayload> CODEC = StreamCodec.of((buf, v) -> {
        buf.writeUtf(v.action, 16); buf.writeUtf(v.id, 160); buf.writeUtf(v.mode, 16);
    }, buf -> new CharacterActionPayload(buf.readUtf(16), buf.readUtf(160), buf.readUtf(16)));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
