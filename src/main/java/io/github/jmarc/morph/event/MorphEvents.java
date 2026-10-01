package io.github.jmarc.morph.event;

import io.github.jmarc.morph.MorphMod;
import io.github.jmarc.morph.morph.MorphManager;
import io.github.jmarc.morph.morph.Passive;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Player;

/**
 * Hooks the morph system into the game through Fabric API events.
 */
public final class MorphEvents {
	private MorphEvents() {
	}

	public static void init() {
		// Unlock a form when a player kills a mob.
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (!MorphMod.config().unlockOnKill || !(source.getEntity() instanceof ServerPlayer killer)) {
				return;
			}
			if (!(entity instanceof Player) && MorphManager.unlock(killer, entity.getType())) {
				killer.sendSystemMessage(Component.translatable("message.morphmod.unlocked", entity.getType().getDescription())
					.withStyle(ChatFormatting.LIGHT_PURPLE));
			}
		});

		// Passive damage immunities.
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (!(entity instanceof Player player)) {
				return true;
			}
			if (source.is(DamageTypeTags.IS_FIRE) && MorphManager.has(player, Passive.FIRE_IMMUNE)) {
				return false;
			}
			return !(source.is(DamageTypeTags.IS_FALL) && MorphManager.has(player, Passive.NO_FALL_DAMAGE));
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				MorphManager.tickPassives(player);
			}
		});

		ServerPlayerEvents.JOIN.register(player -> {
			if (!MorphMod.config().requireUnlock) {
				MorphManager.unlockAll(player);
			}
			MorphManager.resync(player);
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> MorphManager.resync(newPlayer));
		ServerPlayerEvents.LEAVE.register(MorphManager::forget);
	}
}
