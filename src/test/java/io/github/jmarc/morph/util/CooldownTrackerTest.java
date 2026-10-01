package io.github.jmarc.morph.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class CooldownTrackerTest {
	private final UUID player = UUID.randomUUID();

	@Test
	void unknownPlayerIsReady() {
		CooldownTracker tracker = new CooldownTracker(2);
		assertTrue(tracker.isReady(player, 0, 100));
		assertEquals(0, tracker.remaining(player, 1, 100));
	}

	@Test
	void cooldownExpiresAfterDuration() {
		CooldownTracker tracker = new CooldownTracker(2);
		tracker.start(player, 0, 100, 60);
		assertFalse(tracker.isReady(player, 0, 159));
		assertEquals(1, tracker.remaining(player, 0, 159));
		assertTrue(tracker.isReady(player, 0, 160));
	}

	@Test
	void slotsAreIndependent() {
		CooldownTracker tracker = new CooldownTracker(2);
		tracker.start(player, 0, 0, 100);
		assertTrue(tracker.isReady(player, 1, 10));
	}

	@Test
	void clearResetsEverything() {
		CooldownTracker tracker = new CooldownTracker(2);
		tracker.start(player, 1, 0, 100);
		tracker.clear(player);
		assertTrue(tracker.isReady(player, 1, 1));
	}
}
