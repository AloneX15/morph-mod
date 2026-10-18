package com.takumistudios.morphmod.client.render;

/** Extracted once from the player; rendering never reads mutable world entities. */
public interface PlayerWalkState {
    boolean morphmod$walkEnabled();
    float morphmod$groundSpeed();
    void morphmod$setWalk(boolean enabled, float speed);
}
