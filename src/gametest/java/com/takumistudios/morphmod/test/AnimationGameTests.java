package com.takumistudios.morphmod.test;

import com.takumistudios.morphmod.animation.*;
import com.takumistudios.morphmod.network.AnimationNetworking;
import com.takumistudios.morphmod.character.CharacterPermissions;
import java.util.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.gametest.framework.GameTestHelper;

public final class AnimationGameTests {
    @GameTest public void profilesAssignmentsPersistenceAndPermissions(GameTestHelper helper) {
        var player = TestPlayers.create(helper); var server = player.level().getServer();
        var previous = AnimationCatalog.current().settings();
        try {
            var parsed = server.getCommands().getDispatcher().parse("morph animation force morphmod:dedsafio_4", server.createCommandSourceStack());
            helper.assertTrue(!parsed.getReader().canRead() && parsed.getExceptions().isEmpty(), "profile command accepts namespaced ids without quoting");
            helper.assertTrue(AnimationCatalog.exists("morphmod:dedsafio_4"), "bundled full animation profile loads on dedicated server");
            var settings = new AnimationSettings("morphmod:legacy_walk", List.of(new AnimationSettings.Rule("morphmod.animation.profile.test", "morphmod:dedsafio_4", 100)), Map.of());
            AnimationCatalog.settings(settings);
            helper.assertTrue(AnimationNetworking.selection(player).profile().equals("morphmod:legacy_walk"), "undefined rule uses global default");
            settings = settings.assign(List.of(player.getUUID()), "morphmod:vanilla"); AnimationCatalog.settings(settings);
            helper.assertTrue(AnimationNetworking.selection(player).profile().equals("morphmod:vanilla"), "individual vanilla disables imported movement");
            AnimationNetworking.force(server, "morphmod:dedsafio_4");
            helper.assertTrue(AnimationNetworking.selection(player).profile().equals("morphmod:dedsafio_4"), "forced profile wins over individual");
            AnimationNetworking.force(server, null);
            helper.assertTrue(AnimationNetworking.selection(player).profile().equals("morphmod:vanilla"), "force off restores exception");
            AnimationCatalog.save(settings).join();
            helper.assertTrue(AnimationCatalog.load().join().settings().overrides().get(player.getUUID()).equals("morphmod:vanilla"), "UUID exception survives a disk reload");
            AnimationCatalog.settings(settings.assign(List.of(player.getUUID()), null));
            if (FabricLoader.getInstance().isModLoaded("luckperms")) AnimationLuckPermsTestSupport.run(helper, player);
            helper.succeed();
        } finally { AnimationNetworking.force(server, null); AnimationCatalog.settings(previous); AnimationCatalog.save(previous).join(); }
    }
}
