package io.github.jmarc.morph.morph;

import io.github.jmarc.morph.ability.impl.ArrowAbility;
import io.github.jmarc.morph.ability.impl.DarknessPulseAbility;
import io.github.jmarc.morph.ability.impl.ExplodeAbility;
import io.github.jmarc.morph.ability.impl.FireballAbility;
import io.github.jmarc.morph.ability.impl.FlingAbility;
import io.github.jmarc.morph.ability.impl.PlayDeadAbility;
import io.github.jmarc.morph.ability.impl.SonicBoomAbility;
import io.github.jmarc.morph.ability.impl.TeleportAbility;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

/**
 * All known morphs. Mobs with a hand-written definition get stats and powers;
 * any other living mob gets an automatic definition (shape + health + damage + armor).
 */
public final class MorphRegistry {
	/** Mobs that can never be morphed into. */
	private static final Set<EntityType<?>> BLACKLIST = Set.of(
		EntityTypes.PLAYER,
		EntityTypes.MANNEQUIN,
		EntityTypes.ARMOR_STAND,
		EntityTypes.ENDER_DRAGON
	);

	/** Attributes copied from the mob's own defaults for automatic definitions. */
	private static final List<Holder<Attribute>> FALLBACK_ATTRIBUTES = List.of(
		Attributes.MAX_HEALTH,
		Attributes.ATTACK_DAMAGE,
		Attributes.ARMOR,
		Attributes.ARMOR_TOUGHNESS,
		Attributes.KNOCKBACK_RESISTANCE
	);

	private static final Map<EntityType<?>, MorphDefinition> DEFINITIONS = new HashMap<>();
	private static final Map<EntityType<?>, MorphDefinition> FALLBACK_CACHE = new HashMap<>();

	private MorphRegistry() {
	}

	public static void init() {
		register(MorphDefinition.builder(EntityTypes.WARDEN)
			.stat(Attributes.MAX_HEALTH, 500.0)
			.stat(Attributes.ATTACK_DAMAGE, 30.0)
			.stat(Attributes.ATTACK_KNOCKBACK, 1.5)
			.stat(Attributes.KNOCKBACK_RESISTANCE, 1.0)
			.stat(Attributes.MOVEMENT_SPEED, 0.085)
			.stat(Attributes.STEP_HEIGHT, 1.0)
			.passive(Passive.DARKNESS_IMMUNE, Passive.VIBRATION_SENSE)
			.primary(new SonicBoomAbility())
			.secondary(new DarknessPulseAbility())
			.build());

		register(MorphDefinition.builder(EntityTypes.CREEPER)
			.stat(Attributes.MAX_HEALTH, 20.0)
			.passive(Passive.NO_FALL_DAMAGE)
			.primary(new ExplodeAbility())
			.build());

		register(MorphDefinition.builder(EntityTypes.ENDERMAN)
			.stat(Attributes.MAX_HEALTH, 40.0)
			.stat(Attributes.ATTACK_DAMAGE, 7.0)
			.stat(Attributes.MOVEMENT_SPEED, 0.12)
			.stat(Attributes.STEP_HEIGHT, 1.0)
			.stat(Attributes.ENTITY_INTERACTION_RANGE, 4.0)
			.stat(Attributes.BLOCK_INTERACTION_RANGE, 6.5)
			.passive(Passive.WATER_SENSITIVE)
			.primary(new TeleportAbility())
			.build());

		register(MorphDefinition.builder(EntityTypes.BLAZE)
			.stat(Attributes.MAX_HEALTH, 20.0)
			.stat(Attributes.ATTACK_DAMAGE, 6.0)
			.passive(Passive.FIRE_IMMUNE, Passive.SLOW_FALLING, Passive.WATER_SENSITIVE)
			.primary(FireballAbility.blaze())
			.build());

		register(MorphDefinition.builder(EntityTypes.SPIDER)
			.stat(Attributes.MAX_HEALTH, 16.0)
			.stat(Attributes.ATTACK_DAMAGE, 2.0)
			.stat(Attributes.MOVEMENT_SPEED, 0.12)
			.passive(Passive.CLIMB_WALLS, Passive.NIGHT_VISION)
			.build());

		register(MorphDefinition.builder(EntityTypes.BAT)
			.stat(Attributes.MAX_HEALTH, 6.0)
			.passive(Passive.FLIGHT, Passive.NIGHT_VISION, Passive.NO_FALL_DAMAGE)
			.flyingSpeed(0.06F)
			.build());

		register(MorphDefinition.builder(EntityTypes.IRON_GOLEM)
			.stat(Attributes.MAX_HEALTH, 100.0)
			.stat(Attributes.ATTACK_DAMAGE, 15.0)
			.stat(Attributes.KNOCKBACK_RESISTANCE, 1.0)
			.stat(Attributes.MOVEMENT_SPEED, 0.08)
			.stat(Attributes.STEP_HEIGHT, 1.0)
			.passive(Passive.NO_FALL_DAMAGE)
			.primary(new FlingAbility())
			.build());

		register(MorphDefinition.builder(EntityTypes.SKELETON)
			.stat(Attributes.MAX_HEALTH, 20.0)
			.passive(Passive.BURNS_IN_SUN)
			.primary(new ArrowAbility())
			.build());

		register(MorphDefinition.builder(EntityTypes.GHAST)
			.stat(Attributes.MAX_HEALTH, 10.0)
			.passive(Passive.FLIGHT, Passive.FIRE_IMMUNE, Passive.NO_FALL_DAMAGE)
			.flyingSpeed(0.03F)
			.primary(FireballAbility.ghast())
			.build());

		register(MorphDefinition.builder(EntityTypes.AXOLOTL)
			.stat(Attributes.MAX_HEALTH, 14.0)
			.stat(Attributes.ATTACK_DAMAGE, 2.0)
			.stat(Attributes.WATER_MOVEMENT_EFFICIENCY, 1.0)
			.passive(Passive.WATER_BREATHING, Passive.FAST_SWIMMING)
			.primary(new PlayDeadAbility())
			.build());
	}

	public static void register(MorphDefinition definition) {
		DEFINITIONS.put(definition.type(), definition);
	}

	public static boolean isMorphable(EntityType<?> type) {
		return !BLACKLIST.contains(type) && DefaultAttributes.hasSupplier(type);
	}

	public static boolean hasCustomDefinition(EntityType<?> type) {
		return DEFINITIONS.containsKey(type);
	}

	public static Optional<MorphDefinition> get(EntityType<?> type) {
		if (!isMorphable(type)) {
			return Optional.empty();
		}
		MorphDefinition custom = DEFINITIONS.get(type);
		if (custom != null) {
			return Optional.of(custom);
		}
		return Optional.of(FALLBACK_CACHE.computeIfAbsent(type, MorphRegistry::createFallback));
	}

	public static Optional<MorphDefinition> get(Identifier id) {
		return BuiltInRegistries.ENTITY_TYPE.getOptional(id).flatMap(MorphRegistry::get);
	}

	public static List<Identifier> allMorphableIds() {
		return BuiltInRegistries.ENTITY_TYPE.stream()
			.filter(MorphRegistry::isMorphable)
			.map(BuiltInRegistries.ENTITY_TYPE::getKey)
			.sorted()
			.toList();
	}

	@SuppressWarnings("unchecked")
	private static MorphDefinition createFallback(EntityType<?> type) {
		AttributeSupplier supplier = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) type);
		MorphDefinition.Builder builder = MorphDefinition.builder(type);
		for (Holder<Attribute> attribute : FALLBACK_ATTRIBUTES) {
			if (supplier.hasAttribute(attribute)) {
				builder.stat(attribute, supplier.getBaseValue(attribute));
			}
		}
		return builder.build();
	}
}
