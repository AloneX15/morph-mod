package com.takumistudios.morphmod.client.render;

/** Extracted once from the player; rendering never reads mutable world entities. */
public interface PlayerWalkState {
    com.takumistudios.morphmod.client.animation.PlayerAnimationPose morphmod$animationPose();
    boolean morphmod$walkEnabled();
    float morphmod$groundSpeed();
    void morphmod$setWalk(boolean enabled, float speed);
}
