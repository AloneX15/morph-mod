package com.takumistudios.morphmod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.takumistudios.morphmod.MorphMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

/** Keep normal Brigadier validation; contain runtime failures per command action. */
final class SafeCommand implements Command<CommandSourceStack> {
	private final Command<CommandSourceStack> delegate;
	private boolean enabled = true;
	SafeCommand(Command<CommandSourceStack> delegate) { this.delegate = delegate; }
	@Override public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		if (enabled) {
			try { return delegate.run(context); }
			catch (RuntimeException | LinkageError e) {
				enabled = false;
				MorphMod.LOGGER.error("Morph command action disabled after a failure", e);
			}
		}
		context.getSource().sendFailure(Component.translatable("commands.morphmod.disabled"));
		return 0;
	}
}
