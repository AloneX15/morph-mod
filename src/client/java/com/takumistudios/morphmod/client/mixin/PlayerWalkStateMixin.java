package com.takumistudios.morphmod.client.mixin;

import com.takumistudios.morphmod.client.render.PlayerWalkState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public abstract class PlayerWalkStateMixin implements PlayerWalkState {
    @Unique private boolean morphmod$walk;
    @Unique private float morphmod$speed;
    public boolean morphmod$walkEnabled() { return morphmod$walk; }
    public float morphmod$groundSpeed() { return morphmod$speed; }
    public void morphmod$setWalk(boolean enabled, float speed) { morphmod$walk = enabled; morphmod$speed = speed; }
}
