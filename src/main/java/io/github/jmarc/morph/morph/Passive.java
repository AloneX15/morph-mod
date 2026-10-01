package io.github.jmarc.morph.morph;

/**
 * Always-on traits of a morph, handled by {@link MorphManager#tickPassives} and the mixins.
 */
public enum Passive {
	FIRE_IMMUNE,
	NO_FALL_DAMAGE,
	WATER_BREATHING,
	CLIMB_WALLS,
	FLIGHT,
	NIGHT_VISION,
	SLOW_FALLING,
	FAST_SWIMMING,
	WATER_SENSITIVE,
	BURNS_IN_SUN,
	DARKNESS_IMMUNE,
	/** Client-only: outlines moving creatures nearby (Warden). */
	VIBRATION_SENSE
}
