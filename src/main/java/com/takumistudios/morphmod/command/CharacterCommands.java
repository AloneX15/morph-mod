package com.takumistudios.morphmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.character.*;
import com.takumistudios.morphmod.morph.MorphManager;
import com.takumistudios.morphmod.network.CharacterNetworking;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

public final class CharacterCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("morph")
            .then(Commands.literal("character")
                .then(Commands.literal("list").executes(new SafeCommand(c -> {
                    var player = c.getSource().getPlayerOrException();
                    String names = CharacterCatalog.bundles().keySet().stream().sorted().filter(id -> CharacterManager.allowed(player, CharacterCatalog.get(id))).collect(java.util.stream.Collectors.joining(", "));
                    c.getSource().sendSuccess(() -> Component.translatable("commands.morphmod.characters.list", names), false); return 1;
                })))
                .then(Commands.literal("select").then(Commands.argument("id", StringArgumentType.word())
                    .suggests((c, b) -> { var player = c.getSource().getPlayerOrException(); CharacterCatalog.bundles().keySet().stream().filter(id -> CharacterManager.allowed(player, CharacterCatalog.get(id))).forEach(b::suggest); return b.buildFuture(); })
                    .executes(new SafeCommand(c -> result(c.getSource(), CharacterManager.select(c.getSource().getPlayerOrException(), StringArgumentType.getString(c, "id")))))))
                .then(Commands.literal("clear").executes(new SafeCommand(c -> { MorphManager.demorph(c.getSource().getPlayerOrException()); return 1; })))
                .then(Commands.literal("reload").requires(CharacterCommands::admin).executes(new SafeCommand(c -> {
                    var server = c.getSource().getServer();
                    CharacterCatalog.load().whenComplete((catalog, error) -> server.execute(() -> {
                        if (error != null) { MorphMod.LOGGER.warn("Character reload rejected; retaining previous catalog", error); c.getSource().sendFailure(Component.translatable("commands.morphmod.characters.reload_failed")); }
                        else { CharacterCatalog.publish(catalog); server.getPlayerList().getPlayers().forEach(CharacterNetworking::offer); c.getSource().sendSuccess(() -> Component.translatable("commands.morphmod.characters.reloaded", catalog.size()), true); }
                    })); return 1;
                })))
                .then(Commands.literal("unlock").requires(CharacterCommands::admin)
                    .then(Commands.argument("id", StringArgumentType.word()).suggests((c, b) -> { CharacterCatalog.bundles().keySet().forEach(b::suggest); return b.buildFuture(); })
                        .then(Commands.argument("targets", EntityArgument.players()).executes(new SafeCommand(c -> {
                            String id = StringArgumentType.getString(c, "id"); if (CharacterCatalog.get(id) == null) return result(c.getSource(), false);
                            var targets = EntityArgument.getPlayers(c, "targets"); targets.forEach(p -> CharacterManager.unlock(p, id)); return targets.size();
                        }))))))
            .then(Commands.literal("emote")
                .then(Commands.literal("list").executes(new SafeCommand(c -> {
                    var p = c.getSource().getPlayerOrException(); var d = CharacterCatalog.get(CharacterManager.selected(p));
                    if (d == null) return result(c.getSource(), false);
                    String names = d.emotes().keySet().stream().sorted().filter(id -> CharacterManager.allowedEmote(p, d, id)).collect(java.util.stream.Collectors.joining(", "));
                    c.getSource().sendSuccess(() -> Component.translatable("commands.morphmod.emotes.list", names), false); return 1;
                })))
                .then(Commands.literal("stop").executes(new SafeCommand(c -> { CharacterManager.stop(c.getSource().getPlayerOrException()); return 1; })))
                .then(Commands.literal("play").then(Commands.argument("id", StringArgumentType.word())
                    .suggests((c, b) -> { var p = c.getSource().getPlayerOrException(); var d = CharacterCatalog.get(CharacterManager.selected(p)); if (d != null) d.emotes().keySet().stream().filter(id -> CharacterManager.allowedEmote(p, d, id)).forEach(b::suggest); return b.buildFuture(); })
                    .executes(new SafeCommand(c -> result(c.getSource(), CharacterManager.play(c.getSource().getPlayerOrException(), StringArgumentType.getString(c, "id"), "full"))))
                    .then(Commands.argument("mode", StringArgumentType.word()).suggests((c, b) -> { b.suggest("full"); b.suggest("overlay"); return b.buildFuture(); })
                        .executes(new SafeCommand(c -> result(c.getSource(), CharacterManager.play(c.getSource().getPlayerOrException(), StringArgumentType.getString(c, "id"), StringArgumentType.getString(c, "mode"))))))))));
    }
    private static boolean admin(CommandSourceStack source) {
        var player = source.getPlayer(); boolean fallback = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source);
        return player == null ? fallback : CharacterPermissions.check(player, "morphmod.characters.admin", fallback);
    }
    private static int result(CommandSourceStack source, boolean success) { if (!success) source.sendFailure(Component.translatable("commands.morphmod.characters.denied")); return success ? 1 : 0; }
    private CharacterCommands() { }
}
