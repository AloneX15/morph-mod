package com.takumistudios.morphmod.network;

import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.character.CharacterBundle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Shared configuration/play transport. Every field is bounded before allocation. */
public record CharacterPayload(int stage, String id, String hash, int size, int index, byte[] data) implements CustomPacketPayload {
    public static final Type<CharacterPayload> TYPE = new Type<>(MorphMod.id("character_assets"));
    public static final StreamCodec<FriendlyByteBuf, CharacterPayload> CODEC = StreamCodec.of((buf, value) -> {
        buf.writeVarInt(value.stage); buf.writeUtf(value.id, 160); buf.writeUtf(value.hash, 64);
        buf.writeVarInt(value.size); buf.writeVarInt(value.index); buf.writeByteArray(value.data);
    }, buf -> new CharacterPayload(buf.readVarInt(), buf.readUtf(160), buf.readUtf(64), buf.readVarInt(), buf.readVarInt(), buf.readByteArray(CharacterBundle.FRAGMENT)));
    public static CharacterPayload signal(int stage, byte[] bytes) { return new CharacterPayload(stage, "", "", 0, 0, bytes); }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
