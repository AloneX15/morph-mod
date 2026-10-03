package com.takumistudios.morphmod.ability.impl;

import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.ability.MorphAbility;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Warden: a sonic beam that pierces every creature in a line, ignoring armor (vanilla sonic_boom damage type).
 * Mirrors {@code net.minecraft.world.entity.ai.behavior.warden.SonicBoom}.
 */
public final class SonicBoomAbility implements MorphAbility {
	private static final int RANGE = 15;
	private static final double BEAM_RADIUS = 1.25;
	private static final float DAMAGE = 10.0F;

	@Override
	public String id() {
		return "sonic_boom";
	}

	@Override
	public int cooldownTicks() {
		return 60;
	}

	@Override
	public boolean activate(ServerPlayer player) {
		ServerLevel level = player.level();
		Vec3 origin = player.getEyePosition().subtract(0, 0.4, 0);
		Vec3 dir = player.getLookAngle();

		Set<LivingEntity> hit = new LinkedHashSet<>();
		for (int i = 1; i <= RANGE; i++) {
			Vec3 point = origin.add(dir.scale(i));
			level.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0, 0, 0, 0);
			AABB box = new AABB(point, point).inflate(BEAM_RADIUS);
			hit.addAll(level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive()));
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3.0F, 1.0F);
		float damage = (float) (DAMAGE * MorphMod.config().damageMultiplier);
		for (LivingEntity target : hit) {
			if (target.hurtServer(level, level.damageSources().sonicBoom(player), damage)) {
				double resistance = 1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
				target.push(dir.x * 2.5 * resistance, dir.y * 0.5 * resistance, dir.z * 2.5 * resistance);
			}
		}
		return true;
	}
}
