package com.takumistudios.morphmod.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.takumistudios.morphmod.client.render.PlayerWalkState;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;

/** Imported waist/root rotation replaces the global swimming/elytra pitch, never adds it twice. */
@Mixin(AvatarRenderer.class)
public abstract class AnimationRotationsMixin {
    @WrapMethod(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V")
    private void morphmod$rotations(AvatarRenderState state, PoseStack pose, float bodyRot, float scale, Operation<Void> original) {
        boolean flying = state.isFallFlying; float swim = state.swimAmount;
        if (((PlayerWalkState)state).morphmod$animationPose().replacesBodyOrientation()) { state.isFallFlying = false; state.swimAmount = 0; }
        try { original.call(state, pose, bodyRot, scale); }
        finally { state.isFallFlying = flying; state.swimAmount = swim; }
    }
}
