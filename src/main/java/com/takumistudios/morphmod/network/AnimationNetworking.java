package com.takumistudios.morphmod.network;

import com.google.gson.*;
import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.animation.*;
import com.takumistudios.morphmod.character.CharacterPermissions;
import com.takumistudios.morphmod.util.FeatureGuard;
import com.takumistudios.morphmod.util.RequestLimiter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class AnimationNetworking {
    private record Offer(String revision, Map<String, AnimationProfile> profiles) { }
    private static final Map<UUID, Offer> OFFERS = new HashMap<>();
    private static final Map<UUID, Transfer> TRANSFERS = new HashMap<>();
    private static final Map<UUID, AnimationSettings.Selection> SELECTED = new HashMap<>();
    private static final FeatureGuard GUARD = new FeatureGuard("animation profiles");
    private static final RequestLimiter ACKS = new RequestLimiter(8, 20);
    private static String forced;
    public static void init() {
        PayloadTypeRegistry.clientboundPlay().register(AnimationPayload.TYPE, AnimationPayload.CODEC.cast());
        PayloadTypeRegistry.serverboundPlay().register(AnimationPayload.TYPE, AnimationPayload.CODEC);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            forced = null; SELECTED.clear(); OFFERS.clear(); TRANSFERS.clear();
            try { AnimationCatalog.publish(AnimationCatalog.load().join()); }
            catch (RuntimeException e) { AnimationCatalog.publish(new AnimationCatalog.Snapshot(Map.of(), AnimationSettings.defaults())); MorphMod.LOGGER.error("Animation catalog unavailable; using legacy default", e); }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> { SELECTED.keySet().forEach(ACKS::forget); OFFERS.clear(); TRANSFERS.clear(); SELECTED.clear(); forced = null; });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> GUARD.run(() -> { offer(handler.player); refresh(handler.player); }));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> { UUID id = handler.player.getUUID(); OFFERS.remove(id); TRANSFERS.remove(id); SELECTED.remove(id); ACKS.forget(id); });
        EntityTrackingEvents.START_TRACKING.register((entity, viewer) -> GUARD.run(() -> { if (entity instanceof ServerPlayer player) sendSelection(viewer, player); }));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!GUARD.enabled()) return;
            try {
                if (server.getTickCount() % 20 == 0) refreshAll(server);
                var iterator = TRANSFERS.entrySet().iterator();
                while (iterator.hasNext()) {
                    var entry = iterator.next(); ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                    if (player == null || entry.getValue().pump(player)) iterator.remove();
                }
            } catch (RuntimeException | LinkageError e) { GUARD.disable(e); }
        });
        ServerPlayNetworking.registerGlobalReceiver(AnimationPayload.TYPE, (payload, context) -> {
            // A single acknowledgement is accepted for each server-generated revision.
            if (payload.stage() != 4) return;
            if (!ACKS.allow(context.player().getUUID(), context.player().level().getServer().getTickCount())) return;
            UUID id = context.player().getUUID(); Offer offered = OFFERS.get(id);
            if (offered == null || !offered.revision.equals(payload.hash())) return;
            try {
                JsonArray hashes = JsonParser.parseString(new String(payload.data(), StandardCharsets.UTF_8)).getAsJsonArray();
                if (hashes.size() > 128) return;
                Set<String> cached = new HashSet<>();
                for (JsonElement hash : hashes) { String value = hash.getAsString(); if (!value.matches("[a-f0-9]{64}")) return; cached.add(value); }
                OFFERS.remove(id); TRANSFERS.put(id, new Transfer(offered, cached));
            } catch (RuntimeException e) { MorphMod.LOGGER.debug("Ignored invalid animation cache acknowledgement from {}", id); }
        });
    }
    public static AnimationSettings.Selection selection(ServerPlayer player) {
        return AnimationCatalog.current().settings().resolve(player.getUUID(), node -> CharacterPermissions.check(player, node, false), AnimationCatalog::exists, forced);
    }
    public static void refreshAll(MinecraftServer server) { server.getPlayerList().getPlayers().forEach(AnimationNetworking::refresh); }
    public static void force(MinecraftServer server, String profile) { forced = profile; refreshAll(server); }
    public static void refresh(ServerPlayer player) {
        var selection = selection(player); var old = SELECTED.put(player.getUUID(), selection);
        if (old == null || !old.profile().equals(selection.profile())) {
            sendSelection(player, player);
            PlayerLookup.tracking(player).forEach(viewer -> sendSelection(viewer, player));
        }
    }
    private static void sendSelection(ServerPlayer viewer, ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(viewer, AnimationPayload.TYPE)) return;
        JsonObject json = new JsonObject(); json.addProperty("player", player.getUUID().toString()); json.addProperty("profile", selection(player).profile());
        ServerPlayNetworking.send(viewer, AnimationPayload.signal(5, "", json.toString().getBytes(StandardCharsets.UTF_8)));
    }
    public static void offerAll(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(player -> { offer(player); sendSelection(player, player); PlayerLookup.tracking(player).forEach(viewer -> sendSelection(viewer, player)); });
    }
    public static void offer(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, AnimationPayload.TYPE)) return;
        Map<String, AnimationProfile> snapshot = AnimationCatalog.current().profiles(); String revision = UUID.randomUUID().toString();
        byte[] metadata = metadata(snapshot);
        if (metadata.length > 32768) throw new IllegalArgumentException("Animation catalog metadata exceeds limit");
        OFFERS.put(player.getUUID(), new Offer(revision, snapshot)); TRANSFERS.remove(player.getUUID());
        ServerPlayNetworking.send(player, AnimationPayload.signal(0, revision, metadata));
    }
    public static byte[] metadata(Map<String, AnimationProfile> profiles) {
        JsonArray json = new JsonArray();
        profiles.values().stream().sorted(Comparator.comparing(AnimationProfile::id)).forEach(profile -> {
            JsonObject entry = new JsonObject(); entry.addProperty("id", profile.id()); entry.addProperty("hash", profile.hash()); entry.addProperty("size", profile.data().length); json.add(entry);
        });
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }
    private static final class Transfer {
        private final Offer offer;
        private final Iterator<AnimationProfile> remaining;
        private AnimationProfile profile;
        private int offset;
        Transfer(Offer offer, Set<String> cached) { this.offer = offer; remaining = offer.profiles.values().stream().filter(p -> !cached.contains(p.hash())).iterator(); }
        boolean pump(ServerPlayer player) {
            for (int budget = 0; budget < 8; budget++) {
                if (profile == null) {
                    if (!remaining.hasNext()) { ServerPlayNetworking.send(player, AnimationPayload.signal(3, offer.revision, new byte[0])); return true; }
                    profile = remaining.next(); offset = 0;
                }
                byte[] bytes = profile.data(); int end = Math.min(offset + 32768, bytes.length);
                ServerPlayNetworking.send(player, new AnimationPayload(1, profile.id(), profile.hash(), bytes.length, offset / 32768, Arrays.copyOfRange(bytes, offset, end))); offset = end;
                if (end == bytes.length) { ServerPlayNetworking.send(player, new AnimationPayload(2, profile.id(), profile.hash(), bytes.length, 0, new byte[0])); profile = null; }
            }
            return false;
        }
    }
    private AnimationNetworking() { }
}
