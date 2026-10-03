package com.takumistudios.morphmod.client.mixin;

import com.takumistudios.morphmod.client.character.CharacterRenderManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Inventory preview only; the camera must retain the vanilla avatar state. */
@Mixin(InventoryScreen.class)
public abstract class CharacterDispatcherMixin {
    @ModifyVariable(method = "extractEntityInInventoryFollowsMouse", at = @At("HEAD"), argsOnly = true)
    private static LivingEntity morphmod$character(LivingEntity entity) {
        if (entity instanceof AbstractClientPlayer player) {
            var proxy = CharacterRenderManager.proxy(player);
            if (proxy != null) return proxy;
        }
        return entity;
    }
}
