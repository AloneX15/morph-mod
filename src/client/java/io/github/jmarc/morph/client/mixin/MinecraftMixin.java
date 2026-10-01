package io.github.jmarc.morph.client.mixin;

import io.github.jmarc.morph.morph.MorphManager;
import io.github.jmarc.morph.morph.Passive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.jspecify.annotations.Nullable;

/**
 * Warden "vibration sense": creatures moving nearby get an outline, only for the morphed player.
 * Sneaking creatures stay hidden, just like with a real Warden.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	private static final double SENSE_RANGE_SQR = 24 * 24;

	@Shadow
	public @Nullable LocalPlayer player;

	@Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
	private void morphmod$vibrationSense(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		LocalPlayer self = this.player;
		if (self == null || entity == self || !(entity instanceof LivingEntity living)) {
			return;
		}
		if (living.walkAnimation.isMoving()
			&& !living.isSteppingCarefully()
			&& self.distanceToSqr(living) < SENSE_RANGE_SQR
			&& MorphManager.has(self, Passive.VIBRATION_SENSE)) {
			cir.setReturnValue(true);
		}
	}
}
