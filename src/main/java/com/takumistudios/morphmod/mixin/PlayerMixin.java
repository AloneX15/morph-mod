package com.takumistudios.morphmod.mixin;

import com.takumistudios.morphmod.morph.MorphManager;
import com.takumistudios.morphmod.morph.Passive;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Spider-style wall climbing: touching a wall counts as being on a ladder.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {
	@org.spongepowered.asm.mixin.Unique private static boolean morphmod$enabled = true;
	@Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
	private void morphmod$climbWalls(CallbackInfoReturnable<Boolean> cir) {
		Player self = (Player) (Object) this;
		if (!morphmod$enabled) return;
		try {
			if (self.horizontalCollision && !self.getAbilities().flying && MorphManager.has(self, Passive.CLIMB_WALLS)) cir.setReturnValue(true);
		} catch (RuntimeException | LinkageError e) {
			morphmod$enabled = false;
			com.takumistudios.morphmod.MorphMod.LOGGER.error("Morph climbing disabled", e);
		}
	}
}
