package io.github.jmarc.morph.ability.impl;

import io.github.jmarc.morph.ability.MorphAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Axolotl: plays dead, regenerating health quickly while barely moving.
 */
public final class PlayDeadAbility implements MorphAbility {
	@Override
	public String id() {
		return "play_dead";
	}

	@Override
	public int cooldownTicks() {
		return 60 * 20;
	}

	@Override
	public boolean activate(ServerPlayer player) {
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 1));
		player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 10 * 20, 3));
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AXOLOTL_HURT, SoundSource.PLAYERS, 1.0F, 1.0F);
		return true;
	}
}
