package com.takumistudios.morphmod.client.mixin;

import com.takumistudios.morphmod.client.render.PlayerWalkState;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Wings use their own model instead of HumanoidModel; attach its root to the imported torso. */
@Mixin(ElytraModel.class)
public abstract class AnimationWingsMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void morphmod$wings(HumanoidRenderState state, CallbackInfo ci) {
        if (state instanceof PlayerWalkState walk) walk.morphmod$animationPose().applyEquipmentRoot(((ElytraModel)(Object)this).root());
    }
}
