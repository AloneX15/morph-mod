package io.github.jmarc.morph.client.screen;

import io.github.jmarc.morph.ability.AbilitySlot;
import io.github.jmarc.morph.ability.MorphAbility;
import io.github.jmarc.morph.client.MorphKeybinds;
import io.github.jmarc.morph.client.render.DisguiseManager;
import io.github.jmarc.morph.morph.MorphDefinition;
import io.github.jmarc.morph.morph.MorphManager;
import io.github.jmarc.morph.morph.MorphRegistry;
import io.github.jmarc.morph.morph.Passive;
import io.github.jmarc.morph.network.DemorphPayload;
import io.github.jmarc.morph.network.SelectMorphPayload;
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

	private final List<Button> entryButtons = new ArrayList<>();
	private final Map<Identifier, LivingEntity> previews = new HashMap<>();
	private List<Identifier> filtered = List.of();
	private String search = "";
	private int page;
	private int rows;
	private @Nullable Identifier selected;

	private Button previousPage;
	private Button nextPage;
	private Button morphButton;

	public MorphSelectScreen() {
		super(Component.translatable("screen.morphmod.title"));
	}

	@Override
	protected void init() {
		entryButtons.clear();
		int listWidth = COLUMNS * BUTTON_WIDTH + (COLUMNS - 1) * GAP;
		int left = (width - (listWidth + GAP * 4 + PANEL_WIDTH)) / 2;
		int top = 40;
		rows = Math.max(1, (height - top - 60) / (BUTTON_HEIGHT + GAP));

		EditBox searchBox = new EditBox(font, left, 18, listWidth, 18, Component.translatable("screen.morphmod.search"));
		searchBox.setHint(Component.translatable("screen.morphmod.search").withStyle(ChatFormatting.DARK_GRAY));
		searchBox.setValue(search);
		searchBox.setResponder(value -> {
			search = value;
			page = 0;
			refresh();
		});
		addRenderableWidget(searchBox);

		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				int index = row * COLUMNS + col;
				Button button = Button.builder(Component.empty(), b -> select(index))
					.bounds(left + col * (BUTTON_WIDTH + GAP), top + row * (BUTTON_HEIGHT + GAP), BUTTON_WIDTH, BUTTON_HEIGHT)
					.build();
				entryButtons.add(button);
				addRenderableWidget(button);
			}
		}

		int bottom = top + rows * (BUTTON_HEIGHT + GAP) + GAP;
		previousPage = addRenderableWidget(Button.builder(Component.literal("<"), b -> changePage(-1))
			.bounds(left, bottom, 20, BUTTON_HEIGHT).build());
		nextPage = addRenderableWidget(Button.builder(Component.literal(">"), b -> changePage(1))
			.bounds(left + listWidth - 20, bottom, 20, BUTTON_HEIGHT).build());

		// Right panel: preview, then the two buttons side by side, then the details text.
		int panelLeft = left + listWidth + GAP * 4;
		int buttonsTop = top + PREVIEW_HEIGHT + GAP;
		int halfWidth = (PANEL_WIDTH - GAP) / 2;
		morphButton = addRenderableWidget(Button.builder(Component.translatable("screen.morphmod.morph"), b -> morphIntoSelected())
			.bounds(panelLeft, buttonsTop, halfWidth, BUTTON_HEIGHT).build());
		addRenderableWidget(Button.builder(Component.translatable("screen.morphmod.demorph"), b -> demorph())
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
				button.setMessage(custom ? name.copy().withStyle(ChatFormatting.AQUA) : name);
				button.active = !id.equals(selected);
			}
		}
		previousPage.active = page > 0;
		nextPage.active = page < pages - 1;
		morphButton.active = selected != null;
	}

	private void changePage(int delta) {
		page += delta;
		refresh();
	}

	private void select(int buttonIndex) {
		int index = page * entryButtons.size() + buttonIndex;
		if (index < filtered.size()) {
			selected = filtered.get(index);
			refresh();
		}
	}

	private void morphIntoSelected() {
		if (selected != null) {
			ClientPlayNetworking.send(new SelectMorphPayload(selected));
			onClose();
		}
	}

	private void demorph() {
		ClientPlayNetworking.send(DemorphPayload.INSTANCE);
		onClose();
	}

	private static Component displayName(Identifier id) {
		return BuiltInRegistries.ENTITY_TYPE.getOptional(id).map(EntityType::getDescription).orElse(Component.literal(id.toString()));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.centeredText(font, title, width / 2, 5, TEXT_COLOR);

		if (filtered.isEmpty()) {
			Button first = entryButtons.getFirst();
			graphics.textWithWordWrap(font, Component.translatable("screen.morphmod.empty"), first.getX(), first.getY() + 4, COLUMNS * BUTTON_WIDTH, MUTED_COLOR);
		}

		int pageSize = entryButtons.size();
		int pages = Math.max(1, (filtered.size() + pageSize - 1) / pageSize);
		graphics.centeredText(font, Component.literal((page + 1) + " / " + pages),
			(previousPage.getX() + nextPage.getX() + 20) / 2, previousPage.getY() + 6, MUTED_COLOR);

		if (selected != null) {
			MorphRegistry.get(selected).ifPresent(def -> extractDetails(graphics, def, mouseX, mouseY));
		}
	}

	private void extractDetails(GuiGraphicsExtractor graphics, MorphDefinition definition, int mouseX, int mouseY) {
		int x = morphButton.getX();
		int y = entryButtons.getFirst().getY();
		int previewBottom = y + PREVIEW_HEIGHT;
		graphics.fill(x, y, x + PANEL_WIDTH, previewBottom, 0x66000000);

		LivingEntity preview = preview(definition);
		if (preview != null) {
			float size = Math.max(preview.getBbHeight(), preview.getBbWidth());
			int scale = (int) Math.clamp(60.0F / size, 8.0F, 45.0F);
			InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, x, y, x + PANEL_WIDTH, previewBottom, scale, 0.0625F, mouseX, mouseY, preview);
		}

		int line = morphButton.getBottom() + GAP;
		graphics.text(font, definition.type().getDescription(), x, line, TEXT_COLOR);
		line += 12;
		Double health = definition.stats().get(Attributes.MAX_HEALTH);
		Double damage = definition.stats().get(Attributes.ATTACK_DAMAGE);
		Component stats = Component.translatable("screen.morphmod.stats",
			health == null ? "-" : formatNumber(health),
			damage == null ? "-" : formatNumber(damage));
		graphics.text(font, stats, x, line, MUTED_COLOR);
		line += 12;

		for (AbilitySlot slot : AbilitySlot.values()) {
			MorphAbility ability = definition.ability(slot);
			if (ability != null) {
				Component text = Component.literal("[").append(MorphKeybinds.forSlot(slot).getTranslatedKeyMessage()).append("] ").append(ability.displayName());
				graphics.text(font, text, x, line, ACCENT_COLOR);
				line += 10;
			}
		}
		for (Passive passive : definition.passives()) {
			graphics.text(font, Component.literal("• ").append(Component.translatable("passive.morphmod." + passive.name().toLowerCase(Locale.ROOT))), x, line, MUTED_COLOR);
			line += 10;
		}
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
