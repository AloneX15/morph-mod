package com.takumistudios.morphmod.client;

import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.client.hud.MorphHud;
import com.takumistudios.morphmod.client.render.DisguiseManager;
import com.takumistudios.morphmod.network.CooldownPayload;
import com.takumistudios.morphmod.network.ProtocolPayload;
import com.takumistudios.morphmod.util.FeatureGuard;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

public class MorphModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MorphKeybinds.init();
		com.takumistudios.morphmod.client.character.CharacterRenderManager.init();
		com.takumistudios.morphmod.client.character.ClientCharacters.init();
		com.takumistudios.morphmod.client.screen.EquipmentToggleTab.init();
		FeatureGuard protocol = new FeatureGuard("client protocol");
		FeatureGuard cooldown = new FeatureGuard("client cooldown packet");
		FeatureGuard keys = new FeatureGuard("key handling");
		FeatureGuard disguises = new FeatureGuard("disguise tick");
		FeatureGuard hud = new FeatureGuard("HUD");
		FeatureGuard hudUpdate = new FeatureGuard("HUD text update");
		ClientPlayNetworking.registerGlobalReceiver(ProtocolPayload.TYPE, (payload, context) -> protocol.run(() -> {
			ClientMorphState.setProtocolReady(payload.version() == ProtocolPayload.VERSION);
			if (ClientMorphState.protocolReady() && ClientPlayNetworking.canSend(ProtocolPayload.TYPE)) {
				ClientPlayNetworking.send(new ProtocolPayload(ProtocolPayload.VERSION));
			}
		}));

		ClientPlayNetworking.registerGlobalReceiver(CooldownPayload.TYPE,
			(payload, context) -> cooldown.run(() -> ClientMorphState.startCooldown(payload.slot(), payload.ticks())));

		ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
			ClientMorphState.tick();
			com.takumistudios.morphmod.client.character.CharacterRenderManager.tick(minecraft);
			keys.run(() -> MorphKeybinds.handle(minecraft));
			disguises.run(() -> DisguiseManager.tick(minecraft));
			hudUpdate.run(() -> MorphHud.update(minecraft));
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, minecraft) -> {
			DisguiseManager.clear();
			ClientMorphState.reset();
			com.takumistudios.morphmod.client.character.ClientCharacters.reset();
		});

		HudElementRegistry.addLast(MorphMod.id("hud"), (graphics, delta) -> {
			if (!hud.enabled()) return;
			try { MorphHud.extract(graphics, delta); }
			catch (RuntimeException | LinkageError e) { hud.disable(e); }
		});
	}
}
