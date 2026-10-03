package com.takumistudios.morphmod.mixin;

import com.takumistudios.morphmod.morph.MorphManager;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives a morphed player the hitbox and eye height of their mob.
 * Runs on both sides: the client needs it for its own movement and collisions.
 */
@Mixin(Avatar.class)
public abstract class AvatarMixin {
	@org.spongepowered.asm.mixin.Unique private static boolean morphmod$enabled = true;
	@Inject(method = "getDefaultDimensions", at = @At("HEAD"), cancellable = true)
	private void morphmod$useMorphDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
		if (pose == Pose.SLEEPING || pose == Pose.DYING || !((Object) this instanceof Player player)) {
			return;
		}
		if (!morphmod$enabled) return;
		try {
			var definition = MorphManager.currentDefinition(player);
			if (definition != null) cir.setReturnValue(definition.type().getDimensions());
		} catch (RuntimeException | LinkageError e) {
			morphmod$enabled = false;
			com.takumistudios.morphmod.MorphMod.LOGGER.error("Morph dimensions disabled", e);
		}
	}
}
