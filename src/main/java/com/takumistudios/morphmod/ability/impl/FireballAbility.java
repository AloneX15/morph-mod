package com.takumistudios.morphmod.ability.impl;

import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.ability.MorphAbility;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

/**
 * Blaze (three small fireballs) and Ghast (one explosive fireball).
 */
public final class FireballAbility implements MorphAbility {
	private final boolean large;

	private FireballAbility(boolean large) {
		this.large = large;
	}

	public static FireballAbility blaze() {
		return new FireballAbility(false);
	}

	public static FireballAbility ghast() {
		return new FireballAbility(true);
	}

	@Override
	public String id() {
		return large ? "ghast_fireball" : "blaze_fireballs";
	}

	@Override
	public int cooldownTicks() {
		return large ? 3 * 20 : 2 * 20;
	}

	@Override
	public boolean activate(ServerPlayer player) {
		ServerLevel level = player.level();
		Vec3 dir = player.getLookAngle();
		Vec3 spawn = player.getEyePosition().add(dir.scale(1.5));

		if (large) {
			int power = MorphMod.config().abilitiesBreakBlocks ? 1 : 0;
			LargeFireball fireball = new LargeFireball(level, player, dir, power);
			fireball.setPos(spawn);
			level.addFreshEntity(fireball);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 3.0F, 1.0F);
		} else {
			RandomSource random = level.getRandom();
			for (int i = 0; i < 3; i++) {
				Vec3 spread = dir.add(random.triangle(0.0, 0.1), random.triangle(0.0, 0.1), random.triangle(0.0, 0.1));
				SmallFireball fireball = new SmallFireball(level, player, spread.normalize());
				fireball.setPos(spawn);
				level.addFreshEntity(fireball);
			}
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		return true;
	}
}
