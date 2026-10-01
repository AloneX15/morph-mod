package io.github.jmarc.morph.util;

/**
 * Small numeric helpers used when applying morph stats. Pure Java for unit testing.
 */
public final class StatMath {
	private StatMath() {
	}

	/** Amount for an ADD_VALUE modifier that turns {@code base} into {@code target}. */
	public static double modifierFor(double base, double target) {
		return target - base;
	}

	/**
	 * Keeps the player's health fraction when their max health changes,
	 * e.g. 10/20 HP becomes 250/500 HP when turning into a Warden.
	 */
	public static float rescaleHealth(float health, float oldMax, float newMax) {
		if (oldMax <= 0 || newMax <= 0) {
			return Math.max(1.0F, newMax);
		}
		float scaled = health / oldMax * newMax;
		return Math.clamp(scaled, 1.0F, newMax);
	}

	/** Applies a config multiplier to a cooldown, never going below one tick. */
	public static int scaleCooldown(int ticks, double multiplier) {
		if (ticks <= 0) {
			return 0;
		}
		return Math.max(1, (int) Math.round(ticks * Math.max(0, multiplier)));
	}
}
