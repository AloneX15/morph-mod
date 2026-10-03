package com.takumistudios.morphmod.character;

import com.takumistudios.morphmod.MorphMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

/** Optional integration: no LuckPerms classes are linked when the provider is absent. */
public final class CharacterPermissions {
    public interface Provider { boolean check(ServerPlayer player, String node, boolean fallback); }
    private static Provider provider = (player, node, fallback) -> fallback;
    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("luckperms")) {
            try { provider = new com.takumistudios.morphmod.compat.LuckPermsCharacters(); }
            catch (RuntimeException | LinkageError e) { MorphMod.LOGGER.warn("LuckPerms integration unavailable; using configured access", e); }
        }
    }
    private static boolean reported;
    public static boolean check(ServerPlayer player, String node, boolean fallback) {
        try { return provider.check(player, node, fallback); }
        catch (RuntimeException | LinkageError e) {
            if (!reported) { reported = true; MorphMod.LOGGER.warn("Permission provider failed; custom character access denied", e); }
            return false;
        }
    }
    private CharacterPermissions() { }
}
