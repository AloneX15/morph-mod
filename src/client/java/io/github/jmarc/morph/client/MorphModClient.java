package io.github.jmarc.morph.client;

import io.github.jmarc.morph.MorphMod;
import io.github.jmarc.morph.client.hud.MorphHud;
import io.github.jmarc.morph.client.render.DisguiseManager;
import io.github.jmarc.morph.network.CooldownPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

public class MorphModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MorphKeybinds.init();

		ClientPlayNetworking.registerGlobalReceiver(CooldownPayload.TYPE,
			(payload, context) -> ClientMorphState.startCooldown(payload.slot(), payload.ticks()));

		ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
			ClientMorphState.tick();
			MorphKeybinds.handle(minecraft);
			DisguiseManager.tick(minecraft);
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, minecraft) -> {
			DisguiseManager.clear();
			ClientMorphState.reset();
		});

		HudElementRegistry.addLast(MorphMod.id("hud"), MorphHud::extract);
	}
}
