package com.takumistudios.morphmod.test;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.level.GameType;
import io.netty.channel.embedded.EmbeddedChannel;

/** Reproduces vanilla's mock login, including permission preloading when installed. */
final class TestPlayers {
    static ServerPlayer create(GameTestHelper helper) {
        if (!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("luckperms")) return helper.makeMockServerPlayerInLevel();
        var profile = new GameProfile(UUID.randomUUID(), "test-mock-player");
        net.luckperms.api.LuckPermsProvider.get().getUserManager().loadUser(profile.id()).join();
        var cookie = CommonListenerCookie.createInitial(profile, false);
        var level = helper.getLevel();
        var player = new ServerPlayer(level.getServer(), level, profile, cookie.clientInformation()) {
            @Override public GameType gameMode() { return GameType.CREATIVE; }
        };
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        return player;
    }
    private TestPlayers() { }
}
