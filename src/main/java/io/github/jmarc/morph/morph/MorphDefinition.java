package io.github.jmarc.morph.morph;

import io.github.jmarc.morph.ability.AbilitySlot;
import io.github.jmarc.morph.ability.MorphAbility;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jspecify.annotations.Nullable;

/**
 * Everything a morph changes on the player: absolute attribute values, passives and up to two active abilities.
 *
 * @param stats target attribute values (absolute, not deltas); attributes not listed keep the player's value
 * @param flyingSpeed flying speed for {@link Passive#FLIGHT} morphs (player default is 0.05)
 */
public record MorphDefinition(
	EntityType<?> type,
	Map<Holder<Attribute>, Double> stats,
	Set<Passive> passives,
	@Nullable MorphAbility primary,
	@Nullable MorphAbility secondary,
	float flyingSpeed
) {
	public @Nullable MorphAbility ability(AbilitySlot slot) {
		return slot == AbilitySlot.PRIMARY ? primary : secondary;
	}

	public boolean has(Passive passive) {
		return passives.contains(passive);
	}

	public static Builder builder(EntityType<?> type) {
		return new Builder(type);
	}

	public static final class Builder {
		private final EntityType<?> type;
		private final Map<Holder<Attribute>, Double> stats = new LinkedHashMap<>();
		private final Set<Passive> passives = EnumSet.noneOf(Passive.class);
		private @Nullable MorphAbility primary;
		private @Nullable MorphAbility secondary;
		private float flyingSpeed = 0.05F;

		private Builder(EntityType<?> type) {
			this.type = type;
		}

		public Builder stat(Holder<Attribute> attribute, double value) {
			stats.put(attribute, value);
			return this;
		}

		public Builder passive(Passive... values) {
			passives.addAll(Set.of(values));
			return this;
		}

		public Builder primary(MorphAbility ability) {
			this.primary = ability;
			return this;
		}

		public Builder secondary(MorphAbility ability) {
			this.secondary = ability;
			return this;
		}

		public Builder flyingSpeed(float speed) {
			this.flyingSpeed = speed;
			return this;
		}

		public MorphDefinition build() {
			Set<Passive> passiveCopy = passives.isEmpty() ? Set.of() : EnumSet.copyOf(passives);
			return new MorphDefinition(type, Map.copyOf(stats), passiveCopy, primary, secondary, flyingSpeed);
		}
	}
}
