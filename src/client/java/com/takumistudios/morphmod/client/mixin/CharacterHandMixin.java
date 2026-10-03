package com.takumistudios.morphmod.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.takumistudios.morphmod.client.character.CharacterRenderManager;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only replaces bare-hand geometry; vanilla keeps first-person item placement and effects. */
@Mixin(AvatarRenderer.class)
public abstract class CharacterHandMixin {
    @Inject(method = "renderRightHand", at = @At("HEAD"), cancellable = true)
    private void morphmod$right(PoseStack pose, SubmitNodeCollector tasks, int light, Identifier skin, boolean sleeve, CallbackInfo ci) {
        if (CharacterRenderManager.renderArm(pose, tasks, light, "right_arm")) ci.cancel();
    }
    @Inject(method = "renderLeftHand", at = @At("HEAD"), cancellable = true)
    private void morphmod$left(PoseStack pose, SubmitNodeCollector tasks, int light, Identifier skin, boolean sleeve, CallbackInfo ci) {
        if (CharacterRenderManager.renderArm(pose, tasks, light, "left_arm")) ci.cancel();
    }
}
