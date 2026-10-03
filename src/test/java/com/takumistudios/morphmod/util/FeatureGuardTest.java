package com.takumistudios.morphmod.util;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class FeatureGuardTest {
	@Test void runtimeFailureDisablesOnlyItsOwnFeature() {
		FeatureGuard failing = new FeatureGuard("test failure");
		FeatureGuard healthy = new FeatureGuard("test healthy");
		AtomicInteger calls = new AtomicInteger();
		assertFalse(failing.get(() -> { calls.incrementAndGet(); throw new IllegalStateException("regression"); }, false));
		assertFalse(failing.get(() -> { calls.incrementAndGet(); return true; }, false));
		assertEquals(1, calls.get());
		assertTrue(healthy.get(() -> true, false));
	}
}
