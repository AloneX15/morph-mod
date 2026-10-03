package com.takumistudios.morphmod.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StatMathTest {
	@Test
	void modifierTurnsBaseIntoTarget() {
		assertEquals(480.0, StatMath.modifierFor(20.0, 500.0));
		assertEquals(-14.0, StatMath.modifierFor(20.0, 6.0));
	}

	@Test
	void healthKeepsItsFraction() {
		assertEquals(250.0F, StatMath.rescaleHealth(10.0F, 20.0F, 500.0F));
		assertEquals(3.0F, StatMath.rescaleHealth(10.0F, 20.0F, 6.0F));
	}

	@Test
	void healthNeverDropsBelowOneOrAboveMax() {
		assertEquals(1.0F, StatMath.rescaleHealth(0.1F, 20.0F, 6.0F));
		assertEquals(20.0F, StatMath.rescaleHealth(30.0F, 20.0F, 20.0F));
	}

	@Test
	void cooldownScaling() {
		assertEquals(30, StatMath.scaleCooldown(60, 0.5));
		assertEquals(1, StatMath.scaleCooldown(60, 0.0001));
		assertEquals(0, StatMath.scaleCooldown(0, 2.0));
	}
}
