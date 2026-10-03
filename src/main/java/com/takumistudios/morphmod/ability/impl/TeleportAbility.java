package com.takumistudios.morphmod.ability.impl;

import com.takumistudios.morphmod.ability.MorphAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Enderman: teleports onto the block the player is looking at (up to 32 blocks).
 */
public final class TeleportAbility implements MorphAbility {
	private static final double RANGE = 32.0;

	@Override
	public String id() {
		return "teleport";
	}

	@Override
	public int cooldownTicks() {
		return 3 * 20;
	}

	@Override
	public boolean activate(ServerPlayer player) {
		HitResult hit = player.pick(RANGE, 1.0F, false);
		if (hit.getType() != HitResult.Type.BLOCK || !(hit instanceof BlockHitResult blockHit)) {
			return false;
		}
		ServerLevel level = player.level();
		BlockPos target = blockHit.getBlockPos().relative(blockHit.getDirection());
		double x = target.getX() + 0.5;
		double y = target.getY();
		double z = target.getZ() + 0.5;
		if (!isSafeDestination(player, target)) {
			return false;
		}

		level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 32, 0.5, 1.0, 0.5, 0.1);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
		player.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
		player.resetFallDistance();
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
		return true;
	}

	/** Shared server-side predicate; the client never supplies teleport coordinates. */
	public static boolean isSafeDestination(ServerPlayer player, BlockPos target) {
		ServerLevel level = player.level();
		return level.getChunkSource().hasChunk(target.getX() >> 4, target.getZ() >> 4)
			&& level.getWorldBorder().isWithinBounds(target)
			&& target.getY() >= level.getMinY() && target.getY() + player.getBbHeight() < level.getMaxY()
			&& level.noCollision(player, player.getBoundingBox().move(target.getX() + 0.5 - player.getX(),
				target.getY() - player.getY(), target.getZ() + 0.5 - player.getZ()));
	}
}
