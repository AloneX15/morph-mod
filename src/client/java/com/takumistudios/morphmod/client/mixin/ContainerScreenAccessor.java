package com.takumistudios.morphmod.client.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Panel position of inventory screens, so the equipment tab follows the recipe book shift. */
@Mixin(AbstractContainerScreen.class)
public interface ContainerScreenAccessor {
	@Accessor("leftPos")
	int morphmod$getLeftPos();

	@Accessor("topPos")
	int morphmod$getTopPos();

	@Accessor("imageWidth")
	int morphmod$getImageWidth();
}
