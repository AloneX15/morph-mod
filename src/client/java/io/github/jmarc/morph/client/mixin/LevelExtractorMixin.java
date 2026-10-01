package io.github.jmarc.morph.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.jmarc.morph.client.render.DisguiseManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Swaps a morphed player's world render state for the state of their disguise mob,
 * so the mob's own renderer draws them (model, texture, animations).
 * Only the world entity pass is touched: the local player's first-person state stays a player.
 */
@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
	@WrapOperation(
		method = "extractVisibleEntities",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"
		)
	)
	private EntityRenderState morphmod$extractDisguise(LevelExtractor extractor, Entity entity, float partialTicks, Operation<EntityRenderState> original) {
		if (entity instanceof AbstractClientPlayer player) {
			LivingEntity disguise = DisguiseManager.disguiseFor(player);
			if (disguise != null) {
				return original.call(extractor, disguise, partialTicks);
			}
		}
		return original.call(extractor, entity, partialTicks);
	}
}
