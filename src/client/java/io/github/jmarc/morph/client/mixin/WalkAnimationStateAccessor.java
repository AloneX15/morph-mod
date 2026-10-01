package io.github.jmarc.morph.client.mixin;

import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets the disguise copy the player's limb swing exactly. */
@Mixin(WalkAnimationState.class)
public interface WalkAnimationStateAccessor {
	@Accessor("speedOld")
	float morphmod$getSpeedOld();

	@Accessor("speedOld")
	void morphmod$setSpeedOld(float value);

	@Accessor("speed")
	float morphmod$getSpeed();

	@Accessor("speed")
	void morphmod$setSpeed(float value);

	@Accessor("position")
	float morphmod$getPosition();

	@Accessor("position")
	void morphmod$setPosition(float value);
}
