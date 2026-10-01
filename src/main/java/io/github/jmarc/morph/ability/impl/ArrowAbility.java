package io.github.jmarc.morph.ability.impl;

import io.github.jmarc.morph.ability.MorphAbility;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Skeleton: shoots an arrow without needing a bow or ammo. The arrow cannot be picked up.
 */
public final class ArrowAbility implements MorphAbility {
	@Override
	public String id() {
		return "shoot_arrow";
	}

	@Override
	public int cooldownTicks() {
		return 20;
	}

	@Override
	public boolean activate(ServerPlayer player) {
		ServerLevel level = player.level();
		Arrow arrow = new Arrow(level, player, new ItemStack(Items.ARROW), null);
		arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
		arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);
		level.addFreshEntity(arrow);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SKELETON_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
		return true;
	}
}
