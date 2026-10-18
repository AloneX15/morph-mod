package com.takumistudios.morphmod.network;

import com.takumistudios.morphmod.MorphMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Channel version 1 is the capability negotiation; older clients never receive it. */
public record AnimationPayload(int stage, String id, String hash, int size, int index, byte[] data) implements CustomPacketPayload {
    public static final Type<AnimationPayload> TYPE = new Type<>(MorphMod.id("animation_profiles_v1"));
    public static final StreamCodec<FriendlyByteBuf, AnimationPayload> CODEC = StreamCodec.of((buf, value) -> {
        buf.writeVarInt(value.stage); buf.writeUtf(value.id, 160); buf.writeUtf(value.hash, 64);
        buf.writeVarInt(value.size); buf.writeVarInt(value.index); buf.writeByteArray(value.data);
    }, buf -> new AnimationPayload(buf.readVarInt(), buf.readUtf(160), buf.readUtf(64), buf.readVarInt(), buf.readVarInt(), buf.readByteArray(32768)));
    public static AnimationPayload signal(int stage, String revision, byte[] data) { return new AnimationPayload(stage, "", revision, 0, 0, data); }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
