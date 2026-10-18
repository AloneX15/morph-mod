package com.takumistudios.morphmod.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.takumistudios.morphmod.client.render.PlayerWalkState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

/** Camera-space shoulder adapter. No imported root/waist displacement reaches the camera. */
public final class FirstPersonProfileRenderer {
    //? if >=26.2 {
    public static final String COMPAT_HAND_METHOD = "submitArmWithItem";
    //?} else {
    /*public static final String COMPAT_HAND_METHOD = "renderArmWithItem";
    *///?}
    private static final ModelPart[] ARMS = createArms();
    private static ModelPart[] createArms() {
        ModelPart[] result = new ModelPart[8];
        for (int index = 0; index < 8; index++) {
            boolean slim = (index & 4) != 0, right = (index & 2) != 0, sleeve = (index & 1) != 0;
            var mesh = PlayerModel.createMesh(new net.minecraft.client.model.geom.builders.CubeDeformation(0), slim);
            var model = new PlayerModel(mesh.getRoot().bake(64, 64), slim);
            ModelPart arm = right ? model.rightArm : model.leftArm; arm.setPos(0, 0, 0); arm.setRotation(0, 0, 0);
            (right ? model.rightSleeve : model.leftSleeve).visible = sleeve;
            result[index] = arm;
        }
        return result;
    }
    public static boolean submit(AvatarRenderState state, HumanoidArm side, ItemStack stack, ItemStackRenderState item,
                                 PoseStack pose, SubmitNodeCollector tasks, int light, float equip) {
        PlayerAnimationPose animation = ((PlayerWalkState)state).morphmod$animationPose(); boolean right = side == HumanoidArm.RIGHT;
        if (!animation.hasArm(right) || !animation.eligible || state.isInvisible || stack.has(DataComponents.MAP_ID)) return false;
        boolean slim = state.skin.model() == net.minecraft.world.entity.player.PlayerModelType.SLIM;
        boolean sleeve = right ? state.showRightSleeve : state.showLeftSleeve;
        ModelPart arm = ARMS[(slim ? 4 : 0) | (right ? 2 : 0) | (sleeve ? 1 : 0)];
        pose.pushPose();
        try {
            float sign = right ? 1 : -1;
            pose.translate(sign * 0.36F + Math.clamp(-animation.armValue(right, 3) / 16, -0.25F, 0.25F), -0.22F - equip * 0.5F + Math.clamp(animation.armValue(right, 4) / 16, -0.25F, 0.25F), -0.45F - Math.clamp(animation.armValue(right, 5) / 16, -0.25F, 0.25F));
            pose.scale(1, -1, -1);
            float x = Math.clamp((float)Math.toRadians(-animation.armValue(right, 0)) + (float)Math.PI / 4, -0.5F, 1.9F);
            float y = Math.clamp((float)Math.toRadians(-animation.armValue(right, 1)), -0.8F, 0.8F);
            float z = Math.clamp((float)Math.toRadians(animation.armValue(right, 2)), -0.8F, 0.8F);
            //? if >=26.3 {
            pose.rotate(Axis.ZP.rotation(z)); pose.rotate(Axis.YP.rotation(y)); pose.rotate(Axis.XP.rotation(x));
            //?} else {
            /*pose.mulPose(Axis.ZP.rotation(z)); pose.mulPose(Axis.YP.rotation(y)); pose.mulPose(Axis.XP.rotation(x));
            *///?}
            pose.scale(Math.clamp(animation.armValue(right, 6), 0, 2), Math.clamp(animation.armValue(right, 7), 0, 2), Math.clamp(animation.armValue(right, 8), 0, 2));
            // Deferred submission must not reference mutable third-person model parts.
            tasks.submitModelPart(arm, pose, RenderTypes.entityTranslucent(state.skin.body().texturePath()), light, OverlayTexture.NO_OVERLAY, null);
            if (!item.isEmpty()) {
                pose.translate(0, 11.0 / 16, 0);
                //? if >=26.3 {
                pose.rotate(Axis.XP.rotationDegrees(-90)); pose.rotate(Axis.YP.rotationDegrees(180));
                //?} else {
                /*pose.mulPose(Axis.XP.rotationDegrees(-90)); pose.mulPose(Axis.YP.rotationDegrees(180));
                *///?}
                item.submit(pose, tasks, light, OverlayTexture.NO_OVERLAY, 0);
            }
            return true;
        } finally { pose.popPose(); }
    }
    private FirstPersonProfileRenderer() { }
}
