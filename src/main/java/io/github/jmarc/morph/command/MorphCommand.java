package io.github.jmarc.morph.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.jmarc.morph.morph.MorphManager;
import io.github.jmarc.morph.morph.MorphRegistry;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

/**
 * {@code /morph ...} admin and testing commands, plus {@code /morph list} for everyone.
 */
public final class MorphCommand {
	private MorphCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(Commands.literal("morph")
			.then(Commands.literal("list").executes(c -> list(c.getSource(), c.getSource().getPlayerOrException())))
			.then(Commands.literal("into")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.argument("entity", ResourceArgument.resource(context, Registries.ENTITY_TYPE))
					.suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES))
					.executes(c -> into(c.getSource(), List.of(c.getSource().getPlayerOrException()), entity(c)))
					.then(Commands.argument("targets", EntityArgument.players())
						.executes(c -> into(c.getSource(), EntityArgument.getPlayers(c, "targets"), entity(c))))))
			.then(Commands.literal("clear")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.executes(c -> clear(c.getSource(), List.of(c.getSource().getPlayerOrException())))
				.then(Commands.argument("targets", EntityArgument.players())
					.executes(c -> clear(c.getSource(), EntityArgument.getPlayers(c, "targets")))))
			.then(Commands.literal("unlock")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("all")
					.executes(c -> unlockAll(c.getSource(), List.of(c.getSource().getPlayerOrException())))
					.then(Commands.argument("targets", EntityArgument.players())
						.executes(c -> unlockAll(c.getSource(), EntityArgument.getPlayers(c, "targets")))))
				.then(Commands.argument("entity", ResourceArgument.resource(context, Registries.ENTITY_TYPE))
					.suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES))
					.executes(c -> unlock(c.getSource(), List.of(c.getSource().getPlayerOrException()), entity(c)))
					.then(Commands.argument("targets", EntityArgument.players())
						.executes(c -> unlock(c.getSource(), EntityArgument.getPlayers(c, "targets"), entity(c))))))
			.then(Commands.literal("reset")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.argument("targets", EntityArgument.players())
					.executes(c -> reset(c.getSource(), EntityArgument.getPlayers(c, "targets"))))));

		dispatcher.register(Commands.literal("demorph")
			.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
			.executes(c -> clear(c.getSource(), List.of(c.getSource().getPlayerOrException()))));
	}

	private static EntityType<?> entity(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		return ResourceArgument.getSummonableEntityType(context, "entity").value();
	}

	private static int into(CommandSourceStack source, Collection<ServerPlayer> targets, EntityType<?> type) {
		if (!MorphRegistry.isMorphable(type)) {
			source.sendFailure(Component.translatable("commands.morphmod.not_morphable", type.getDescription()));
			return 0;
		}
		int count = 0;
		for (ServerPlayer player : targets) {
			if (MorphManager.morph(player, type)) {
				count++;
			}
		}
		int done = count;
		source.sendSuccess(() -> Component.translatable("commands.morphmod.into", done, type.getDescription()), true);
		return count;
	}

	private static int clear(CommandSourceStack source, Collection<ServerPlayer> targets) {
		targets.forEach(MorphManager::demorph);
		source.sendSuccess(() -> Component.translatable("commands.morphmod.clear", targets.size()), true);
		return targets.size();
	}

	private static int unlock(CommandSourceStack source, Collection<ServerPlayer> targets, EntityType<?> type) {
		if (!MorphRegistry.isMorphable(type)) {
			source.sendFailure(Component.translatable("commands.morphmod.not_morphable", type.getDescription()));
			return 0;
		}
		targets.forEach(player -> MorphManager.unlock(player, type));
		source.sendSuccess(() -> Component.translatable("commands.morphmod.unlock", type.getDescription(), targets.size()), true);
		return targets.size();
	}

	private static int unlockAll(CommandSourceStack source, Collection<ServerPlayer> targets) {
		targets.forEach(MorphManager::unlockAll);
		source.sendSuccess(() -> Component.translatable("commands.morphmod.unlock_all", targets.size()), true);
		return targets.size();
	}

	private static int reset(CommandSourceStack source, Collection<ServerPlayer> targets) {
		for (ServerPlayer player : targets) {
			MorphManager.demorph(player);
			MorphManager.resetUnlocks(player);
		}
		source.sendSuccess(() -> Component.translatable("commands.morphmod.reset", targets.size()), true);
		return targets.size();
	}

	private static int list(CommandSourceStack source, ServerPlayer player) {
		List<Identifier> unlocked = MorphManager.unlocked(player);
		if (unlocked.isEmpty()) {
			source.sendSuccess(() -> Component.translatable("commands.morphmod.list.empty"), false);
			return 0;
		}
		String names = unlocked.stream().map(Identifier::toString).collect(Collectors.joining(", "));
		source.sendSuccess(() -> Component.translatable("commands.morphmod.list", unlocked.size(), names), false);
		return unlocked.size();
	}
}
