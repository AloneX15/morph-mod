package com.takumistudios.morphmod.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.takumistudios.morphmod.client.render.DisguiseManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
//? if >=26.2 {
import net.minecraft.client.renderer.extract.LevelExtractor;
//?} else {
/*import net.minecraft.client.renderer.LevelRenderer;
*///?}
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Swaps a morphed player's world render state for the state of their disguise mob,
 * so the mob's own renderer draws them (model, texture, animations).
 * Only the world entity pass is touched: the local player's first-person state stays a player.
 */
//? if >=26.2 {
@Mixin(LevelExtractor.class)
//?} else {
/*@Mixin(LevelRenderer.class)
*///?}
public abstract class LevelExtractorMixin {
	@org.spongepowered.asm.mixin.Unique private static boolean morphmod$enabled = true;
	@WrapOperation(
		method = "extractVisibleEntities",
		at = @At(
			value = "INVOKE",
			//? if >=26.2 {
			target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"
			//?} else {
			/*target = "Lnet/minecraft/client/renderer/LevelRenderer;extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"
			*///?}
		)
	)
	//? if >=26.2 {
	private EntityRenderState morphmod$extractDisguise(LevelExtractor extractor, Entity entity, float partialTicks, Operation<EntityRenderState> original) {
	//?} else {
	/*private EntityRenderState morphmod$extractDisguise(LevelRenderer extractor, Entity entity, float partialTicks, Operation<EntityRenderState> original) {
	*///?}
		if (morphmod$enabled && entity instanceof AbstractClientPlayer player) {
			try {
			LivingEntity disguise = DisguiseManager.disguiseFor(player);
			if (disguise != null) {
				return original.call(extractor, disguise, partialTicks);
			}
			} catch (RuntimeException | LinkageError e) {
				morphmod$enabled = false;
				com.takumistudios.morphmod.MorphMod.LOGGER.error("Morph world rendering disabled; using player model", e);
			}
		}
		return original.call(extractor, entity, partialTicks);
	}
}
