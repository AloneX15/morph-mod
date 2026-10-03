package com.takumistudios.morphmod.compat;

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
    private PlayerAnimationState() { }
}
