package io.github.jmarc.morph.client.hud;

import io.github.jmarc.morph.ability.AbilitySlot;
import io.github.jmarc.morph.ability.MorphAbility;
import io.github.jmarc.morph.client.ClientMorphState;
import io.github.jmarc.morph.client.MorphKeybinds;
import io.github.jmarc.morph.morph.MorphDefinition;
import io.github.jmarc.morph.morph.MorphManager;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Top-left overlay: current form and the cooldown of each ability.
 */
public final class MorphHud {
	private static final int X = 4;
	private static final int Y = 4;
	private static final int BAR_WIDTH = 80;

	private MorphHud() {
	}

	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) {
			return;
		}
		Optional<MorphDefinition> current = MorphManager.current(minecraft.player);
		if (current.isEmpty()) {
			return;
		}
		Font font = minecraft.font;
		MorphDefinition definition = current.get();
		int y = Y;
		graphics.text(font, Component.translatable("hud.morphmod.form", definition.type().getDescription()), X, y, 0xFFFFFFFF);
		y += 11;

		for (AbilitySlot slot : AbilitySlot.values()) {
			MorphAbility ability = definition.ability(slot);
			if (ability == null) {
				continue;
			}
			float progress = ClientMorphState.cooldownProgress(slot);
			Component label = Component.literal("[").append(MorphKeybinds.forSlot(slot).getTranslatedKeyMessage()).append("] ").append(ability.displayName());
			if (progress > 0) {
				label = label.copy().append(String.format(Locale.ROOT, " %.1fs", ClientMorphState.secondsLeft(slot)));
			}
			graphics.text(font, label, X, y, progress > 0 ? 0xFFA0A0A0 : 0xFF55FFFF);
			y += 10;
			graphics.fill(X, y, X + BAR_WIDTH, y + 2, 0x80000000);
			graphics.fill(X, y, X + (int) (BAR_WIDTH * (1.0F - progress)), y + 2, progress > 0 ? 0xFFAA5500 : 0xFF55FF55);
			y += 5;
		}
	}
}
