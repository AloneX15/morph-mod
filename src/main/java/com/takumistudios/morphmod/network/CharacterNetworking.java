package com.takumistudios.morphmod.network;

import com.google.gson.*;
import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.character.*;
import com.takumistudios.morphmod.morph.MorphManager;
import com.takumistudios.morphmod.util.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.*;

public final class CharacterNetworking {
    private static final Map<ServerConfigurationPacketListenerImpl, TransferTask> CONFIG = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, CharacterBundle>> PLAY = new HashMap<>();
    private static final Map<UUID, Transfer> TRANSFERS = new HashMap<>();
    private static final Map<UUID, String> ACCESS = new HashMap<>();
    private static final RequestLimiter LIMIT = new RequestLimiter(8, 20);
    private static final FeatureGuard GUARD = new FeatureGuard("character networking");
    public static void init() {
        PayloadTypeRegistry.clientboundConfiguration().register(CharacterPayload.TYPE, CharacterPayload.CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(CharacterPayload.TYPE, CharacterPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CharacterPayload.TYPE, CharacterPayload.CODEC.cast());
        PayloadTypeRegistry.serverboundPlay().register(CharacterPayload.TYPE, CharacterPayload.CODEC.cast());
        PayloadTypeRegistry.serverboundPlay().register(CharacterActionPayload.TYPE, CharacterActionPayload.CODEC);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try { CharacterCatalog.publish(CharacterCatalog.load().join()); }
            catch (RuntimeException e) { MorphMod.LOGGER.error("Character catalog unavailable", e); }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> { CONFIG.clear(); PLAY.clear(); TRANSFERS.clear(); ACCESS.clear(); });
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            var iterator = TRANSFERS.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next(); ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                if (player == null || entry.getValue().pump(value -> ServerPlayNetworking.send(player, value))) iterator.remove();
            }
        });
        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            if (ServerConfigurationNetworking.canSend(handler, CharacterPayload.TYPE)) {
                TransferTask task = new TransferTask(handler, CharacterCatalog.bundles());
                CONFIG.put(handler, task); handler.addTask(task);
            }
        });
        ServerConfigurationNetworking.registerGlobalReceiver(CharacterPayload.TYPE, (payload, context) -> {
            TransferTask task = CONFIG.get(context.packetListener());
            if (task != null) task.receive(payload);
        });
        ServerConfigurationConnectionEvents.DISCONNECT.register((handler, server) -> CONFIG.remove(handler));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sendAccess(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.player.getUUID(); PLAY.remove(id); TRANSFERS.remove(id); ACCESS.remove(id); LIMIT.forget(id);
        });
        ServerPlayNetworking.registerGlobalReceiver(CharacterPayload.TYPE, (payload, context) -> GUARD.run(() -> {
            ServerPlayer player = context.player();
            if (payload.stage() == 4) {
                Map<String, CharacterBundle> offered = PLAY.remove(player.getUUID());
                if (offered != null) TRANSFERS.put(player.getUUID(), new Transfer(offered, payload));
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(CharacterActionPayload.TYPE, (payload, context) -> GUARD.run(() -> {
            ServerPlayer player = context.player();
            if (!MorphNetworking.negotiated(player) || !player.isAlive() || player.isSpectator()
                || !LIMIT.allow(player.getUUID(), player.level().getServer().getTickCount())) return;
            switch (payload.action()) {
                case "select" -> CharacterManager.select(player, payload.id());
                case "clear" -> MorphManager.demorph(player);
                case "play" -> CharacterManager.play(player, payload.id(), payload.mode());
                case "stop" -> CharacterManager.stop(player);
                default -> MorphMod.LOGGER.debug("Ignored invalid character action {}", payload.action());
            }
        }));
    }
    public static void offer(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, CharacterPayload.TYPE)) return;
        PLAY.put(player.getUUID(), CharacterCatalog.bundles());
        TRANSFERS.remove(player.getUUID());
        ServerPlayNetworking.send(player, offer(CharacterCatalog.bundles()));
        ACCESS.remove(player.getUUID()); sendAccess(player);
    }
    public static void sendAccess(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, CharacterPayload.TYPE)) return;
        JsonObject permissions = new JsonObject();
        CharacterCatalog.bundles().values().stream().sorted(Comparator.comparing(b -> b.definition().id())).forEach(b -> {
            CharacterDefinition d = b.definition();
            JsonArray emotes = new JsonArray();
            d.emotes().keySet().stream().sorted().filter(id -> CharacterManager.allowedEmote(player, d, id)).forEach(emotes::add);
            JsonObject value = new JsonObject(); value.addProperty("allowed", CharacterManager.allowed(player, d)); value.add("emotes", emotes);
            permissions.add(d.id(), value);
        });
        String encoded = permissions.toString();
        if (!encoded.equals(ACCESS.put(player.getUUID(), encoded))) {
            byte[] bytes = encoded.getBytes(StandardCharsets.UTF_8);
            if (bytes.length <= CharacterBundle.FRAGMENT) ServerPlayNetworking.send(player, CharacterPayload.signal(6, bytes));
        }
    }
    private static CharacterPayload offer(Map<String, CharacterBundle> bundles) {
        JsonArray list = new JsonArray();
        for (var b : bundles.values()) {
            JsonObject entry = new JsonObject(); entry.addProperty("id", b.definition().id()); entry.addProperty("hash", b.hash()); entry.addProperty("size", b.archive().length); list.add(entry);
        }
        byte[] bytes = list.toString().getBytes(StandardCharsets.UTF_8);
        if (bytes.length > CharacterBundle.FRAGMENT) throw new IllegalArgumentException("Catalog metadata exceeds limit");
        return CharacterPayload.signal(0, bytes);
    }
    private static Set<String> cachedHashes(CharacterPayload reply) {
        Set<String> cached = new HashSet<>();
        try {
            JsonArray hashes = JsonParser.parseString(new String(reply.data(), StandardCharsets.UTF_8)).getAsJsonArray();
            if (hashes.size() > 256) throw new IllegalArgumentException("Too many hashes");
            for (JsonElement hash : hashes) cached.add(hash.getAsString());
        } catch (RuntimeException e) { MorphMod.LOGGER.debug("Ignoring invalid cache acknowledgement", e); }
        return cached;
    }
    /** Retains archive references; copies at most 256 KiB per player per tick. */
    private static final class Transfer {
        private final Iterator<CharacterBundle> remaining;
        private CharacterBundle bundle;
        private int offset;
        Transfer(Map<String, CharacterBundle> bundles, CharacterPayload reply) {
            Set<String> cached = cachedHashes(reply);
            remaining = bundles.values().stream().filter(b -> !cached.contains(b.hash())).iterator();
        }
        boolean pump(Consumer<CharacterPayload> sender) {
            for (int budget = 0; budget < 8; budget++) {
                if (bundle == null) {
                    if (!remaining.hasNext()) { sender.accept(CharacterPayload.signal(3, new byte[0])); return true; }
                    bundle = remaining.next(); offset = 0;
                }
                byte[] archive = bundle.archive();
                int end = Math.min(offset + CharacterBundle.FRAGMENT, archive.length);
                sender.accept(new CharacterPayload(1, bundle.definition().id(), bundle.hash(), archive.length, offset / CharacterBundle.FRAGMENT, Arrays.copyOfRange(archive, offset, end)));
                offset = end;
                if (offset == archive.length) {
                    sender.accept(new CharacterPayload(2, bundle.definition().id(), bundle.hash(), archive.length, 0, new byte[0]));
                    bundle = null;
                }
            }
            return false;
        }
    }
    private static final class TransferTask implements ConfigurationTask {
        private static final Type KEY = new Type("morphmod:characters");
        private final ServerConfigurationPacketListenerImpl handler;
        private final Map<String, CharacterBundle> snapshot;
        private long start;
        private boolean sent;
        private boolean done;
        private Transfer transfer;
        TransferTask(ServerConfigurationPacketListenerImpl handler, Map<String, CharacterBundle> snapshot) { this.handler = handler; this.snapshot = snapshot; }
        @Override public Type type() { return KEY; }
        @Override public void start(Consumer<Packet<?>> sender) { start = System.nanoTime(); ServerConfigurationNetworking.send(handler, offer(snapshot)); }
        synchronized void receive(CharacterPayload payload) {
            if (done) return;
            try {
                if (payload.stage() == 4 && !sent) { sent = true; transfer = new Transfer(snapshot, payload); }
                else if (payload.stage() == 5) finish();
            } catch (RuntimeException e) { MorphMod.LOGGER.warn("Character transfer failed; continuing without custom visuals", e); finish(); }
        }
        private void finish() { if (!done) { done = true; CONFIG.remove(handler); handler.completeTask(KEY); } }
        @Override public synchronized boolean tick() {
            if (!done && transfer != null && transfer.pump(value -> ServerConfigurationNetworking.send(handler, value))) transfer = null;
            if (!done && start != 0 && System.nanoTime() - start > 120_000_000_000L) finish();
            return done;
        }
    }
    private CharacterNetworking() { }
}
