package com.takumistudios.morphmod.ability.impl;

import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.ability.MorphAbility;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;

/**
 * Creeper: an explosion centred on the player that never hurts the player themselves.
 */
public final class ExplodeAbility implements MorphAbility {
	private static final float POWER = 3.0F;

	@Override
	public String id() {
		return "explode";
	}

	@Override
	public int cooldownTicks() {
		return 10 * 20;
	}

	@Override
	public boolean activate(ServerPlayer player) {
		ServerLevel level = player.level();
		ExplosionDamageCalculator sparePlayer = new ExplosionDamageCalculator() {
			@Override
			public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
				return entity != player && super.shouldDamageEntity(explosion, entity);
			}

			@Override
			public float getKnockbackMultiplier(Entity entity) {
				return entity == player ? 0.0F : super.getKnockbackMultiplier(entity);
			}
		};
		Level.ExplosionInteraction interaction = MorphMod.config().abilitiesBreakBlocks || MorphMod.config().creeperBreaksBlocks
			? Level.ExplosionInteraction.MOB
			: Level.ExplosionInteraction.NONE;
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CREEPER_PRIMED, SoundSource.PLAYERS, 1.0F, 0.5F);
		level.explode(player, null, sparePlayer, player.getX(), player.getY(), player.getZ(), POWER, false, interaction);
		return true;
	}
}
