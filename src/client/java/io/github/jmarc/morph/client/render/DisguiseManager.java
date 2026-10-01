package io.github.jmarc.morph.client.render;

import io.github.jmarc.morph.MorphMod;
import io.github.jmarc.morph.client.mixin.WalkAnimationStateAccessor;
import io.github.jmarc.morph.data.MorphAttachments;
import io.github.jmarc.morph.morph.MorphDefinition;
import io.github.jmarc.morph.morph.MorphManager;
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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import org.jspecify.annotations.Nullable;

/**
 * Keeps one client-only "disguise" mob per morphed player. The mob is never added to the world:
 * it only mirrors the player's position, rotations and animations so the mob renderer can draw it.
 */
public final class DisguiseManager {
	private static final Map<UUID, LivingEntity> DISGUISES = new HashMap<>();
	private static final Map<UUID, Identifier> KNOWN_MORPHS = new HashMap<>();
	/** Mob types whose tick() threw on a detached client entity; they are rendered without ticking. */
	private static final Set<EntityType<?>> NO_TICK = new HashSet<>();
	private static int nextId = -1;

	private DisguiseManager() {
	}

	/** The disguise to render instead of {@code player}, or null if they are not morphed. */
	public static @Nullable LivingEntity disguiseFor(AbstractClientPlayer player) {
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
		Set<UUID> seen = new HashSet<>();
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
	}

	public static void clear() {
		DISGUISES.clear();
		KNOWN_MORPHS.clear();
	}

	/** Creates a detached mob for menus and previews. */
	public static @Nullable LivingEntity create(MorphDefinition definition, ClientLevel level) {
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
	}

	private static void tickDisguise(AbstractClientPlayer player, LivingEntity disguise) {
		copyState(player, disguise);
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

		WalkAnimationStateAccessor from = (WalkAnimationStateAccessor) player.walkAnimation;
		WalkAnimationStateAccessor to = (WalkAnimationStateAccessor) disguise.walkAnimation;
		to.morphmod$setSpeedOld(from.morphmod$getSpeedOld());
		to.morphmod$setSpeed(from.morphmod$getSpeed());
		to.morphmod$setPosition(from.morphmod$getPosition());
	}
}
