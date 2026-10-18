package com.takumistudios.morphmod.test;

import com.takumistudios.morphmod.character.CharacterPermissions;
import com.takumistudios.morphmod.network.AnimationNetworking;
import net.minecraft.gametest.framework.GameTestHelper;

final class AnimationLuckPermsTestSupport {
    static void run(GameTestHelper helper, net.minecraft.server.level.ServerPlayer player) {
        var api = net.luckperms.api.LuckPermsProvider.get();
        var user = api.getUserManager().loadUser(player.getUUID()).join();
        var group = api.getGroupManager().createAndLoadGroup("morph_animation_test").join();
        var grant = net.luckperms.api.node.Node.builder("morphmod.animation.profile.test").value(true).build();
        var deny = net.luckperms.api.node.Node.builder("morphmod.animation.profile.test").value(false).build();
        try {
            group.data().add(grant); user.data().add(net.luckperms.api.node.types.InheritanceNode.builder("morph_animation_test").build()); user.getCachedData().invalidate();
            helper.assertTrue(AnimationNetworking.selection(player).profile().equals("morphmod:dedsafio_4"), "inherited permission selects profile");
            user.data().add(deny); user.getCachedData().invalidate();
            helper.assertTrue(AnimationNetworking.selection(player).profile().equals("morphmod:legacy_walk"), "explicit denial excludes automatic rule");
            user.data().remove(deny); user.getCachedData().invalidate();
            helper.assertTrue(AnimationNetworking.selection(player).profile().equals("morphmod:dedsafio_4"), "permission changes take effect without reconnect");
            var contextualDeny = net.luckperms.api.node.Node.builder("morphmod.animation.profile.test").value(false).withContext("world", "morphmod_missing_world").build();
            user.data().add(contextualDeny); user.getCachedData().invalidate();
            helper.assertTrue(AnimationNetworking.selection(player).profile().equals("morphmod:dedsafio_4"), "denial in a different world context does not override inherited grant");
            var adminDeny = net.luckperms.api.node.Node.builder("morphmod.animations.admin").value(false).build(); user.data().add(adminDeny); user.getCachedData().invalidate();
            helper.assertTrue(!CharacterPermissions.check(player, "morphmod.animations.admin", true), "explicit administrative deny overrides OP fallback");
        } finally { user.data().clear(); user.getCachedData().invalidate(); group.data().clear(); }
    }
}
