package com.takumistudios.morphmod.client.render;

import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.client.mixin.WalkAnimationStateAccessor;
import com.takumistudios.morphmod.compat.PlayerAnimationState;
import com.takumistudios.morphmod.data.MorphAttachments;
import com.takumistudios.morphmod.morph.MorphDefinition;
import com.takumistudios.morphmod.morph.MorphManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Keeps one client-only "disguise" mob per morphed player. The mob is never added to the world:
 * it only mirrors the player's position, rotations and animations so the mob renderer can draw it.
 */
public final class DisguiseManager {
	private static final Map<UUID, LivingEntity> DISGUISES = new HashMap<>();
	private static final Map<UUID, Identifier> KNOWN_MORPHS = new HashMap<>();
	/** Last observed swing of each player, so the disguise starts its attack animation once per swing. */
	private static final Map<UUID, Object> LAST_SWINGS = new HashMap<>();
	/** Mob types whose tick() threw on a detached client entity; they are rendered without ticking. */
	private static final Set<EntityType<?>> NO_TICK = new HashSet<>();
	private static final Set<EntityType<?>> NO_CREATE = new HashSet<>();
	private static final Set<UUID> SEEN = new HashSet<>();
	private static int nextId = -1;

	private DisguiseManager() {
	}

	/** The disguise to render instead of {@code player}, or null if they are not morphed. */
	public static @Nullable LivingEntity disguiseFor(AbstractClientPlayer player) {
		if (!com.takumistudios.morphmod.character.CharacterManager.selected(player).isEmpty()) return com.takumistudios.morphmod.client.character.CharacterRenderManager.proxy(player);
		LivingEntity disguise = DISGUISES.get(player.getUUID());
		if (disguise == null) {
			return null;
		}
		copyState(player, disguise);
		return disguise;
	}

	/** Called every client tick: creates/drops disguises and ticks their animations. */
	public static void tick(Minecraft minecraft) {
		ClientLevel level = minecraft.level;
		if (level == null) {
			clear();
			return;
		}
		Set<UUID> seen = SEEN;
		seen.clear();
		for (AbstractClientPlayer player : level.players()) {
			UUID id = player.getUUID();
			seen.add(id);
			Identifier morph = player.getAttached(MorphAttachments.CURRENT_MORPH);

			// Hitbox follows the synced morph on this side too.
			if (!Objects.equals(KNOWN_MORPHS.get(id), morph)) {
				KNOWN_MORPHS.put(id, morph);
				player.refreshDimensions();
				DISGUISES.remove(id);
			}

			LivingEntity disguise = DISGUISES.get(id);
			if (morph == null) {
				continue;
			}
			if (disguise == null || disguise.level() != level) {
				disguise = MorphManager.current(player).map(def -> create(def, level)).orElse(null);
				if (disguise == null) {
					continue;
				}
				DISGUISES.put(id, disguise);
			}
			tickDisguise(player, disguise);
		}
		DISGUISES.keySet().retainAll(seen);
		KNOWN_MORPHS.keySet().retainAll(seen);
		LAST_SWINGS.keySet().retainAll(seen);
	}

	public static void clear() {
		DISGUISES.clear();
		KNOWN_MORPHS.clear();
		LAST_SWINGS.clear();
		NO_TICK.clear();
		NO_CREATE.clear();
		SEEN.clear();
		nextId = -1;
	}

	/** Creates a detached mob for menus and previews. */
	public static @Nullable LivingEntity create(MorphDefinition definition, ClientLevel level) {
		if (NO_CREATE.contains(definition.type())) return null;
		try {
		Entity entity = definition.type().create(level, EntitySpawnReason.LOAD);
		if (!(entity instanceof LivingEntity living)) {
			return null;
		}
		// Renderers read the id (item model seeds); negative ids never collide with real entities.
		living.setId(nextId--);
		living.setSilent(true);
		living.setNoGravity(true);
		if (living instanceof Bat bat) {
			bat.setResting(false);
		}
		return living;
		} catch (RuntimeException | LinkageError e) {
			NO_CREATE.add(definition.type());
			MorphMod.LOGGER.warn("Disguise {} disabled after a creation failure", definition.type(), e);
			return null;
		}
	}

	private static void tickDisguise(AbstractClientPlayer player, LivingEntity disguise) {
		copyState(player, disguise);
		copyAttack(player, disguise);
		if (NO_TICK.contains(disguise.getType())) {
			disguise.tickCount = player.tickCount;
			return;
		}
		try {
			// Lets mobs advance their client-side animation states (bat wings, warden tendrils...).
			disguise.tick();
		} catch (RuntimeException e) {
			NO_TICK.add(disguise.getType());
			MorphMod.LOGGER.warn("Disguise {} cannot be ticked, rendering without animations", disguise.getType(), e);
		}
		copyState(player, disguise);
	}

	/** Mirrors the start of each player swing: arm swing for humanoids, attack event for golems, wardens... */
	private static void copyAttack(AbstractClientPlayer player, LivingEntity disguise) {
		Object swing = PlayerAnimationState.swingToken(player);
		Object previous = LAST_SWINGS.put(player.getUUID(), swing);
		if (!PlayerAnimationState.swingStarted(previous, swing)) {
			return;
		}
		try {
			PlayerAnimationState.copySwing(player, disguise);
			if (disguise instanceof IronGolem || disguise instanceof Warden || disguise instanceof Ravager
				|| disguise instanceof Hoglin || disguise instanceof Zoglin) {
				// Same entity event the server broadcasts when these mobs attack.
				disguise.handleEntityEvent((byte) 4);
			}
		} catch (RuntimeException e) {
			MorphMod.LOGGER.debug("Disguise {} cannot mirror the attack animation", disguise.getType(), e);
		}
	}

	private static void copyEquipment(AbstractClientPlayer player, LivingEntity disguise) {
		boolean show = MorphAttachments.showsEquipment(player);
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			ItemStack stack = show ? player.getItemBySlot(slot) : ItemStack.EMPTY;
			if (disguise.getItemBySlot(slot) != stack) {
				disguise.setItemSlot(slot, stack);
			}
		}
	}

	private static void copyState(AbstractClientPlayer player, LivingEntity disguise) {
		disguise.setPos(player.getX(), player.getY(), player.getZ());
		disguise.xo = player.xo;
		disguise.yo = player.yo;
		disguise.zo = player.zo;
		disguise.xOld = player.xOld;
		disguise.yOld = player.yOld;
		disguise.zOld = player.zOld;
		disguise.setYRot(player.getYRot());
		disguise.setXRot(player.getXRot());
		disguise.yRotO = player.yRotO;
		disguise.xRotO = player.xRotO;
		disguise.yBodyRot = player.yBodyRot;
		disguise.yBodyRotO = player.yBodyRotO;
		disguise.yHeadRot = player.yHeadRot;
		disguise.yHeadRotO = player.yHeadRotO;
		disguise.tickCount = player.tickCount;
		disguise.hurtTime = player.hurtTime;
		disguise.deathTime = player.deathTime;
		disguise.setOnGround(player.onGround());
		disguise.setShiftKeyDown(player.isShiftKeyDown());
		disguise.setInvisible(player.isInvisible());
		disguise.setRemainingFireTicks(player.getRemainingFireTicks());
		disguise.setCustomName(player == Minecraft.getInstance().player ? null : player.getDisplayName());
		copyEquipment(player, disguise);

		WalkAnimationStateAccessor from = (WalkAnimationStateAccessor) player.walkAnimation;
		WalkAnimationStateAccessor to = (WalkAnimationStateAccessor) disguise.walkAnimation;
		to.morphmod$setSpeedOld(from.morphmod$getSpeedOld());
		to.morphmod$setSpeed(from.morphmod$getSpeed());
		to.morphmod$setPosition(from.morphmod$getPosition());
	}
}
