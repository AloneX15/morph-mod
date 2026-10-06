package com.takumistudios.morphmod.compat;

import java.util.Objects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;

public final class PlayerAnimationState {
    public static boolean swinging(Player player) {
        //? if >=26.3 {
        return player.isSwinging();
        //?} else {
        /*return player.swinging;
        *///?}
    }
    public static InteractionHand hand(Player player) {
        //? if >=26.3 {
        return player.getCurrentSwing().hand();
        //?} else {
        /*return player.swingingArm;
        *///?}
    }
    public static float progress(Player player, float tick) {
        //? if >=26.3 {
        return player.getSwingAnimation(tick);
        //?} else {
        /*return player.getAttackAnim(tick);
        *///?}
    }
    /** Opaque value that changes whenever a new swing starts; null while idle. */
    public static Object swingToken(Player player) {
        //? if >=26.3 {
        return player.getCurrentSwing();
        //?} else {
        /*return player.swinging ? Integer.valueOf(player.swingTime) : null;
        *///?}
    }
    /** True when {@code now} belongs to a swing that started after {@code before}. */
    public static boolean swingStarted(Object before, Object now) {
        if (now == null) return false;
        //? if >=26.3 {
        return now != before; // A new SwingDescription is created for every swing.
        //?} else {
        /*return !(before instanceof Integer previous) || (Integer) now < previous;
        *///?}
    }
    /** Starts the same swing on a client-only mirror entity (never sends packets). */
    public static void copySwing(Player player, LivingEntity target) {
        //? if >=26.3 {
        var swing = Objects.requireNonNull(player.getCurrentSwing());
        target.swing(swing.hand(), swing.animation(), false);
        //?} else {
        /*target.swing(player.swingingArm == null ? InteractionHand.MAIN_HAND : player.swingingArm);
        *///?}
    }
    private PlayerAnimationState() { }
}
