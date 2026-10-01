package io.github.jmarc.morph.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * An active power bound to a key (R / G). Implementations run on the server only.
 */
public interface MorphAbility {
	/** Translation key suffix, e.g. {@code sonic_boom} → {@code ability.morphmod.sonic_boom}. */
	String id();

	/** Base cooldown in ticks, before the config multiplier. */
	int cooldownTicks();

	/**
	 * Performs the ability.
	 *
	 * @return true if it fired and the cooldown should start
	 */
	boolean activate(ServerPlayer player);

	default Component displayName() {
		return Component.translatable("ability.morphmod." + id());
	}
}
