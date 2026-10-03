package com.takumistudios.morphmod.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks per-player, per-slot ability cooldowns against an external tick clock.
 * Pure Java so it can be unit tested without bootstrapping Minecraft.
 */
public final class CooldownTracker {
	private final int slots;
	private final Map<UUID, long[]> readyAt = new HashMap<>();

	public CooldownTracker(int slots) {
		this.slots = slots;
	}

	public boolean isReady(UUID player, int slot, long now) {
		return remaining(player, slot, now) == 0;
	}

	public long remaining(UUID player, int slot, long now) {
		long[] times = readyAt.get(player);
		if (times == null) {
			return 0;
		}
		return Math.max(0, times[slot] - now);
	}

	public void start(UUID player, int slot, long now, long durationTicks) {
		readyAt.computeIfAbsent(player, id -> new long[slots])[slot] = now + Math.max(0, durationTicks);
	}

	public void clear(UUID player) {
		readyAt.remove(player);
	}
}
