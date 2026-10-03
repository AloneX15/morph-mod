package com.takumistudios.morphmod.character;

import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.data.MorphAttachments;
import com.takumistudios.morphmod.morph.*;
import com.takumistudios.morphmod.network.CharacterNetworking;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Server-authoritative selection and transient emotes. */
public final class CharacterManager {
    private static final Set<UUID> ACTIVE = new HashSet<>();
    private static final Map<UUID, net.minecraft.world.phys.Vec3> POSITIONS = new HashMap<>();
    public record EmoteState(String id, String mode, long start) {
        public String encode() { return id + "|" + mode + "|" + start; }
        public static EmoteState decode(String text) {
            if (text == null || text.isEmpty()) return null;
            try { String[] parts = text.split("\\|", -1); return parts.length == 3 ? new EmoteState(parts[0], parts[1], Long.parseLong(parts[2])) : null; }
            catch (NumberFormatException e) { return null; }
        }
    }
    public static String selected(Player player) {
        Identifier id = player.getAttached(MorphAttachments.CURRENT_CHARACTER); return id == null ? "" : id.toString();
    }
    public static EmoteState emote(Player player) { return EmoteState.decode(player.getAttached(MorphAttachments.CURRENT_EMOTE)); }
    public static boolean allowed(ServerPlayer player, CharacterDefinition definition) {
        boolean unlocked = player.getAttachedOrCreate(MorphAttachments.UNLOCKED_CHARACTERS).contains(definition.id());
        return CharacterPermissions.check(player, definition.permission(), definition.free() || unlocked);
    }
    public static boolean allowedEmote(ServerPlayer player, CharacterDefinition definition, String emote) {
        var entry = definition.emotes().get(emote);
        return entry != null && allowed(player, definition) && CharacterPermissions.check(player, definition.emotePermission(emote), entry.free());
    }
    public static boolean select(ServerPlayer player, String id) {
        CharacterDefinition definition = CharacterCatalog.get(id);
        if (definition == null || !player.isAlive() || player.isSpectator() || !allowed(player, definition)) return false;
        if (!definition.mob().isEmpty() && MorphRegistry.get(Identifier.parse(definition.mob())).isEmpty()) return false;
        MorphManager.demorph(player);
        if (!definition.mob().isEmpty()) MorphManager.morph(player, BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(definition.mob())));
        player.setAttached(MorphAttachments.CURRENT_CHARACTER, Identifier.parse(id));
        ACTIVE.add(player.getUUID());
        POSITIONS.put(player.getUUID(), player.position());
        return true;
    }
    public static boolean play(ServerPlayer player, String id, String mode) {
        var definition = CharacterCatalog.get(selected(player));
        if (definition == null || !player.isAlive() || player.isSpectator() || player.isSleeping()
                || !allowedEmote(player, definition, id) || !definition.emotes().get(id).modes().contains(mode)) return false;
        player.setAttached(MorphAttachments.CURRENT_EMOTE, new EmoteState(id, mode, player.level().getGameTime()).encode());
        return true;
    }
    public static void stop(Player player) { player.removeAttached(MorphAttachments.CURRENT_EMOTE); }
    public static void clear(Player player) {
        player.removeAttached(MorphAttachments.CURRENT_CHARACTER); stop(player); ACTIVE.remove(player.getUUID()); POSITIONS.remove(player.getUUID());
    }
    public static void join(ServerPlayer player) {
        var definition = CharacterCatalog.get(selected(player));
        if (definition == null && !selected(player).isEmpty()) MorphManager.demorph(player);
        else if (definition != null) {
            if (!allowed(player, definition)) MorphManager.demorph(player);
            else ACTIVE.add(player.getUUID());
        }
        stop(player);
    }
    public static void unlock(ServerPlayer player, String id) {
        if (CharacterCatalog.get(id) == null) return;
        Set<String> values = new TreeSet<>(player.getAttachedOrCreate(MorphAttachments.UNLOCKED_CHARACTERS));
        if (values.size() < 256) values.add(id);
        player.setAttached(MorphAttachments.UNLOCKED_CHARACTERS, List.copyOf(values));
        CharacterNetworking.sendAccess(player);
    }
    public static void tick(MinecraftServer server) {
        for (UUID uuid : List.copyOf(ACTIVE)) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player == null) { ACTIVE.remove(uuid); POSITIONS.remove(uuid); continue; }
            var previousPosition = POSITIONS.put(uuid, player.position());
            var definition = CharacterCatalog.get(selected(player));
            if (!player.isAlive() || definition == null) { MorphManager.demorph(player); continue; }
            if (server.getTickCount() % 20 == 0) {
                if (!allowed(player, definition)) { MorphManager.demorph(player); CharacterNetworking.sendAccess(player); continue; }
            }
            EmoteState state = emote(player);
            if (state == null) continue;
            var clip = definition.emotes().get(state.id());
            boolean acting = com.takumistudios.morphmod.compat.PlayerAnimationState.swinging(player) || player.isUsingItem() || player.isPassenger();
            boolean moving = player.getDeltaMovement().horizontalDistanceSqr() > 0.0001
                || (previousPosition != null && player.position().subtract(previousPosition).horizontalDistanceSqr() > 0.0001);
            if (clip == null || player.isSleeping() || (state.mode().equals("full") && (acting || moving))
                || (!clip.loop() && player.level().getGameTime() - state.start() >= clip.seconds() * 20)
                || (server.getTickCount() % 20 == 0 && !allowedEmote(player, definition, state.id()))) stop(player);
        }
        if (server.getTickCount() % 20 == 0) server.getPlayerList().getPlayers().forEach(CharacterNetworking::sendAccess);
    }
    public static void forget(ServerPlayer player) { ACTIVE.remove(player.getUUID()); POSITIONS.remove(player.getUUID()); }
    private CharacterManager() { }
}
