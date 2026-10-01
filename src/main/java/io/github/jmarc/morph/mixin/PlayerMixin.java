package io.github.jmarc.morph.mixin;

import io.github.jmarc.morph.morph.MorphManager;
import io.github.jmarc.morph.morph.Passive;
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
	@Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
	private void morphmod$climbWalls(CallbackInfoReturnable<Boolean> cir) {
		Player self = (Player) (Object) this;
		if (self.horizontalCollision && !self.getAbilities().flying && MorphManager.has(self, Passive.CLIMB_WALLS)) {
			cir.setReturnValue(true);
		}
	}
}
