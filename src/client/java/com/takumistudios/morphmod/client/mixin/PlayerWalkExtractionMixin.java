package com.takumistudios.morphmod.client.mixin;

import com.takumistudios.morphmod.character.PlayerLocomotion;
import com.takumistudios.morphmod.client.render.PlayerWalkState;
import com.takumistudios.morphmod.data.MorphAttachments;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class PlayerWalkExtractionMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void morphmod$walk(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        PlayerWalkState walk = (PlayerWalkState)state;
        // Render states are reused: reset even for mannequins and fallback morph renders.
        walk.morphmod$setWalk(false, 0);
        if (!(avatar instanceof Player player) || !player.onGround() || player.onClimbable()
            || player.isInWater() || player.isPassenger() || player.isSleeping() || player.isSpectator()
            || player.getAttached(MorphAttachments.CURRENT_MORPH) != null || player.getAttached(MorphAttachments.CURRENT_CHARACTER) != null) return;
        double dx = player.getX() - player.xo, dz = player.getZ() - player.zo;
        walk.morphmod$setWalk(true, PlayerLocomotion.speed(Math.hypot(dx, dz)));
    }
}
