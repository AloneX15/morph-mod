package com.takumistudios.morphmod.client.animation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.takumistudios.morphmod.MorphMod;
import java.util.concurrent.CompletionException;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;

/** /morphpreview: try a profile folder on your own player, without the server or other players. */
public final class AnimationPreviewCommand {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registries) -> dispatcher.register(literal("morphpreview")
            .then(literal("load").then(argument("folder", StringArgumentType.word()).executes(c -> load(c, StringArgumentType.getString(c, "folder")))))
            .then(literal("reload").executes(c -> {
                String name = ClientAnimationProfiles.previewName();
                if (name == null) { c.getSource().sendError(Component.translatable("commands.morphmod.preview.none")); return 0; }
                return load(c, name);
            }))
            .then(literal("off").executes(c -> {
                ClientAnimationProfiles.clearPreview();
                c.getSource().sendFeedback(Component.translatable("commands.morphmod.preview.off")); return 1;
            }))));
    }
    private static int load(CommandContext<FabricClientCommandSource> context, String name) {
        FabricClientCommandSource source = context.getSource();
        ClientAnimationProfiles.loadPreview(name).whenCompleteAsync((profile, error) -> {
            if (error != null) {
                Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
                MorphMod.LOGGER.warn("Animation preview {} rejected", name, cause);
                source.sendError(Component.translatable("commands.morphmod.preview.failed", name, String.valueOf(cause.getMessage())));
                return;
            }
            source.sendFeedback(Component.translatable("commands.morphmod.preview.loaded", profile.name(), profile.id()));
            if (!profile.warnings().isEmpty()) source.sendFeedback(Component.translatable("commands.morphmod.preview.warnings", String.join("\n", profile.warnings())));
        }, Minecraft.getInstance());
        return 1;
    }
    private AnimationPreviewCommand() { }
}
