package com.takumistudios.morphmod.util;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RequestLimiterTest {
	@Test void limitsBurstsAndAllowsNextWindow() {
		RequestLimiter limiter = new RequestLimiter(2, 20);
		UUID id = UUID.randomUUID();
		assertTrue(limiter.allow(id, 100));
		assertTrue(limiter.allow(id, 100));
		assertFalse(limiter.allow(id, 119));
		assertTrue(limiter.allow(id, 120));
	}
	@Test void playersAndReconnectsAreIndependent() {
		RequestLimiter limiter = new RequestLimiter(1, 20);
		UUID id = UUID.randomUUID();
		assertTrue(limiter.allow(id, 10));
		assertFalse(limiter.allow(id, 10));
		assertTrue(limiter.allow(UUID.randomUUID(), 10));
		limiter.forget(id);
		assertTrue(limiter.allow(id, 10));
		assertTrue(limiter.allow(id, 0));
	}
}
