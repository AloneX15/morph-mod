package io.github.jmarc.morph.client;

import io.github.jmarc.morph.ability.AbilitySlot;
import java.util.Arrays;

/**
 * Client copy of the ability cooldowns, driven by {@code CooldownPayload}. Only used for the HUD.
 */
public final class ClientMorphState {
	private static final long[] READY_AT = new long[AbilitySlot.values().length];
	private static final int[] DURATION = new int[AbilitySlot.values().length];
	private static long clientTicks;

	private ClientMorphState() {
	}

	public static void tick() {
		clientTicks++;
	}

	public static void startCooldown(int slot, int ticks) {
		if (slot < 0 || slot >= READY_AT.length) {
			return;
		}
		READY_AT[slot] = clientTicks + ticks;
		DURATION[slot] = ticks;
	}

	/** 0 when ready, 1 right after use. */
	public static float cooldownProgress(AbilitySlot slot) {
		int i = slot.ordinal();
		long remaining = READY_AT[i] - clientTicks;
		if (remaining <= 0 || DURATION[i] <= 0) {
			return 0.0F;
		}
		return (float) remaining / DURATION[i];
	}

	public static float secondsLeft(AbilitySlot slot) {
		return Math.max(0, READY_AT[slot.ordinal()] - clientTicks) / 20.0F;
	}

	public static void reset() {
		Arrays.fill(READY_AT, 0);
		Arrays.fill(DURATION, 0);
	}
}
