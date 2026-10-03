package com.takumistudios.morphmod.client.hud;

import com.takumistudios.morphmod.ability.AbilitySlot;
import com.takumistudios.morphmod.ability.MorphAbility;
import com.takumistudios.morphmod.client.ClientMorphState;
import com.takumistudios.morphmod.client.MorphKeybinds;
import com.takumistudios.morphmod.morph.MorphDefinition;
import com.takumistudios.morphmod.morph.MorphManager;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Text is rebuilt on state changes during ticks, never during extraction. */
public final class MorphHud {
    private static final AbilitySlot[] SLOTS = AbilitySlot.values();
    private static final Component[] LABELS = new Component[SLOTS.length];
    private static final String[] LABEL_KEYS = new String[SLOTS.length];
    private static final float[] PROGRESS = new float[SLOTS.length];
    private static MorphDefinition cached;
    private static com.takumistudios.morphmod.character.CharacterDefinition cachedCharacter;
    private static Component formLabel;
    private MorphHud() { }

    public static void update(Minecraft minecraft) {
        MorphDefinition definition = minecraft.player == null ? null : MorphManager.current(minecraft.player).orElse(null);
        var character = minecraft.player == null ? null : com.takumistudios.morphmod.client.character.ClientCharacters.get(com.takumistudios.morphmod.character.CharacterManager.selected(minecraft.player));
        if (definition != cached || character != cachedCharacter) {
            cached = definition;
            cachedCharacter = character;
            formLabel = character != null ? Component.translatable("hud.morphmod.form", Component.literal(character.name())) : definition == null ? null : Component.translatable("hud.morphmod.form", definition.type().getDescription());
            Arrays.fill(LABEL_KEYS, null);
            Arrays.fill(LABELS, null);
        }
        if (definition == null) return;
        for (AbilitySlot slot : SLOTS) {
            int i = slot.ordinal();
            MorphAbility ability = definition.ability(slot);
            if (ability == null) { LABELS[i] = null; continue; }
            PROGRESS[i] = ClientMorphState.cooldownProgress(slot);
            int tenths = (int) Math.ceil(ClientMorphState.secondsLeft(slot) * 10);
            String key = MorphKeybinds.forSlot(slot).saveString() + ":" + tenths;
            if (!key.equals(LABEL_KEYS[i])) {
                LABEL_KEYS[i] = key;
                Component label = Component.translatable("hud.morphmod.ability", MorphKeybinds.forSlot(slot).getTranslatedKeyMessage(), ability.displayName());
                LABELS[i] = tenths > 0 ? Component.translatable("hud.morphmod.cooldown", label, String.format(Locale.ROOT, "%.1f", tenths / 10.0)) : label;
            }
        }
    }
    public static void extract(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (formLabel == null) return;
        Font font = Minecraft.getInstance().font;
        int y = 4;
        graphics.text(font, formLabel, 4, y, 0xFFFFFFFF);
        y += 11;
        for (AbilitySlot slot : SLOTS) {
            Component label = LABELS[slot.ordinal()];
            if (label == null) continue;
            float progress = PROGRESS[slot.ordinal()];
            graphics.text(font, label, 4, y, progress > 0 ? 0xFFCCCCCC : 0xFF55FFFF);
            y += 10;
            graphics.fill(4, y, 84, y + 2, 0x80000000);
            graphics.fill(4, y, 4 + (int) (80 * (1.0F - progress)), y + 2, progress > 0 ? 0xFFAA5500 : 0xFF55FF55);
            y += 5;
        }
    }
}
