package com.takumistudios.morphmod.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Main-thread, fixed-window limiter. Entries are removed on disconnect. */
public final class RequestLimiter {
	private record Window(long start, int count) { }
	private final Map<UUID, Window> windows = new HashMap<>();
	private final int maximum;
	private final int windowTicks;
	public RequestLimiter(int maximum, int windowTicks) {
		if (maximum < 1 || windowTicks < 1) throw new IllegalArgumentException("Positive limits required");
		this.maximum = maximum;
		this.windowTicks = windowTicks;
	}
	public boolean allow(UUID player, long now) {
		Window window = windows.get(player);
		if (window == null || now < window.start() || now - window.start() >= windowTicks) {
			windows.put(player, new Window(now, 1));
			return true;
		}
		if (window.count() >= maximum) return false;
		windows.put(player, new Window(window.start(), window.count() + 1));
		return true;
	}
	public void forget(UUID player) { windows.remove(player); }
}
