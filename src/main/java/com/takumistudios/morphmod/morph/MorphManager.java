package com.takumistudios.morphmod.morph;

import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.ability.AbilitySlot;
import com.takumistudios.morphmod.ability.MorphAbility;
import com.takumistudios.morphmod.config.MorphConfig;
import com.takumistudios.morphmod.data.MorphAttachments;
import com.takumistudios.morphmod.network.CooldownPayload;
import com.takumistudios.morphmod.network.MorphNetworking;
import com.takumistudios.morphmod.util.CooldownTracker;
import com.takumistudios.morphmod.util.StatMath;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;

/**
 * Applies and removes morphs, runs passives and abilities. Server-side except {@link #current(Player)}.
 */
public final class MorphManager {
	/** Id of the attribute modifier the mod puts on every changed attribute. */
	public static final Identifier MODIFIER_ID = MorphMod.id("morph");
	private static final float DEFAULT_FLYING_SPEED = 0.05F;
	/** Long enough to avoid the night vision flicker that starts at 10 seconds. */
	private static final int PASSIVE_EFFECT_TICKS = 15 * 20;

	private static final CooldownTracker COOLDOWNS = new CooldownTracker(AbilitySlot.values().length);
	private static final Set<UUID> ACTIVE = new HashSet<>();
	private static final Map<MorphAbility, com.takumistudios.morphmod.util.FeatureGuard> ABILITY_GUARDS = new java.util.IdentityHashMap<>();

	private MorphManager() {
	}

	/** The player's current morph. Works on both client and server (the attachment is synced). */
	public static Optional<MorphDefinition> current(Player player) {
		return Optional.ofNullable(currentDefinition(player));
	}

	/** Allocation-free lookup for render hooks and passive checks. */
	public static MorphDefinition currentDefinition(Player player) {
		Identifier id = player.getAttached(MorphAttachments.CURRENT_MORPH);
		return id == null ? null : MorphRegistry.definition(id);
	}

	public static boolean has(Player player, Passive passive) {
		MorphDefinition definition = currentDefinition(player);
		return definition != null && definition.has(passive);
	}

	// --- Unlocks -------------------------------------------------------------------------------

	public static List<Identifier> unlocked(Player player) {
		return player.getAttachedOrCreate(MorphAttachments.UNLOCKED_MORPHS);
	}

	public static boolean isUnlocked(Player player, Identifier id) {
		return !MorphMod.config().requireUnlock || unlocked(player).contains(id);
	}

	/** @return true if the form was newly unlocked */
	public static boolean unlock(ServerPlayer player, EntityType<?> type) {
		if (!MorphRegistry.isMorphable(type)) {
			return false;
		}
		Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
		List<Identifier> current = unlocked(player);
		if (current.contains(id) || current.size() >= MorphAttachments.MAX_FORMS) {
			return false;
		}
		List<Identifier> updated = new ArrayList<>(current);
		updated.add(id);
		updated.sort(null);
		player.setAttached(MorphAttachments.UNLOCKED_MORPHS, List.copyOf(updated));
		return true;
	}

	public static void unlockAll(ServerPlayer player) {
		player.setAttached(MorphAttachments.UNLOCKED_MORPHS, MorphRegistry.allMorphableIds().stream().limit(MorphAttachments.MAX_FORMS).toList());
	}

	public static void resetUnlocks(ServerPlayer player) {
		player.setAttached(MorphAttachments.UNLOCKED_MORPHS, List.of());
	}

	// --- Morphing ------------------------------------------------------------------------------

	public static boolean morph(ServerPlayer player, EntityType<?> type) {
		Optional<MorphDefinition> definition = MorphRegistry.get(type);
		if (definition.isEmpty()) {
			return false;
		}
		com.takumistudios.morphmod.character.CharacterManager.clear(player);
		float oldMax = player.getMaxHealth();
		removeEffects(player);
		player.setAttached(MorphAttachments.CURRENT_MORPH, BuiltInRegistries.ENTITY_TYPE.getKey(type));
		ACTIVE.add(player.getUUID());
		applyStats(player, definition.get());
		applyFlight(player, definition.get());
		player.setHealth(StatMath.rescaleHealth(player.getHealth(), oldMax, player.getMaxHealth()));
		player.refreshDimensions();
		COOLDOWNS.clear(player.getUUID());
		return true;
	}

	public static void demorph(ServerPlayer player) {
		com.takumistudios.morphmod.character.CharacterManager.clear(player);
		ACTIVE.remove(player.getUUID());
		if (!player.hasAttached(MorphAttachments.CURRENT_MORPH)) {
			return;
		}
		float oldMax = player.getMaxHealth();
		player.removeAttached(MorphAttachments.CURRENT_MORPH);
		removeEffects(player);
		syncStats(player, removeStats(player));
		resetFlight(player);
		player.setHealth(StatMath.rescaleHealth(player.getHealth(), oldMax, player.getMaxHealth()));
		player.refreshDimensions();
		COOLDOWNS.clear(player.getUUID());
	}

	/**
	 * Makes the player's attributes and abilities match their attachment again.
	 * Called on join and respawn so no modifier survives without a morph (and vice versa).
	 */
	public static void resync(ServerPlayer player) {
		Optional<MorphDefinition> definition = current(player);
		if (definition.isPresent()) {
			ACTIVE.add(player.getUUID());
			applyStats(player, definition.get());
			applyFlight(player, definition.get());
		} else {
			ACTIVE.remove(player.getUUID());
			player.removeAttached(MorphAttachments.CURRENT_MORPH);
			syncStats(player, removeStats(player));
			resetFlight(player);
		}
		if (player.getHealth() > player.getMaxHealth()) {
			player.setHealth(player.getMaxHealth());
		}
		player.refreshDimensions();
	}

	private static void applyStats(ServerPlayer player, MorphDefinition definition) {
		List<AttributeInstance> changed = removeStats(player);
		MorphConfig config = MorphMod.config();
		for (Map.Entry<Holder<Attribute>, Double> entry : definition.stats().entrySet()) {
			AttributeInstance instance = player.getAttribute(entry.getKey());
			if (instance == null) {
				continue;
			}
			double target = entry.getValue();
			if (entry.getKey().equals(Attributes.MAX_HEALTH)) {
				target = Math.min(target, config.maxHealthCap);
			} else if (entry.getKey().equals(Attributes.ATTACK_DAMAGE)) {
				target *= config.damageMultiplier;
			}
			double amount = StatMath.modifierFor(instance.getBaseValue(), target);
			// Permanent so it is saved with the player; otherwise health above 20 would be clamped on load.
			instance.addOrReplacePermanentModifier(new AttributeModifier(MODIFIER_ID, amount, AttributeModifier.Operation.ADD_VALUE));
			if (!changed.contains(instance)) changed.add(instance);
		}
		syncStats(player, changed);
	}

	private static List<AttributeInstance> removeStats(ServerPlayer player) {
		List<AttributeInstance> changed = new ArrayList<>();
		BuiltInRegistries.ATTRIBUTE.listElements().forEach(holder -> {
			AttributeInstance instance = player.getAttribute(holder);
			if (instance != null && instance.getModifier(MODIFIER_ID) != null) {
				instance.removeModifier(MODIFIER_ID);
				changed.add(instance);
			}
		});
		return changed;
	}

	private static void syncStats(ServerPlayer player, List<AttributeInstance> changed) {
		// The owner's entity tracker can miss updates while reloading client resources.
		// Send a vanilla delta once per transition; never poll or resend it from the tick.
		if (!changed.isEmpty()) player.connection.send(new ClientboundUpdateAttributesPacket(player.getId(), changed));
	}

	private static void applyFlight(ServerPlayer player, MorphDefinition definition) {
		if (!definition.has(Passive.FLIGHT) || !MorphMod.config().allowFlight) {
			resetFlight(player);
			return;
		}
		Abilities abilities = player.getAbilities();
		abilities.mayfly = true;
		abilities.setFlyingSpeed(definition.flyingSpeed());
		player.onUpdateAbilities();
	}

	private static void resetFlight(ServerPlayer player) {
		Abilities abilities = player.getAbilities();
		if (!player.isCreative() && !player.isSpectator()) {
			abilities.mayfly = false;
			abilities.flying = false;
		}
		abilities.setFlyingSpeed(DEFAULT_FLYING_SPEED);
		player.onUpdateAbilities();
	}

	// --- Passives ------------------------------------------------------------------------------

	public static void tickPassives(ServerPlayer player) {
		Optional<MorphDefinition> current = current(player);
		if (current.isEmpty() || !player.isAlive()) {
			return;
		}
		MorphDefinition definition = current.get();
		ServerLevel level = player.level();

		if (definition.has(Passive.FIRE_IMMUNE) && player.isOnFire()) {
			player.clearFire();
		}
		if (definition.has(Passive.NIGHT_VISION)) {
			refreshEffect(player, MobEffects.NIGHT_VISION);
		}
		if (definition.has(Passive.SLOW_FALLING)) {
			refreshEffect(player, MobEffects.SLOW_FALLING);
		}
		if (definition.has(Passive.FAST_SWIMMING) && player.isInWater()) {
			refreshEffect(player, MobEffects.DOLPHINS_GRACE);
		}
		if (definition.has(Passive.WATER_BREATHING)) {
			player.setAirSupply(player.getMaxAirSupply());
		}
		if (definition.has(Passive.DARKNESS_IMMUNE) && player.hasEffect(MobEffects.DARKNESS)) {
			player.removeEffect(MobEffects.DARKNESS);
		}
		if (definition.has(Passive.WATER_SENSITIVE) && player.isInWaterOrRain() && player.tickCount % 10 == 0) {
			player.hurtServer(level, level.damageSources().drown(), 1.0F);
		}
		if (definition.has(Passive.BURNS_IN_SUN) && player.tickCount % 20 == 0 && isInSunlight(player)) {
			player.igniteForSeconds(8.0F);
		}
		if (definition.has(Passive.FLIGHT) && MorphMod.config().allowFlight && !player.getAbilities().mayfly) {
			applyFlight(player, definition);
		}
	}

	private static boolean isInSunlight(ServerPlayer player) {
		ServerLevel level = player.level();
		BlockPos eyes = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
		return level.environmentAttributes().getValue(EnvironmentAttributes.MONSTERS_BURN, player.position())
			&& level.canSeeSky(eyes)
			&& !player.isInWaterOrRain()
			&& player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
			&& !player.isCreative()
			&& !player.isSpectator();
	}

	/** Passive effects are ambient and hidden so they are recognisable when removing them. */
	private static void refreshEffect(ServerPlayer player, Holder<MobEffect> effect) {
		MobEffectInstance existing = player.getEffect(effect);
		if (existing == null || (existing.isAmbient() && existing.getDuration() < PASSIVE_EFFECT_TICKS - 20)) {
			player.addEffect(new MobEffectInstance(effect, PASSIVE_EFFECT_TICKS, 0, true, false, false));
		}
	}

	private static void removeEffects(ServerPlayer player) {
		for (Holder<MobEffect> effect : List.of(MobEffects.NIGHT_VISION, MobEffects.SLOW_FALLING, MobEffects.DOLPHINS_GRACE)) {
			MobEffectInstance existing = player.getEffect(effect);
			if (existing != null && existing.isAmbient() && !existing.isVisible() && existing.getDuration() <= PASSIVE_EFFECT_TICKS) {
				player.removeEffect(effect);
			}
		}
	}

	// --- Abilities -----------------------------------------------------------------------------

	public static void useAbility(ServerPlayer player, AbilitySlot slot) {
		com.takumistudios.morphmod.character.CharacterManager.stop(player);
		Optional<MorphDefinition> current = current(player);
		if (current.isEmpty() || !player.isAlive() || player.isSpectator()) {
			return;
		}
		MorphAbility ability = current.get().ability(slot);
		if (ability == null) {
			player.sendOverlayMessage(Component.translatable("message.morphmod.no_ability"));
			return;
		}
		long now = player.level().getServer().getTickCount();
		if (!COOLDOWNS.isReady(player.getUUID(), slot.ordinal(), now)) {
			return;
		}
		if (ABILITY_GUARDS.computeIfAbsent(ability, key -> new com.takumistudios.morphmod.util.FeatureGuard("ability " + key.id()))
			.get(() -> ability.activate(player), false)) {
			int cooldown = StatMath.scaleCooldown(ability.cooldownTicks(), MorphMod.config().cooldownMultiplier);
			COOLDOWNS.start(player.getUUID(), slot.ordinal(), now, cooldown);
			if (MorphNetworking.negotiated(player) && ServerPlayNetworking.canSend(player, CooldownPayload.TYPE)) {
				ServerPlayNetworking.send(player, new CooldownPayload(slot.ordinal(), cooldown));
			}
		}
	}

	public static void forget(ServerPlayer player) {
		ACTIVE.remove(player.getUUID());
		COOLDOWNS.clear(player.getUUID());
	}

	public static void tickActive(net.minecraft.server.MinecraftServer server) {
		for (UUID id : ACTIVE) {
			ServerPlayer player = server.getPlayerList().getPlayer(id);
			if (player != null) tickPassives(player);
		}
	}
}
