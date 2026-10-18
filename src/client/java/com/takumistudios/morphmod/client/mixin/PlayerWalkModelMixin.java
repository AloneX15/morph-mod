package com.takumistudios.morphmod.client.mixin;

import com.takumistudios.morphmod.character.PlayerLocomotion;
import com.takumistudios.morphmod.client.render.PlayerWalkState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class PlayerWalkModelMixin {
    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;

    // Replace just the locomotion baseline, BEFORE item poses, swings, crouching and swimming.
    // Armor uses the same state and setupAnim, so equipment follows the new limb movement.
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;isPassenger:Z", ordinal = 0))
    private void morphmod$walk(HumanoidRenderState state, CallbackInfo ci) {
        if (!com.takumistudios.morphmod.client.render.PlayerAnimationConfig.enabled()
            || !(state instanceof PlayerWalkState walk) || !walk.morphmod$walkEnabled()
            || state.isFallFlying || state.swimAmount > 0 || state.isAutoSpinAttack || state.deathTime > 0) return;
        float age = state.ageInTicks, speed = walk.morphmod$groundSpeed();
        boolean crouch = state.isCrouching;
        // Vanilla adds +0.4 later for crouching; compensate to retain the source's 10 degree pose.
        rightArm.xRot = PlayerLocomotion.armPitch(age, speed, crouch, true) - (crouch ? 0.4F : 0);
        leftArm.xRot = PlayerLocomotion.armPitch(age, speed, crouch, false) - (crouch ? 0.4F : 0);
        rightArm.zRot = PlayerLocomotion.radians(crouch ? 5 : 2);
        leftArm.zRot = -rightArm.zRot;
        rightLeg.xRot = PlayerLocomotion.legPitch(age, speed, crouch, true);
        leftLeg.xRot = PlayerLocomotion.legPitch(age, speed, crouch, false);
        rightLeg.yRot = PlayerLocomotion.radians(crouch ? -1 : -2);
        leftLeg.yRot = -rightLeg.yRot;
        rightLeg.zRot = PlayerLocomotion.radians(crouch ? 0 : 1);
        leftLeg.zRot = -rightLeg.zRot;
        rightLeg.x += crouch ? 0.1F : 0.15F;
        leftLeg.x -= crouch ? 0.1F : 0.15F;
        if (!crouch) {
            body.zRot = PlayerLocomotion.roll(age, speed, false);
            head.zRot = PlayerLocomotion.roll(age, speed, true);
        }
    }
}
