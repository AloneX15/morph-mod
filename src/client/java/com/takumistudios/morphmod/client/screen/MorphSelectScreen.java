package com.takumistudios.morphmod.client.screen;

import com.takumistudios.morphmod.ability.AbilitySlot;
import com.takumistudios.morphmod.ability.MorphAbility;
import com.takumistudios.morphmod.client.MorphKeybinds;
import com.takumistudios.morphmod.client.ClientMorphState;
import com.takumistudios.morphmod.util.FeatureGuard;
import com.takumistudios.morphmod.client.render.DisguiseManager;
import com.takumistudios.morphmod.morph.MorphDefinition;
import com.takumistudios.morphmod.morph.MorphManager;
import com.takumistudios.morphmod.morph.MorphRegistry;
import com.takumistudios.morphmod.morph.Passive;
import com.takumistudios.morphmod.network.DemorphPayload;
import com.takumistudios.morphmod.network.SelectMorphPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jspecify.annotations.Nullable;

/**
 * Morph menu (default key M): searchable, paged list of unlocked forms with a 3D preview
 * and a summary of stats, passives and abilities.
 */
public class MorphSelectScreen extends Screen {
	private static final int COLUMNS = 2;
	private static final int BUTTON_WIDTH = 110;
	private static final int BUTTON_HEIGHT = 20;
	private static final int GAP = 4;
	private static final int PANEL_WIDTH = 160;
	private static final int PREVIEW_HEIGHT = 64;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int MUTED_COLOR = 0xFFA0A0A0;
	private static final int ACCENT_COLOR = 0xFF55FFFF;
	private final FeatureGuard actions = new FeatureGuard("morph menu actions");
	private final FeatureGuard rendering = new FeatureGuard("morph menu preview");

	private final List<Button> entryButtons = new ArrayList<>();
	private final Map<Identifier, LivingEntity> previews = new java.util.LinkedHashMap<>(16, 0.75F, true) {
		@Override protected boolean removeEldestEntry(Map.Entry<Identifier, LivingEntity> entry) { return size() > 16; }
	};
	private List<Identifier> filtered = List.of();
	private String search = "";
	private int page;
	private int rows;
	private int columns = COLUMNS;
	private int buttonWidth = BUTTON_WIDTH;
	private int panelWidth = PANEL_WIDTH;
	private Component pageLabel = Component.empty();
	private MorphDefinition selectedDefinition;
	private LivingEntity selectedPreview;
	private int detailScroll;
	private final List<net.minecraft.util.FormattedCharSequence> detailLines = new ArrayList<>();
	private static final Component EMPTY = Component.translatable("screen.morphmod.empty");
	private @Nullable Identifier selected;

	private Button previousPage;
	private Button nextPage;
	private Button morphButton;

	public MorphSelectScreen() {
		super(Component.translatable("screen.morphmod.title"));
	}

	@Override
	protected void init() {
		actions.run(this::initializeWidgets);
	}
	private void initializeWidgets() {
		entryButtons.clear();
		columns = width < 450 ? 1 : COLUMNS;
		panelWidth = Math.min(PANEL_WIDTH, Math.max(130, width / 2 - 16));
		buttonWidth = Math.min(BUTTON_WIDTH, Math.max(80, (width - panelWidth - GAP * 6) / columns));
		int listWidth = columns * buttonWidth + (columns - 1) * GAP;
		int left = Math.max(4, (width - (listWidth + GAP * 4 + panelWidth)) / 2);
		int top = 40;
		rows = Math.max(1, (height - top - 60) / (BUTTON_HEIGHT + GAP));

		EditBox searchBox = new EditBox(font, left, 18, listWidth, 18, Component.translatable("screen.morphmod.search"));
		searchBox.setHint(Component.translatable("screen.morphmod.search").withStyle(ChatFormatting.DARK_GRAY));
		searchBox.setValue(search);
		searchBox.setResponder(value -> {
			search = value;
			page = 0;
			actions.run(this::refresh);
		});
		addRenderableWidget(searchBox);

		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < columns; col++) {
				int index = row * columns + col;
				Button button = Button.builder(Component.empty(), b -> actions.run(() -> select(index)))
					.bounds(left + col * (buttonWidth + GAP), top + row * (BUTTON_HEIGHT + GAP), buttonWidth, BUTTON_HEIGHT)
					.build();
				entryButtons.add(button);
				addRenderableWidget(button);
			}
		}

		int bottom = top + rows * (BUTTON_HEIGHT + GAP) + GAP;
		previousPage = addRenderableWidget(Button.builder(Component.translatable("screen.morphmod.previous"), b -> actions.run(() -> changePage(-1)))
			.bounds(left, bottom, 20, BUTTON_HEIGHT).build());
		nextPage = addRenderableWidget(Button.builder(Component.translatable("screen.morphmod.next"), b -> actions.run(() -> changePage(1)))
			.bounds(left + listWidth - 20, bottom, 20, BUTTON_HEIGHT).build());

		// Right panel: preview, then the two buttons side by side, then the details text.
		int panelLeft = left + listWidth + GAP * 4;
		addRenderableWidget(Button.builder(Component.translatable("screen.morphmod.characters"), b -> CharacterScreen.open(new CharacterScreen(false)))
			.bounds(panelLeft, 18, panelWidth, BUTTON_HEIGHT).build());
		int buttonsTop = top + PREVIEW_HEIGHT + GAP;
		int halfWidth = (panelWidth - GAP) / 2;
		morphButton = addRenderableWidget(Button.builder(Component.translatable("screen.morphmod.morph"), b -> actions.run(this::morphIntoSelected))
			.bounds(panelLeft, buttonsTop, halfWidth, BUTTON_HEIGHT).build());
		addRenderableWidget(Button.builder(Component.translatable("screen.morphmod.demorph"), b -> actions.run(this::demorph))
			.bounds(panelLeft + halfWidth + GAP, buttonsTop, halfWidth, BUTTON_HEIGHT).build());

		if (selected == null && minecraft.player != null) {
			MorphManager.current(minecraft.player).ifPresent(def -> selected = BuiltInRegistries.ENTITY_TYPE.getKey(def.type()));
		}
		setInitialFocus(searchBox);
		refresh();
	}

	private void refresh() {
		List<Identifier> unlocked = minecraft.player == null ? List.of() : MorphManager.unlocked(minecraft.player);
		String query = search.trim().toLowerCase(Locale.ROOT);
		filtered = unlocked.stream()
			.filter(id -> MorphRegistry.get(id).isPresent())
			.filter(id -> query.isEmpty() || id.toString().contains(query) || displayName(id).getString().toLowerCase(Locale.ROOT).contains(query))
			.toList();

		int pageSize = entryButtons.size();
		int pages = Math.max(1, (filtered.size() + pageSize - 1) / pageSize);
		page = Math.clamp(page, 0, pages - 1);
		for (int i = 0; i < pageSize; i++) {
			int index = page * pageSize + i;
			Button button = entryButtons.get(i);
			button.visible = index < filtered.size();
			if (button.visible) {
				Identifier id = filtered.get(index);
				boolean custom = MorphRegistry.get(id).map(def -> MorphRegistry.hasCustomDefinition(def.type())).orElse(false);
				Component name = displayName(id);
				button.setMessage(custom ? Component.translatable("screen.morphmod.custom", name).withStyle(ChatFormatting.AQUA) : name);
				button.active = !id.equals(selected);
			}
		}
		previousPage.active = page > 0;
		nextPage.active = page < pages - 1;
		morphButton.active = selected != null;
		pageLabel = Component.translatable("screen.morphmod.page", page + 1, pages);
		cacheDetails();
	}

	private void cacheDetails() {
		detailScroll = 0;
		selectedDefinition = selected == null ? null : MorphRegistry.get(selected).orElse(null);
		selectedPreview = selectedDefinition == null ? null : preview(selectedDefinition);
		detailLines.clear();
		if (selectedDefinition == null) return;
		MorphDefinition def = selectedDefinition;
		addDetail(def.type().getDescription());
		Double health = def.stats().get(Attributes.MAX_HEALTH);
		Double damage = def.stats().get(Attributes.ATTACK_DAMAGE);
		addDetail(Component.translatable("screen.morphmod.stats", health == null ? "-" : formatNumber(health), damage == null ? "-" : formatNumber(damage)));
		for (AbilitySlot slot : AbilitySlot.values()) {
			MorphAbility ability = def.ability(slot);
			if (ability != null) addDetail(Component.translatable("hud.morphmod.ability", MorphKeybinds.forSlot(slot).getTranslatedKeyMessage(), ability.displayName()).withStyle(ChatFormatting.AQUA));
		}
		for (Passive passive : def.passives()) addDetail(Component.translatable("screen.morphmod.passive", Component.translatable("passive.morphmod." + passive.name().toLowerCase(Locale.ROOT))));
	}
	private void addDetail(Component text) { detailLines.addAll(font.split(text, panelWidth - 4)); }

	private void changePage(int delta) {
		page += delta;
		refresh();
	}

	private void select(int buttonIndex) {
		int index = page * entryButtons.size() + buttonIndex;
		if (index < filtered.size()) {
			selected = filtered.get(index);
			actions.run(this::refresh);
		}
	}

	private void morphIntoSelected() {
		if (selected != null && ClientMorphState.protocolReady() && ClientPlayNetworking.canSend(SelectMorphPayload.TYPE)) {
			ClientPlayNetworking.send(new SelectMorphPayload(selected));
			onClose();
		}
	}

	private void demorph() {
		if (ClientMorphState.protocolReady() && ClientPlayNetworking.canSend(DemorphPayload.TYPE)) ClientPlayNetworking.send(DemorphPayload.INSTANCE);
		onClose();
	}

	private static Component displayName(Identifier id) {
		return BuiltInRegistries.ENTITY_TYPE.getOptional(id).map(EntityType::getDescription).orElse(Component.literal(id.toString()));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		if (entryButtons.isEmpty()) return;
		graphics.centeredText(font, title, width / 2, 5, TEXT_COLOR);

		if (filtered.isEmpty()) {
			Button first = entryButtons.getFirst();
			graphics.textWithWordWrap(font, EMPTY, first.getX(), first.getY() + 4, columns * buttonWidth, MUTED_COLOR);
		}

		int pageSize = entryButtons.size();
		int pages = Math.max(1, (filtered.size() + pageSize - 1) / pageSize);
		graphics.centeredText(font, pageLabel,
			(previousPage.getX() + nextPage.getX() + 20) / 2, previousPage.getY() + 6, MUTED_COLOR);

		if (selectedDefinition != null && rendering.enabled()) {
			try { extractDetails(graphics, selectedDefinition, mouseX, mouseY); }
			catch (RuntimeException | LinkageError e) { rendering.disable(e); }
		}
	}

	private void extractDetails(GuiGraphicsExtractor graphics, MorphDefinition definition, int mouseX, int mouseY) {
		int x = morphButton.getX();
		int y = entryButtons.getFirst().getY();
		int previewBottom = y + PREVIEW_HEIGHT;
		graphics.fill(x, y, x + panelWidth, previewBottom, 0xCC000000);

		LivingEntity preview = selectedPreview;
		if (preview != null) {
			float size = Math.max(preview.getBbHeight(), preview.getBbWidth());
			int scale = (int) Math.clamp(60.0F / size, 8.0F, 45.0F);
			InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, x, y, x + panelWidth, previewBottom, scale, 0.0625F, mouseX, mouseY, preview);
		}

		int line = morphButton.getBottom() + GAP;
        graphics.enableScissor(x, line, x + panelWidth, height - 4);
        for (int i = detailScroll; i < detailLines.size(); i++) {
            graphics.text(font, detailLines.get(i), x, line, TEXT_COLOR);
            line += 10;
        }
        graphics.disableScissor();
    }

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (morphButton != null && mouseX >= morphButton.getX() && mouseY >= morphButton.getBottom()) {
			int visible = Math.max(1, (height - 4 - morphButton.getBottom() - GAP) / 10);
			detailScroll = Math.clamp(detailScroll - (int) Math.signum(vertical), 0, Math.max(0, detailLines.size() - visible));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
	}

	private @Nullable LivingEntity preview(MorphDefinition definition) {
		if (minecraft.level == null) {
			return null;
		}
		Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(definition.type());
		return previews.computeIfAbsent(id, key -> DisguiseManager.create(definition, minecraft.level));
	}

	private static String formatNumber(double value) {
		return value == Math.floor(value) ? Integer.toString((int) value) : String.format(Locale.ROOT, "%.1f", value);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
