package com.takumistudios.morphmod.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.takumistudios.morphmod.client.animation.*;
import com.takumistudios.morphmod.client.render.PlayerWalkState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=26.3 {
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
//?} else {
/*import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
*///?}

//? if >=26.3 {
@Mixin(FirstPersonHandsAndItemsRenderer.class)
//?} else {
/*@Mixin(ItemInHandRenderer.class)
*///?}
public abstract class AnimationFirstPersonMixin {
    //? if >=26.3 {
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void morphmod$hand(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState hands, float partialTick, float pitch,
                              InteractionHand hand, float attack, ItemStack stack, float equip, PoseStack pose, SubmitNodeCollector tasks, int light, CallbackInfo ci) {
        var state = playerState.avatarRenderState;
        if (state == null || hands.isScoping) return;
        var side = hand == InteractionHand.MAIN_HAND ? state.mainArm : state.mainArm.getOpposite();
        try {
            var item = side == net.minecraft.world.entity.HumanoidArm.RIGHT ? state.rightHandItemState : state.leftHandItemState;
            if (FirstPersonProfileRenderer.submit(state, side, stack, item, pose, tasks, light, equip)) ci.cancel();
        } catch (RuntimeException | LinkageError error) { ClientAnimationProfiles.failProfile(((PlayerWalkState)state).morphmod$animationPose().profileId, error); }
    }
    //?} else {
    /*@Inject(method = FirstPersonProfileRenderer.COMPAT_HAND_METHOD, at = @At("HEAD"), cancellable = true)
    private void morphmod$hand(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float attack,
                              ItemStack stack, float equip, PoseStack pose, SubmitNodeCollector tasks, int light, CallbackInfo ci) {
        if (player.isScoping()) return;
        var mc = Minecraft.getInstance();
        var renderer = (AvatarRenderer<AbstractClientPlayer>)mc.getEntityRenderDispatcher().getRenderer(player);
        var state = renderer.createRenderState(player, partialTick);
        var side = hand == InteractionHand.MAIN_HAND ? state.mainArm : state.mainArm.getOpposite();
        var item = side == net.minecraft.world.entity.HumanoidArm.RIGHT ? state.rightHandItemState : state.leftHandItemState;
        try {
            if (FirstPersonProfileRenderer.submit(state, side, stack, item, pose, tasks, light, equip)) ci.cancel();
        } catch (RuntimeException | LinkageError error) { ClientAnimationProfiles.failProfile(((PlayerWalkState)state).morphmod$animationPose().profileId, error); }
    }
    *///?}
}
