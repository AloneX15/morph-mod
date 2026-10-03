package com.takumistudios.morphmod.test;
import com.takumistudios.morphmod.character.*;
import com.takumistudios.morphmod.morph.MorphManager;
import net.minecraft.gametest.framework.GameTestHelper;
final class LuckPermsTestSupport {
    static void run(GameTestHelper helper) {
        var player = TestPlayers.create(helper);
        var api = net.luckperms.api.LuckPermsProvider.get();
        var user = api.getUserManager().loadUser(player.getUUID()).join();
        var group = api.getGroupManager().createAndLoadGroup("morph_test").join();
        var definition = CharacterCatalog.get("morphmod:otter");
        var deny = net.luckperms.api.node.Node.builder(definition.permission()).value(false).build();
        var grant = net.luckperms.api.node.Node.builder(definition.permission()).value(true).build();
        try {
            helper.assertTrue(CharacterManager.allowed(player, definition), "undefined permission uses public default");
            group.data().add(deny);
            user.data().add(net.luckperms.api.node.types.InheritanceNode.builder("morph_test").build());
            user.getCachedData().invalidate();
            helper.assertTrue(!CharacterManager.select(player, definition.id()), "group denial blocks a public character");
            user.data().add(grant); user.getCachedData().invalidate();
            helper.assertTrue(CharacterManager.select(player, definition.id()), "individual grant overrides inherited denial");
            var emoteDeny = net.luckperms.api.node.Node.builder(definition.emotePermission("demo_dance")).value(false).build();
            user.data().add(emoteDeny); user.getCachedData().invalidate();
            helper.assertTrue(!CharacterManager.play(player, "demo_dance", "full"), "explicit emote denial enforced");
            user.data().remove(emoteDeny); user.getCachedData().invalidate();
            helper.assertTrue(CharacterManager.play(player, "demo_dance", "overlay"), "undefined emote permission uses default");
            user.data().remove(grant); user.getCachedData().invalidate();
            helper.assertTrue(!CharacterManager.allowed(player, definition), "removing grant restores inherited denial");
            helper.succeed();
        } finally {
            user.data().clear(); group.data().clear();
            MorphManager.demorph(player); CharacterManager.forget(player); MorphManager.forget(player);
        }
    }
}
