package io.github.jmarc.morph.network;

import io.github.jmarc.morph.ability.AbilitySlot;
import io.github.jmarc.morph.morph.MorphManager;
import io.github.jmarc.morph.morph.MorphRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Registers payload types and the server-side handlers. Every request is validated here:
 * the client is never trusted about what it has unlocked.
 */
public final class MorphNetworking {
	private MorphNetworking() {
	}

	public static void init() {
		PayloadTypeRegistry.serverboundPlay().register(SelectMorphPayload.TYPE, SelectMorphPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(DemorphPayload.TYPE, DemorphPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(UseAbilityPayload.TYPE, UseAbilityPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(CooldownPayload.TYPE, CooldownPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SelectMorphPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			if (!player.isAlive() || player.isSpectator()) {
				return;
			}
			if (!MorphManager.isUnlocked(player, payload.entityType()) || MorphRegistry.get(payload.entityType()).isEmpty()) {
				player.sendOverlayMessage(Component.translatable("message.morphmod.locked"));
				return;
			}
			BuiltInRegistries.ENTITY_TYPE.getOptional(payload.entityType()).ifPresent(type -> MorphManager.morph(player, type));
		});

		ServerPlayNetworking.registerGlobalReceiver(DemorphPayload.TYPE, (payload, context) -> MorphManager.demorph(context.player()));

		ServerPlayNetworking.registerGlobalReceiver(UseAbilityPayload.TYPE, (payload, context) ->
			MorphManager.useAbility(context.player(), AbilitySlot.byIndex(payload.slot())));
	}
}
