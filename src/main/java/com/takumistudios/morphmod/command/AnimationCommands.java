package com.takumistudios.morphmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.animation.*;
import com.takumistudios.morphmod.character.CharacterPermissions;
import com.takumistudios.morphmod.network.AnimationNetworking;
import java.util.*;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

public final class AnimationCommands {
    private static boolean busy;
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        busy = false;
        dispatcher.register(Commands.literal("morph").then(Commands.literal("animation").requires(AnimationCommands::admin)
            .then(Commands.literal("list").executes(new SafeCommand(c -> {
                List<String> ids = new ArrayList<>(AnimationCatalog.current().profiles().keySet()); ids.addAll(List.of("morphmod:vanilla", "morphmod:legacy_walk")); Collections.sort(ids);
                return output(c.getSource(), String.join(", ", ids));
            })))
            .then(Commands.literal("inspect").then(profile().executes(new SafeCommand(c -> {
                String id = StringArgumentType.getString(c, "profile"); if (!exists(c.getSource(), id)) return 0;
                AnimationProfile profile = AnimationCatalog.current().profiles().get(id);
                return output(c.getSource(), profile == null ? id : id + " — " + profile.name() + "\n" + String.join("\n", profile.warnings()));
            }))))
            .then(Commands.literal("status").then(Commands.argument("player", EntityArgument.player()).executes(new SafeCommand(c -> {
                var player = EntityArgument.getPlayer(c, "player"); var selected = AnimationNetworking.selection(player);
                return output(c.getSource(), player.getGameProfile().name() + ": " + selected.profile() + " (" + selected.source() + ")");
            }))))
            .then(Commands.literal("default").then(profile().executes(new SafeCommand(c -> {
                String id = StringArgumentType.getString(c, "profile"); if (!exists(c.getSource(), id)) return 0;
                var old = AnimationCatalog.current().settings(); return save(c.getSource(), new AnimationSettings(id, old.rules(), old.overrides()));
            }))))
            .then(Commands.literal("set").then(Commands.argument("players", EntityArgument.players()).then(profile().executes(new SafeCommand(c -> {
                String id = StringArgumentType.getString(c, "profile"); if (!exists(c.getSource(), id)) return 0;
                return save(c.getSource(), AnimationCatalog.current().settings().assign(EntityArgument.getPlayers(c, "players").stream().map(p -> p.getUUID()).toList(), id));
            })))))
            .then(Commands.literal("reset").then(Commands.argument("players", EntityArgument.players()).executes(new SafeCommand(c ->
                save(c.getSource(), AnimationCatalog.current().settings().assign(EntityArgument.getPlayers(c, "players").stream().map(p -> p.getUUID()).toList(), null))))))
            .then(Commands.literal("force").then(Commands.literal("off").executes(new SafeCommand(c -> { AnimationNetworking.force(c.getSource().getServer(), null); return success(c.getSource()); })))
                .then(profile().executes(new SafeCommand(c -> {
                    String id = StringArgumentType.getString(c, "profile"); if (!exists(c.getSource(), id)) return 0;
                    AnimationNetworking.force(c.getSource().getServer(), id); return success(c.getSource());
                }))))
            .then(Commands.literal("reload").executes(new SafeCommand(c -> {
                var source = c.getSource(); if (!begin(source)) return 0;
                AnimationCatalog.load().whenComplete((snapshot, error) -> source.getServer().execute(() -> {
                    busy = false;
                    if (error != null) { failed(source, error); return; }
                    AnimationCatalog.publish(snapshot); AnimationNetworking.offerAll(source.getServer()); AnimationNetworking.refreshAll(source.getServer()); success(source);
                })); return 1;
            })))));
    }
    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> profile() {
        return Commands.argument("profile", StringArgumentType.greedyString()).suggests((c, b) -> {
            b.suggest("morphmod:vanilla"); b.suggest("morphmod:legacy_walk"); AnimationCatalog.current().profiles().keySet().stream().sorted().forEach(b::suggest); return b.buildFuture();
        });
    }
    private static int save(CommandSourceStack source, AnimationSettings settings) {
        if (!begin(source)) return 0;
        AnimationCatalog.save(settings).whenComplete((ignored, error) -> source.getServer().execute(() -> {
            busy = false;
            if (error != null) { failed(source, error); return; }
            AnimationCatalog.settings(settings); AnimationNetworking.refreshAll(source.getServer()); success(source);
        })); return 1;
    }
    private static boolean begin(CommandSourceStack source) {
        if (busy) { source.sendFailure(Component.translatable("commands.morphmod.animations.busy")); return false; } busy = true; return true;
    }
    private static void failed(CommandSourceStack source, Throwable error) { MorphMod.LOGGER.warn("Animation configuration rejected; retaining previous state", error); source.sendFailure(Component.translatable("commands.morphmod.animations.failed")); }
    private static boolean exists(CommandSourceStack source, String id) { if (AnimationCatalog.exists(id)) return true; source.sendFailure(Component.translatable("commands.morphmod.animations.unknown", id)); return false; }
    private static int success(CommandSourceStack source) { source.sendSuccess(() -> Component.translatable("commands.morphmod.animations.updated"), true); return 1; }
    private static int output(CommandSourceStack source, String value) { source.sendSuccess(() -> Component.translatable("commands.morphmod.animations.output", value), false); return 1; }
    private static boolean admin(CommandSourceStack source) {
        boolean fallback = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source);
        return source.getPlayer() == null ? fallback : CharacterPermissions.check(source.getPlayer(), "morphmod.animations.admin", fallback);
    }
    private AnimationCommands() { }
}
