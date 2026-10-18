package com.takumistudios.morphmod.character;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerLocomotionTest {
    @Test void groundSpeedMatchesGeckoLibUnitsAndRejectsTeleportsAndInvalidInput() {
        assertEquals(0.215F, PlayerLocomotion.speed(0.215), 0.00001F);
        assertEquals(0, PlayerLocomotion.speed(Double.NaN));
        assertEquals(0, PlayerLocomotion.speed(Double.POSITIVE_INFINITY));
        assertEquals(0, PlayerLocomotion.speed(-1));
        assertEquals(1, PlayerLocomotion.speed(100));
    }
    @Test void standingHasNoStrideOrRoll() {
        for (float age : new float[]{0, 3, 9, 1200}) {
            assertEquals(0, PlayerLocomotion.legPitch(age, 0, false, true), 0.00001F);
            assertEquals(PlayerLocomotion.radians(2.5F), PlayerLocomotion.armPitch(age, 0, false, true));
            assertEquals(0, PlayerLocomotion.roll(age, 0, false), 0.00001F);
        }
    }
    @Test void molangQuarterCycleAndOppositeLimbsArePreserved() {
        // 600 degrees/second reaches 90 degrees at 3 ticks, NOT 3 seconds.
        assertEquals(1, PlayerLocomotion.wave(3, false), 0.00001F);
        assertEquals(PlayerLocomotion.radians(8), PlayerLocomotion.legPitch(3, 0.2F, false, true), 0.00001F);
        assertEquals(-PlayerLocomotion.legPitch(3, 0.2F, false, true), PlayerLocomotion.legPitch(3, 0.2F, false, false));
        assertEquals(PlayerLocomotion.radians(-7.5F), PlayerLocomotion.armPitch(3, 0.2F, false, true), 0.00001F);
    }
    @Test void crouchingHasSlowerCadenceAndCappedStride() {
        assertEquals(1, PlayerLocomotion.wave(9, true), 0.00001F);
        assertEquals(PlayerLocomotion.radians(40), PlayerLocomotion.legPitch(9, 1, true, true), 0.00001F);
        assertEquals(PlayerLocomotion.radians(-30), PlayerLocomotion.armPitch(9, 1, true, true), 0.00001F);
    }
}
