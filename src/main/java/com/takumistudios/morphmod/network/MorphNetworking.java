package com.takumistudios.morphmod.network;

import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.ability.AbilitySlot;
import com.takumistudios.morphmod.morph.MorphManager;
import com.takumistudios.morphmod.morph.MorphRegistry;
import com.takumistudios.morphmod.util.FeatureGuard;
import com.takumistudios.morphmod.util.RequestLimiter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Validates C2S requests; unsupported clients retain the command interface. */
public final class MorphNetworking {
	private static final RequestLimiter REQUESTS = new RequestLimiter(8, 20);
	private static final Set<UUID> NEGOTIATED = new HashSet<>();
	private static final FeatureGuard SELECT = new FeatureGuard("select morph packet");
	private static final FeatureGuard DEMORPH = new FeatureGuard("demorph packet");
	private static final FeatureGuard ABILITY = new FeatureGuard("ability packet");
	private static final FeatureGuard PROTOCOL = new FeatureGuard("protocol negotiation");
	private MorphNetworking() { }

	public static void init() {
		PayloadTypeRegistry.serverboundPlay().register(SelectMorphPayload.TYPE, SelectMorphPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(DemorphPayload.TYPE, DemorphPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(UseAbilityPayload.TYPE, UseAbilityPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(CooldownPayload.TYPE, CooldownPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ProtocolPayload.TYPE, ProtocolPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ProtocolPayload.TYPE, ProtocolPayload.CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> PROTOCOL.run(() -> {
			forget(handler.player);
			if (ServerPlayNetworking.canSend(handler.player, ProtocolPayload.TYPE)) {
				ServerPlayNetworking.send(handler.player, new ProtocolPayload(ProtocolPayload.VERSION));
			}
		}));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> forget(handler.player));
		ServerPlayNetworking.registerGlobalReceiver(ProtocolPayload.TYPE, (payload, context) -> PROTOCOL.run(() -> {
			ServerPlayer player = context.player();
			if (!REQUESTS.allow(player.getUUID(), player.level().getServer().getTickCount())) return;
			if (payload.version() == ProtocolPayload.VERSION) NEGOTIATED.add(player.getUUID());
			else {
				NEGOTIATED.remove(player.getUUID());
				MorphMod.LOGGER.debug("Ignoring incompatible protocol {} from {}", payload.version(), player.getUUID());
			}
		}));
		ServerPlayNetworking.registerGlobalReceiver(SelectMorphPayload.TYPE, (payload, context) -> SELECT.run(() -> {
			ServerPlayer player = context.player();
			if (!accept(player)) return;
			if (payload.entityType() == null || MorphRegistry.get(payload.entityType()).isEmpty()) {
				MorphMod.LOGGER.debug("Ignoring invalid morph from {}", player.getUUID());
				return;
			}
			if (!MorphManager.isUnlocked(player, payload.entityType())) {
				player.sendOverlayMessage(Component.translatable("message.morphmod.locked"));
				return;
			}
			BuiltInRegistries.ENTITY_TYPE.getOptional(payload.entityType()).ifPresent(type -> MorphManager.morph(player, type));
		}));
		ServerPlayNetworking.registerGlobalReceiver(DemorphPayload.TYPE, (payload, context) -> DEMORPH.run(() -> {
			if (accept(context.player())) MorphManager.demorph(context.player());
		}));
		ServerPlayNetworking.registerGlobalReceiver(UseAbilityPayload.TYPE, (payload, context) -> ABILITY.run(() -> {
			if (!accept(context.player())) return;
			if (payload.slot() < 0 || payload.slot() >= 2) {
				MorphMod.LOGGER.debug("Ignoring invalid ability slot {}", payload.slot());
				return;
			}
			MorphManager.useAbility(context.player(), AbilitySlot.byIndex(payload.slot()));
		}));
	}
	private static boolean accept(ServerPlayer player) {
		if (!NEGOTIATED.contains(player.getUUID()) || !player.isAlive() || player.isSpectator()
			|| !REQUESTS.allow(player.getUUID(), player.level().getServer().getTickCount())) {
			MorphMod.LOGGER.debug("Ignoring unnegotiated, inactive or rate-limited request from {}", player.getUUID());
			return false;
		}
		return true;
	}
	public static boolean negotiated(ServerPlayer player) { return NEGOTIATED.contains(player.getUUID()); }
	public static void forget(ServerPlayer player) {
		NEGOTIATED.remove(player.getUUID());
		REQUESTS.forget(player.getUUID());
	}
}
