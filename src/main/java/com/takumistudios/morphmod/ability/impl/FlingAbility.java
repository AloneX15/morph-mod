package com.takumistudios.morphmod.ability.impl;

import com.takumistudios.morphmod.ability.MorphAbility;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Iron Golem: hits the creature in front of the player and launches it into the air.
 */
public final class FlingAbility implements MorphAbility {
	private static final double RANGE = 5.0;

	@Override
	public String id() {
		return "fling";
	}

	@Override
	public int cooldownTicks() {
		return 4 * 20;
	}

	@Override
	public boolean activate(ServerPlayer player) {
		HitResult hit = ProjectileUtil.getHitResultOnViewVector(player, e -> e instanceof LivingEntity && e.isAlive() && e != player, RANGE);
		if (!(hit instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof LivingEntity target)) {
			return false;
		}
		ServerLevel level = player.level();
		float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
		target.hurtServer(level, level.damageSources().playerAttack(player), damage);
		double resistance = 1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		target.push(0, resistance, 0);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 1.0F, 1.0F);
		return true;
	}
}
