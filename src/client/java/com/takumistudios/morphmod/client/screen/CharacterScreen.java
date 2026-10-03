package com.takumistudios.morphmod.client.screen;

import com.takumistudios.morphmod.character.*;
import com.takumistudios.morphmod.client.character.*;
import com.takumistudios.morphmod.network.CharacterActionPayload;
import java.util.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

/** Searchable character/emote picker; all selections are requests to the server. */
public final class CharacterScreen extends Screen {
    private final boolean emotes;
    private String search = "", selected = "", mode = "full";
    private int page;
    private List<String> filtered = List.of();
    private final List<Button> rows = new ArrayList<>();
    private Button previous, next, select, modeButton;
    private CharacterEntity preview;
    private CharacterDefinition previewDefinition;
    private long revision = -1;
    private String ownerCharacter = "";
    @Override public void tick() {
        String id = minecraft.player == null ? "" : CharacterManager.selected(minecraft.player);
        if (revision != ClientCharacters.revision() || !ownerCharacter.equals(id)) {
            revision = ClientCharacters.revision(); ownerCharacter = id; refresh();
        }
    }
    public CharacterScreen(boolean emotes) { super(Component.translatable(emotes ? "screen.morphmod.emotes" : "screen.morphmod.characters")); this.emotes = emotes; }
    public static void open(Screen screen) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        //? if >=26.2 {
        mc.gui.setScreen(screen);
        //?} else {
        /*mc.setScreen(screen);
        *///?}
    }
    @Override protected void init() {
        rows.clear(); int leftWidth = Math.max(100, width / 2 - 16), right = width / 2 + 4, rightWidth = width - right - 8;
        addRenderableWidget(Button.builder(Component.translatable("screen.morphmod.mobs"), b -> open(new MorphSelectScreen())).bounds(8, 18, leftWidth / 2 - 2, 20).build());
        addRenderableWidget(Button.builder(Component.translatable(emotes ? "screen.morphmod.characters" : "screen.morphmod.emotes"), b -> open(new CharacterScreen(!emotes))).bounds(10 + leftWidth / 2, 18, leftWidth / 2 - 2, 20).build());
        EditBox query = addRenderableWidget(new EditBox(font, 8, 42, leftWidth, 18, Component.translatable("screen.morphmod.search")));
        query.setHint(Component.translatable("screen.morphmod.search")); query.setValue(search); query.setResponder(s -> { search = s; page = 0; refresh(); });
        int count = Math.max(1, (height - 100) / 24);
        for (int i = 0; i < count; i++) { final int index = i; rows.add(addRenderableWidget(Button.builder(Component.empty(), b -> choose(index)).bounds(8, 66 + i * 24, leftWidth, 20).build())); }
        previous = addRenderableWidget(Button.builder(Component.literal("<"), b -> { page--; refresh(); }).bounds(8, height - 26, 24, 20).build());
        next = addRenderableWidget(Button.builder(Component.literal(">"), b -> { page++; refresh(); }).bounds(36, height - 26, 24, 20).build());
        select = addRenderableWidget(Button.builder(Component.translatable(emotes ? "screen.morphmod.play" : "screen.morphmod.morph"), b -> {
            if (select.active) ClientPlayNetworking.send(new CharacterActionPayload(emotes ? "play" : "select", selected, emotes ? mode : ""));
        }).bounds(right, height - 50, rightWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable(emotes ? "screen.morphmod.stop" : "screen.morphmod.demorph"), b -> ClientPlayNetworking.send(new CharacterActionPayload(emotes ? "stop" : "clear", "", ""))).bounds(right, height - 26, rightWidth, 20).build());
        if (emotes) modeButton = addRenderableWidget(Button.builder(modeLabel(), b -> { mode = mode.equals("full") ? "overlay" : "full"; b.setMessage(modeLabel()); updatePreview(); }).bounds(right, 42, rightWidth, 20).build());
        refresh();
    }
    private Component modeLabel() { return Component.translatable("screen.morphmod.emote_mode." + mode); }
    private CharacterDefinition character() { return minecraft.player == null ? null : ClientCharacters.get(CharacterManager.selected(minecraft.player)); }
    private void refresh() {
        if (emotes) {
            var d = character(); filtered = d == null ? List.of() : d.emotes().keySet().stream().filter(id -> ClientCharacters.allowedEmote(d.id(), id)).filter(this::matches).sorted().toList();
        } else filtered = ClientCharacters.bundles().keySet().stream().filter(ClientCharacters::allowed).filter(id -> matches(id) || matches(ClientCharacters.get(id).name())).sorted().toList();
        int pages = Math.max(1, (filtered.size() + rows.size() - 1) / rows.size()); page = Math.clamp(page, 0, pages - 1);
        for (int i = 0; i < rows.size(); i++) {
            int index = page * rows.size() + i; var row = rows.get(i); row.visible = index < filtered.size();
            if (row.visible) { String id = filtered.get(index); row.setMessage(Component.literal(emotes ? id : ClientCharacters.get(id).name())); row.active = !selected.equals(id); }
        }
        previous.active = page > 0; next.active = page < pages - 1; updatePreview();
    }
    private boolean matches(String text) { return text.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)); }
    private void choose(int index) { int actual = page * rows.size() + index; if (actual < filtered.size()) { selected = filtered.get(actual); refresh(); } }
    private void updatePreview() {
        CharacterDefinition definition = emotes ? character() : ClientCharacters.get(selected);
        if (definition != previewDefinition) {
            previewDefinition = definition; preview = definition == null || minecraft.level == null ? null : new CharacterEntity(CharacterEntity.TYPE, minecraft.level);
            if (preview != null) { preview.definition = definition; preview.setId(-2_000_000); }
        }
        boolean available = definition != null && !selected.isEmpty() && ClientCharacters.allowed(definition.id());
        if (emotes && available) available = definition.emotes().containsKey(selected) && definition.emotes().get(selected).modes().contains(mode) && ClientCharacters.allowedEmote(definition.id(), selected);
        select.active = available;
        if (preview != null && minecraft.player != null) {
            preview.copy(minecraft.player); preview.locomotion = "idle";
            preview.emote = emotes && available ? new CharacterManager.EmoteState(selected, mode, preview.level().getGameTime()) : null;
        }
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tick) {
        super.extractRenderState(graphics, mouseX, mouseY, tick);
        graphics.centeredText(font, title, width / 2, 5, 0xFFFFFFFF);
        if (preview != null) {
            preview.tickCount = minecraft.player == null ? preview.tickCount + 1 : minecraft.player.tickCount;
            int right = width / 2 + 4;
            try { InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, right, 66, width - 8, height - 54, 35, 0.0625F, mouseX, mouseY, preview); }
            catch (RuntimeException | LinkageError e) { com.takumistudios.morphmod.MorphMod.LOGGER.warn("Character preview failed", e); preview = null; }
        }
        if (filtered.isEmpty()) graphics.textWithWordWrap(font, Component.translatable("screen.morphmod.characters.empty"), 8, 68, width / 2 - 16, 0xFFA0A0A0);
    }
    @Override public boolean isPauseScreen() { return false; }
}
