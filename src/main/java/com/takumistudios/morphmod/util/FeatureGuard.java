package com.takumistudios.morphmod.util;

import com.takumistudios.morphmod.MorphMod;
import java.util.function.Supplier;

/** A callback failure disables only this feature for the current process. */
public final class FeatureGuard {
	private final String name;
	private boolean enabled = true;
	public FeatureGuard(String name) { this.name = name; }
	public boolean enabled() { return enabled; }
	public void disable(Throwable failure) {
		enabled = false;
		MorphMod.LOGGER.error("Feature {} disabled after a failure", name, failure);
	}
	public void run(Runnable action) { get(() -> { action.run(); return true; }, false); }
	public <T> T get(Supplier<T> action, T fallback) {
		if (!enabled) return fallback;
		try { return action.get(); }
		catch (RuntimeException | LinkageError e) {
			disable(e);
			return fallback;
		}
	}
}
