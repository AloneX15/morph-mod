package com.takumistudios.morphmod.test;

import com.takumistudios.morphmod.character.CharacterManager;
import com.takumistudios.morphmod.client.character.*;
import com.takumistudios.morphmod.network.CharacterActionPayload;
import java.nio.file.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

/** A second JVM connects over TCP and independently verifies resources and tracked attachments. */
public final class CharacterMultiplayerGameTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        if (System.getProperty("morphmod.test.observer") != null) return;
        var properties = new Properties(); properties.setProperty("online-mode", "false"); properties.setProperty("max-players", "4");
        try (var server = context.worldBuilder().createServer(properties)) {
            try (var connection = server.connect()) {
                context.waitFor(mc -> ClientCharacters.get("morphmod:otter") != null, 600);
                int port = server.computeOnServer(s -> s.getPort());
                server.runCommand("whitelist off");
                Path childDir = FabricLoader.getInstance().getGameDir().resolve("observer").toAbsolutePath();
                Files.createDirectories(childDir); Files.deleteIfExists(childDir.resolve("observer.ready"));
                Process observer = launch(port, childDir);
                try {
                    context.waitFor(mc -> {
                        if (!observer.isAlive()) throw new AssertionError("Observer exited early; see observer.log");
                        return mc.level.players().size() >= 2;
                    }, 2400);
                    server.runCommand("tp Player0 0.5 -60 0.5");
                    server.runCommand("tp MorphObserver 0.5 -60 -3.5");
                    context.runOnClient(mc -> ClientPlayNetworking.send(new CharacterActionPayload("select", "morphmod:otter", "")));
                    context.waitFor(mc -> CharacterManager.selected(mc.player).equals("morphmod:otter"));
                    context.runOnClient(mc -> ClientPlayNetworking.send(new CharacterActionPayload("play", "demo_dance", "full")));
                    context.waitFor(mc -> Files.exists(childDir.resolve("observer.full.ready")), 1200);
                    context.runOnClient(mc -> ClientPlayNetworking.send(new CharacterActionPayload("play", "demo_dance", "overlay")));
                    context.waitFor(mc -> CharacterManager.emote(mc.player) != null);
                    context.waitFor(mc -> {
                        if (!observer.isAlive()) throw new AssertionError("Observer failed; see observer.log");
                        return Files.exists(childDir.resolve("observer.ready"));
                    }, 1200);
                    context.takeScreenshot("morph-multiplayer-host");
                    // Configuration cache acknowledgement and persisted character on a fresh TCP connection.
                } finally { observer.destroy(); if (!observer.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)) observer.destroyForcibly(); }
            }
            try (var reconnect = server.connect()) {
                context.waitFor(mc -> ClientCharacters.get("morphmod:otter") != null, 600);
                context.waitFor(mc -> CharacterManager.selected(mc.player).equals("morphmod:otter"));
                context.runOnClient(mc -> { if (CharacterManager.emote(mc.player) != null) throw new AssertionError("Emote persisted on reconnect"); });
                server.runCommand("morph character reload");
                context.waitTicks(60);
                context.runOnClient(mc -> { if (CharacterRenderManager.proxy(mc.player) == null) throw new AssertionError("Reload lost the selected character"); });
                var previous = com.takumistudios.morphmod.character.CharacterCatalog.bundles();
                Path manifest = FabricLoader.getInstance().getConfigDir().resolve("morphmod/characters/otter/character.json");
                byte[] original = Files.readAllBytes(manifest);
                try {
                    Files.writeString(manifest, "{invalid");
                    boolean rejected = com.takumistudios.morphmod.character.CharacterCatalog.load().handle((v, e) -> e != null).join();
                    if (!rejected || com.takumistudios.morphmod.character.CharacterCatalog.bundles() != previous) throw new AssertionError("Invalid reload replaced the catalog");
                } finally { Files.write(manifest, original); }
                server.runOnServer(s -> {
                    com.takumistudios.morphmod.character.CharacterCatalog.publish(Map.of());
                    s.getPlayerList().getPlayers().forEach(com.takumistudios.morphmod.network.CharacterNetworking::offer);
                });
                context.waitFor(mc -> ClientCharacters.get("morphmod:otter") == null, 600);
                context.waitFor(mc -> CharacterManager.selected(mc.player).isEmpty());
                server.runOnServer(s -> com.takumistudios.morphmod.character.CharacterCatalog.publish(previous));
            }
        } catch (Exception e) { throw new AssertionError("Multiplayer test failed", e); }
    }
    static void observe(ClientGameTestContext context) {
        String address = System.getProperty("morphmod.test.observer");
        context.runOnClient(mc -> {
            var data = new net.minecraft.client.multiplayer.ServerData("Morph test", address, net.minecraft.client.multiplayer.ServerData.Type.OTHER);
            //? if >=26.2 {
            var screen = mc.gui.screen();
            //?} else {
            /*var screen = mc.screen;
            *///?}
            net.minecraft.client.gui.screens.ConnectScreen.startConnecting(screen, mc, net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(address), data, false, null);
        });
        context.waitFor(mc -> mc.player != null && mc.level != null && ClientCharacters.get("morphmod:otter") != null, 2400);
        context.waitFor(mc -> remoteMode(mc, "full"), 1200);
        //? if >=26.2 {
        context.waitFor(mc -> mc.gui.overlay() == null, 600);
        //?} else {
        /*context.waitFor(mc -> mc.getOverlay() == null, 600);
        *///?}
        context.takeScreenshot("morph-multiplayer-full");
        try { Files.writeString(FabricLoader.getInstance().getGameDir().resolve("observer.full.ready"), "ready"); }
        catch (java.io.IOException e) { throw new AssertionError(e); }
        context.waitFor(mc -> remoteMode(mc, "overlay"), 1200);
        context.runOnClient(mc -> {
            var player = mc.level.players().stream().filter(p -> p != mc.player && !CharacterManager.selected(p).isEmpty()).findFirst().orElseThrow();
            var proxy = CharacterRenderManager.proxy(player);
            if (proxy == null || !proxy.emote.mode().equals("overlay")) throw new AssertionError("Remote character/emote missing");
            var renderer = (CharacterRenderer)mc.getEntityRenderDispatcher().getRenderer(proxy);
            if (renderer.getGeoModel().getBakedAnimation(proxy, "__morph_overlay_demo_dance") == null) throw new AssertionError("Remote resources not baked");
            mc.player.setYRot(0); mc.player.setXRot(0);
        });
        context.waitTicks(10); context.takeScreenshot("morph-multiplayer-observer");
        try { Files.writeString(FabricLoader.getInstance().getGameDir().resolve("observer.ready"), "ready"); }
        catch (java.io.IOException e) { throw new AssertionError(e); }
        context.waitFor(mc -> mc.level == null, 2400);
    }
    private static boolean remoteMode(net.minecraft.client.Minecraft mc, String mode) {
        return mc.level.players().stream().anyMatch(p -> p != mc.player && CharacterManager.selected(p).equals("morphmod:otter")
            && CharacterManager.emote(p) != null && CharacterManager.emote(p).mode().equals(mode));
    }
    private static Process launch(int port, Path directory) throws java.io.IOException {
        List<String> args = new ArrayList<>();
        args.add(ProcessHandle.current().info().command().orElseThrow());
        ManagementFactory.getRuntimeMXBean().getInputArguments().stream().filter(a -> !a.startsWith("-agentlib:") && !a.startsWith("-javaagent:") && !a.startsWith("-agentpath:")).forEach(args::add);
        args.add("-Dmorphmod.test.observer=localhost:" + port);
        args.add("-cp"); args.add(System.getProperty("java.class.path"));
        args.add("net.fabricmc.devlaunchinjector.Main");
        args.addAll(List.of("--username", "MorphObserver", "--gameDir", directory.toString(), "--accessToken", "FabricMC", "--version", "Fabric"));
        return new ProcessBuilder(args).directory(directory.toFile()).redirectErrorStream(true).redirectOutput(directory.resolve("observer.log").toFile()).start();
    }
}
