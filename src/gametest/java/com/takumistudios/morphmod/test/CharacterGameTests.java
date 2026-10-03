package com.takumistudios.morphmod.test;

import com.takumistudios.morphmod.character.*;
import com.takumistudios.morphmod.morph.MorphManager;
import com.takumistudios.morphmod.compat.EntityTypes;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class CharacterGameTests {
    @GameTest public void characterSelectionEmotesAndDemorph(GameTestHelper helper) {
        var player = TestPlayers.create(helper);
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("luckperms"))
            net.luckperms.api.LuckPermsProvider.get().getUserManager().loadUser(player.getUUID()).join();
        try {
            helper.assertTrue(CharacterCatalog.get("morphmod:otter") != null, "bundled character catalog loaded");
            MorphManager.morph(player, EntityTypes.WARDEN);
            helper.assertTrue(CharacterManager.select(player, "morphmod:otter"), "select public character");
            helper.assertTrue(player.getMaxHealth() == 20, "human character removes mob stats");
            helper.assertTrue(MorphManager.current(player).isEmpty(), "human profile has no mob powers");
            helper.assertTrue(CharacterManager.play(player, "demo_dance", "overlay"), "play overlay emote");
            helper.assertTrue(CharacterManager.emote(player) != null, "emote is attached");
            helper.assertTrue(!CharacterManager.play(player, "missing", "overlay"), "unknown emote rejected");
            helper.assertTrue(!CharacterManager.play(player, "demo_dance", "invalid"), "invalid mode rejected");
            MorphManager.morph(player, EntityTypes.BAT);
            helper.assertTrue(CharacterManager.selected(player).isEmpty() && CharacterManager.emote(player) == null, "mob transition clears character and emote");
            helper.assertTrue(!CharacterManager.select(player, "unknown:character"), "unknown character rejected");
            MorphManager.demorph(player); helper.succeed();
        } finally { MorphManager.demorph(player); MorphManager.forget(player); }
    }
    @GameTest public void luckPermsInheritanceAndExplicitDeny(GameTestHelper helper) {
        if (!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("luckperms")) { helper.succeed(); return; }
        LuckPermsTestSupport.run(helper);
    }
}
