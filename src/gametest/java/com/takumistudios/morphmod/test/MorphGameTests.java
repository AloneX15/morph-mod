package com.takumistudios.morphmod.test;

import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.ability.AbilitySlot;
import com.takumistudios.morphmod.compat.EntityTypes;
import com.takumistudios.morphmod.morph.MorphManager;
import com.takumistudios.morphmod.morph.MorphRegistry;
import com.takumistudios.morphmod.morph.Passive;
import com.takumistudios.morphmod.data.MorphAttachments;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import com.takumistudios.morphmod.ability.impl.TeleportAbility;

/** Dedicated-server regressions, also run with Lithium and FerriteCore. */
public class MorphGameTests {
	@GameTest public void targetedAbilitiesAndProjectiles(GameTestHelper helper) {
		ServerPlayer player = TestPlayers.create(helper);
		Vec3 origin = helper.absoluteVec(new Vec3(2, 2, 2));
		player.teleportTo(origin.x, origin.y, origin.z);
		player.setYRot(0);
		player.setXRot(0);
		LivingEntity target = (LivingEntity) EntityTypes.WARDEN.create(helper.getLevel(), EntitySpawnReason.COMMAND);
		target.setPos(origin.x, origin.y, origin.z + 3);
		helper.getLevel().addFreshEntity(target);
		try {
			MorphManager.morph(player, EntityTypes.IRON_GOLEM);
			float health = target.getHealth();
			MorphManager.useAbility(player, AbilitySlot.PRIMARY);
			helper.assertTrue(target.getHealth() < health, "fling damages the server-selected target");
			MorphManager.morph(player, EntityTypes.WARDEN);
			MorphManager.useAbility(player, AbilitySlot.SECONDARY);
			helper.assertTrue(target.hasEffect(MobEffects.DARKNESS), "darkness affects nearby target");
			target.discard();
			target = (LivingEntity) EntityTypes.WARDEN.create(helper.getLevel(), EntitySpawnReason.COMMAND);
			target.setPos(origin.x, origin.y, origin.z + 3);
			helper.getLevel().addFreshEntity(target);
			health = target.getHealth();
			MorphManager.useAbility(player, AbilitySlot.PRIMARY);
			helper.assertTrue(target.getHealth() < health, "sonic boom damages target");
			for (EntityType<?> type : List.of(EntityTypes.SKELETON, EntityTypes.BLAZE, EntityTypes.GHAST)) {
				MorphManager.morph(player, type);
				var bounds = player.getBoundingBox().inflate(6);
				int before = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class, bounds).size();
				MorphManager.useAbility(player, AbilitySlot.PRIMARY);
				int after = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class, bounds).size();
				helper.assertTrue(after - before == (type == EntityTypes.BLAZE ? 3 : 1), "expected projectiles spawned");
			}
		} finally { target.discard(); MorphManager.demorph(player); MorphManager.forget(player); }
		helper.succeed();
	}
	@GameTest public void teleportRejectsBlockedDestination(GameTestHelper helper) {
		ServerPlayer player = TestPlayers.create(helper);
		Vec3 origin = helper.absoluteVec(new Vec3(2, 2, 2));
		player.teleportTo(origin.x, origin.y, origin.z);
		MorphManager.morph(player, EntityTypes.ENDERMAN);
		BlockPos floor = BlockPos.containing(origin.x, origin.y - 1, origin.z + 5);
		helper.getLevel().setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
		Vec3 aim = Vec3.atCenterOf(floor).subtract(player.getEyePosition());
		player.setYRot((float) Math.toDegrees(Math.atan2(-aim.x, aim.z)));
		player.setXRot((float) -Math.toDegrees(Math.atan2(aim.y, Math.hypot(aim.x, aim.z))));
		try {
			helper.assertTrue(new TeleportAbility().activate(player), "clear destination accepted");
			BlockPos destination = BlockPos.containing(player.position());
			player.teleportTo(origin.x, origin.y, origin.z);
			// The upper obstruction is above the ray but inside the tall Enderman hitbox.
			helper.getLevel().setBlockAndUpdate(destination.above(2), Blocks.STONE.defaultBlockState());
			helper.assertTrue(!TeleportAbility.isSafeDestination(player, destination), "blocked destination rejected");
			player.setXRot(-90);
			helper.assertTrue(!new TeleportAbility().activate(player), "sky without target rejected");
			helper.assertTrue(player.position().distanceToSqr(origin) < 0.01, "rejected teleport does not move player");
		} finally { MorphManager.demorph(player); MorphManager.forget(player); }
		helper.succeed();
	}
	@GameTest public void formsRestoreAttributesAndDimensions(GameTestHelper helper) {
		ServerPlayer player = TestPlayers.create(helper);
		player.setGameMode(GameType.CREATIVE);
		// Vanilla's mock overrides gameMode() to CREATIVE, regardless of setGameMode.
		boolean originalFlight = player.getAbilities().mayfly;
		long start = System.nanoTime();
		try {
			for (EntityType<?> type : List.of(EntityTypes.WARDEN, EntityTypes.CREEPER, EntityTypes.ENDERMAN,
				EntityTypes.BLAZE, EntityTypes.SPIDER, EntityTypes.BAT, EntityTypes.IRON_GOLEM,
				EntityTypes.SKELETON, EntityTypes.GHAST, EntityTypes.AXOLOTL)) {
				helper.assertTrue(MorphManager.morph(player, type), "known form accepted");
				helper.assertTrue(MorphManager.current(player).orElseThrow().type() == type, "attachment matches");
				helper.assertTrue(Math.abs(player.getBbHeight() - type.getDimensions().height()) < 0.01F, "dimensions match");
				MorphManager.demorph(player);
				helper.assertTrue(MorphManager.current(player).isEmpty(), "attachment removed");
				helper.assertTrue(player.getMaxHealth() == 20 && player.getAttributeValue(Attributes.ATTACK_DAMAGE) == 1, "vanilla stats restored");
				helper.assertTrue(player.getAbilities().mayfly == originalFlight, "original flight permission restored");
			}
			long millis = (System.nanoTime() - start) / 1_000_000;
			MorphMod.LOGGER.info("Morph/demorph budget: {} ms for 10 forms", millis);
			helper.assertTrue(millis < 2000, "morph operation budget exceeded");
		} finally { MorphManager.forget(player); }
		helper.succeed();
	}
	@GameTest public void unlocksRejectInvalidTypesAndSurviveResync(GameTestHelper helper) {
		ServerPlayer player = TestPlayers.create(helper);
		try {
			helper.assertTrue(!MorphManager.unlock(player, EntityTypes.PLAYER), "cannot unlock players");
			helper.assertTrue(MorphRegistry.get(Identifier.fromNamespaceAndPath("minecraft", "missing")).isEmpty(), "unknown registry ID rejected");
			helper.assertTrue(MorphManager.unlock(player, EntityTypes.BAT), "new unlock");
			helper.assertTrue(!MorphManager.unlock(player, EntityTypes.BAT), "duplicate ignored");
			MorphManager.morph(player, EntityTypes.BAT);
			MorphManager.resync(player);
			helper.assertTrue(player.getAbilities().mayfly, "flight resynced");
			player.removeAttached(MorphAttachments.CURRENT_MORPH);
			MorphManager.resync(player);
			helper.assertTrue(player.getMaxHealth() == 20, "orphaned stats removed");
			player.setAttached(MorphAttachments.CURRENT_MORPH, Identifier.fromNamespaceAndPath("minecraft", "missing"));
			MorphManager.resync(player);
			helper.assertTrue(MorphManager.current(player).isEmpty(), "unknown saved morph is removed instead of resolving the registry default");
			MorphManager.resetUnlocks(player);
			helper.assertTrue(MorphManager.unlocked(player).isEmpty(), "reset removes unlocks");
			MorphManager.unlockAll(player);
			helper.assertTrue(MorphManager.unlocked(player).equals(MorphRegistry.allMorphableIds()), "unlock all is complete");
		} finally { MorphManager.forget(player); }
		helper.succeed();
	}
	@GameTest public void passivesAndAbilityCooldowns(GameTestHelper helper) {
		ServerPlayer player = TestPlayers.create(helper);
		player.setGameMode(GameType.SURVIVAL);
		try {
			MorphManager.morph(player, EntityTypes.BLAZE);
			player.igniteForSeconds(5);
			MorphManager.tickPassives(player);
			helper.assertTrue(!player.isOnFire(), "fire immunity clears fire");
			helper.assertTrue(player.hasEffect(MobEffects.SLOW_FALLING), "slow falling applied");
			MorphManager.morph(player, EntityTypes.BAT);
			MorphManager.tickPassives(player);
			helper.assertTrue(player.hasEffect(MobEffects.NIGHT_VISION), "night vision applied");
			MorphManager.morph(player, EntityTypes.AXOLOTL);
			player.setAirSupply(0);
			MorphManager.tickPassives(player);
			helper.assertTrue(player.getAirSupply() == player.getMaxAirSupply(), "water breathing restores air");
			MorphManager.useAbility(player, AbilitySlot.PRIMARY);
			helper.assertTrue(player.hasEffect(MobEffects.REGENERATION), "play dead activates");
			player.removeEffect(MobEffects.REGENERATION);
			MorphManager.useAbility(player, AbilitySlot.PRIMARY);
			helper.assertTrue(!player.hasEffect(MobEffects.REGENERATION), "cooldown blocks second activation");
			MorphManager.morph(player, EntityTypes.WARDEN);
			helper.assertTrue(MorphManager.has(player, Passive.VIBRATION_SENSE), "warden vibration sense available");
			MorphManager.useAbility(player, AbilitySlot.PRIMARY);
			MorphManager.useAbility(player, AbilitySlot.SECONDARY);
			MorphManager.morph(player, EntityTypes.SKELETON);
			MorphManager.useAbility(player, AbilitySlot.PRIMARY);
			MorphManager.morph(player, EntityTypes.BLAZE);
			MorphManager.useAbility(player, AbilitySlot.PRIMARY);
			MorphManager.morph(player, EntityTypes.GHAST);
			MorphManager.useAbility(player, AbilitySlot.PRIMARY);
			MorphManager.morph(player, EntityTypes.CREEPER);
			float health = player.getHealth();
			MorphManager.useAbility(player, AbilitySlot.PRIMARY);
			helper.assertTrue(player.getHealth() == health, "creeper explosion spares owner");
			MorphManager.demorph(player);
		} finally { MorphManager.forget(player); }
		helper.succeed();
	}
}
