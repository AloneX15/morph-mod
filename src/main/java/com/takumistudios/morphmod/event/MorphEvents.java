package com.takumistudios.morphmod.event;

import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.morph.MorphManager;
import com.takumistudios.morphmod.morph.Passive;
import com.takumistudios.morphmod.util.FeatureGuard;
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
		FeatureGuard unlock = new FeatureGuard("kill unlock");
		FeatureGuard damage = new FeatureGuard("damage immunities");
		FeatureGuard passives = new FeatureGuard("passive tick");
		FeatureGuard join = new FeatureGuard("join resync");
		FeatureGuard respawn = new FeatureGuard("respawn resync");
		// Unlock a form when a player kills a mob.
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> unlock.run(() -> {
			if (!MorphMod.config().unlockOnKill || !(source.getEntity() instanceof ServerPlayer killer)) {
				return;
			}
			if (!(entity instanceof Player) && MorphManager.unlock(killer, entity.getType())) {
				killer.sendSystemMessage(Component.translatable("message.morphmod.unlocked", entity.getType().getDescription())
					.withStyle(ChatFormatting.LIGHT_PURPLE));
			}
		}));

		// Passive damage immunities.
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> damage.get(() -> {
			if (!(entity instanceof Player player)) {
				return true;
			}
			if (source.is(DamageTypeTags.IS_FIRE) && MorphManager.has(player, Passive.FIRE_IMMUNE)) {
				return false;
			}
			return !(source.is(DamageTypeTags.IS_FALL) && MorphManager.has(player, Passive.NO_FALL_DAMAGE));
		}, true));

		ServerTickEvents.END_SERVER_TICK.register(server -> passives.run(() -> { MorphManager.tickActive(server); com.takumistudios.morphmod.character.CharacterManager.tick(server); }));

		ServerPlayerEvents.JOIN.register(player -> join.run(() -> {
			if (!MorphMod.config().requireUnlock) {
				MorphManager.unlockAll(player);
			}
			MorphManager.resync(player);
			com.takumistudios.morphmod.character.CharacterManager.join(player);
		}));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> respawn.run(() -> MorphManager.resync(newPlayer)));
		ServerPlayerEvents.LEAVE.register(MorphManager::forget);
		ServerPlayerEvents.LEAVE.register(com.takumistudios.morphmod.character.CharacterManager::forget);
	}
}
