package com.takumistudios.morphmod.client.screen;

import com.takumistudios.morphmod.client.mixin.ContainerScreenAccessor;
import com.takumistudios.morphmod.data.MorphAttachments;
import com.takumistudios.morphmod.network.CharacterActionPayload;
import com.takumistudios.morphmod.util.FeatureGuard;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Small tab on the right of the inventory that toggles armor and held items on the player's morph.
 * Only shown when the server says this player may use it; the server validates every toggle again.
 */
public final class EquipmentToggleTab {
	private static final int SIZE = 20;
	/** Created on first draw: item components are not bound yet when the client entrypoint runs. */
	private static ItemStack icon;
	private static java.lang.ref.WeakReference<Button> current = new java.lang.ref.WeakReference<>(null);
	private static final FeatureGuard GUARD = new FeatureGuard("equipment tab");
	private static final Tooltip SHOWN = Tooltip.create(Component.translatable("screen.morphmod.equipment.shown"));
	private static final Tooltip HIDDEN = Tooltip.create(Component.translatable("screen.morphmod.equipment.hidden"));

	public static void init() {
		ScreenEvents.AFTER_INIT.register((minecraft, screen, width, height) -> GUARD.run(() -> {
			if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) attach((AbstractContainerScreen<?>) screen);
		}));
	}

	private static void attach(AbstractContainerScreen<?> screen) {
		Button button = Button.builder(Component.empty(), b -> {
			if (ClientPlayNetworking.canSend(CharacterActionPayload.TYPE)) ClientPlayNetworking.send(new CharacterActionPayload("equipment", "", ""));
		}).size(SIZE, SIZE).build();
		int[] state = {0}; // Last tooltip shown: 0 none, 1 visible, 2 hidden.
		update(screen, button, state);
		Screens.getWidgets(screen).add(button);
		current = new java.lang.ref.WeakReference<>(button);
		ScreenEvents.afterTick(screen).register(s -> GUARD.run(() -> update(screen, button, state)));
		ScreenEvents.afterExtract(screen).register((s, graphics, mouseX, mouseY, delta) -> GUARD.run(() -> {
			if (!button.visible) return;
			if (icon == null) icon = new ItemStack(Items.DIAMOND_CHESTPLATE);
			graphics.item(icon, button.getX() + 2, button.getY() + 2);
			if (!shown()) graphics.fill(button.getX() + 2, button.getY() + 2, button.getX() + 18, button.getY() + 18, 0x99000000);
		}));
	}

	private static void update(AbstractContainerScreen<?> screen, Button button, int[] state) {
		ContainerScreenAccessor panel = (ContainerScreenAccessor) screen;
		button.setX(panel.morphmod$getLeftPos() + panel.morphmod$getImageWidth() + 2);
		button.setY(panel.morphmod$getTopPos() + 2);
		LocalPlayer player = Minecraft.getInstance().player;
		boolean allowed = player != null && Boolean.TRUE.equals(player.getAttached(MorphAttachments.CAN_TOGGLE_EQUIPMENT));
		button.visible = allowed;
		button.active = allowed;
		// The label stays empty (the icon is drawn on top); the state is described by the tooltip.
		boolean shown = shown();
		if (state[0] != (shown ? 1 : 2)) {
			state[0] = shown ? 1 : 2;
			button.setTooltip(shown ? SHOWN : HIDDEN);
		}
	}

	/** The tab of the open inventory, if any (used by tests). */
	public static @org.jspecify.annotations.Nullable Button current() {
		return current.get();
	}

	private static boolean shown() {
		LocalPlayer player = Minecraft.getInstance().player;
		return player != null && MorphAttachments.showsEquipment(player);
	}

	private EquipmentToggleTab() {
	}
}
