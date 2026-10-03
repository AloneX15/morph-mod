package com.takumistudios.morphmod.compat;

import com.takumistudios.morphmod.character.CharacterPermissions;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.util.Tristate;
import net.minecraft.server.level.ServerPlayer;

/** Uses LuckPerms' current query options so group inheritance and contexts are respected. */
public final class LuckPermsCharacters implements CharacterPermissions.Provider {
    @Override public boolean check(ServerPlayer player, String node, boolean fallback) {
        var api = LuckPermsProvider.get();
        var user = api.getUserManager().getUser(player.getUUID());
        if (user == null) return false;
        var options = api.getContextManager().getQueryOptions(user).orElse(api.getContextManager().getStaticQueryOptions());
        Tristate value = user.getCachedData().getPermissionData(options).checkPermission(node);
        return value == Tristate.UNDEFINED ? fallback : value == Tristate.TRUE;
    }
}
