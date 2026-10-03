package com.takumistudios.morphmod.ability.impl;

import com.takumistudios.morphmod.ability.MorphAbility;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Warden: roars and spreads Darkness to every creature nearby.
 */
public final class DarknessPulseAbility implements MorphAbility {
	private static final double RADIUS = 20.0;
	private static final int DURATION = 12 * 20;

	@Override
	public String id() {
		return "darkness_pulse";
	}

	@Override
	public int cooldownTicks() {
		return 15 * 20;
	}

	@Override
	public boolean activate(ServerPlayer player) {
		ServerLevel level = player.level();
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 3.0F, 1.0F);
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RADIUS), e -> e != player)) {
			target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DURATION, 0, false, false), player);
		}
		return true;
	}
}
