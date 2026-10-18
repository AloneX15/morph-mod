package com.takumistudios.morphmod.character;

/** Native port of the procedural idle/sneaking channels in dedsafio_4.animation.json.
 * MoLang trigonometry uses degrees, anim_time seconds, GeckoLib ground_speed blocks/tick.
 * Bedrock X/Y rotations are inverted when applied to vanilla model parts.
 */
public final class PlayerLocomotion {
    private PlayerLocomotion() { }

    public static float speed(double blocksPerTick) {
        return Double.isFinite(blocksPerTick) ? (float)Math.clamp(blocksPerTick, 0, 1) : 0;
    }
    public static float wave(float ageInTicks, boolean crouching) {
        return (float)Math.sin(Math.toRadians(ageInTicks / 20.0 * (crouching ? 200 : 600)));
    }
    public static float armPitch(float age, float speed, boolean crouching, boolean right) {
        float amplitude = crouching ? Math.clamp(speed * 100, 0, 40) : speed * 50;
        return radians((crouching ? 10 : 2.5F) + (right ? -1 : 1) * wave(age, crouching) * amplitude);
    }
    public static float legPitch(float age, float speed, boolean crouching, boolean right) {
        float amplitude = crouching ? Math.clamp(speed * 100, 0, 40) : speed * 40;
        return radians((right ? 1 : -1) * wave(age, crouching) * amplitude);
    }
    public static float roll(float age, float speed, boolean head) {
        double phase = Math.toRadians(age / 20.0 * 600);
        return radians((float)((head ? Math.sin(phase) : Math.cos(phase)) * Math.clamp(speed * 100, 0, 0.5F)));
    }
    public static float radians(float degrees) { return degrees * ((float)Math.PI / 180); }
}
