package com.takumistudios.morphmod.client.character;

import com.geckolib.loading.math.MolangQueries;
import com.takumistudios.morphmod.MorphMod;
import com.takumistudios.morphmod.character.*;
import com.takumistudios.morphmod.client.mixin.WalkAnimationStateAccessor;
import java.util.*;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;

/** Per-visible-player proxies, released on world change and resource reload. */
public final class CharacterRenderManager {
    private static final Map<UUID, CharacterEntity> PROXIES = new HashMap<>();
    private static final Set<String> FAILED = new HashSet<>();
    private static int nextId = -1_000_000;
    public static void init() {
        EntityRendererRegistry.register(CharacterEntity.TYPE, CharacterRenderer::new);
        MolangQueries.setActorVariable("query.pitch", a -> a.animatable() instanceof CharacterEntity e && e.owner != null ? e.owner.getXRot() : 0);
        MolangQueries.setActorVariable("query.yaw", a -> a.animatable() instanceof CharacterEntity e && e.owner != null ? net.minecraft.util.Mth.wrapDegrees(e.owner.yHeadRot - e.owner.yBodyRot) : 0);
        MolangQueries.setActorVariable("query.right_hand_swing", a -> a.animatable() instanceof CharacterEntity e ? swing(e, true, a.partialTick()) : 0);
        MolangQueries.setActorVariable("query.left_hand_swing", a -> a.animatable() instanceof CharacterEntity e ? swing(e, false, a.partialTick()) : 0);
    }
    private static double swing(CharacterEntity e, boolean right, float tick) {
        if (e.owner == null || !com.takumistudios.morphmod.compat.PlayerAnimationState.swinging(e.owner)) return 0;
        boolean main = com.takumistudios.morphmod.compat.PlayerAnimationState.hand(e.owner) == net.minecraft.world.InteractionHand.MAIN_HAND;
        return (main == (e.owner.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT)) == right ? com.takumistudios.morphmod.compat.PlayerAnimationState.progress(e.owner, tick) : 0;
    }
    public static CharacterEntity proxy(AbstractClientPlayer player) {
        String id = CharacterManager.selected(player); CharacterDefinition definition = ClientCharacters.get(id);
        if (definition == null || FAILED.contains(id)) return null;
        try {
            CharacterEntity proxy = PROXIES.get(player.getUUID());
            if (proxy == null || proxy.definition != definition || proxy.level() != player.level()) {
                proxy = new CharacterEntity(CharacterEntity.TYPE, player.level()); proxy.definition = definition; proxy.setId(nextId--);
                PROXIES.put(player.getUUID(), proxy);
            }
            proxy.copy(player); proxy.setCustomName(player == Minecraft.getInstance().player ? null : player.getDisplayName());
            if (player != Minecraft.getInstance().player) proxy.setDeltaMovement(player.getX() - player.xo, player.getY() - player.yo, player.getZ() - player.zo);
            WalkAnimationStateAccessor from = (WalkAnimationStateAccessor)player.walkAnimation;
            WalkAnimationStateAccessor to = (WalkAnimationStateAccessor)proxy.walkAnimation;
            to.morphmod$setSpeed(from.morphmod$getSpeed()); to.morphmod$setSpeedOld(from.morphmod$getSpeedOld()); to.morphmod$setPosition(from.morphmod$getPosition());
            return proxy;
        } catch (RuntimeException | LinkageError e) { FAILED.add(id); MorphMod.LOGGER.warn("Character {} render disabled", id, e); return null; }
    }
    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null) { clear(); return; }
        Set<UUID> visible = new HashSet<>(); minecraft.level.players().forEach(p -> visible.add(p.getUUID())); PROXIES.keySet().retainAll(visible);
    }
    public static void clear() { PROXIES.clear(); FAILED.clear(); nextId = -1_000_000; }
    public static void disable(String id, Throwable error) { if (FAILED.add(id)) MorphMod.LOGGER.warn("Character {} renderer disabled; using player model", id, error); }
    public static boolean renderArm(com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector tasks, int light, String arm) {
        Minecraft mc = Minecraft.getInstance(); if (mc.player == null) return false;
        CharacterEntity proxy = proxy(mc.player); if (proxy == null || !proxy.definition.anchors().containsKey(arm)) return false;
        try {
            var renderer = (CharacterRenderer)mc.getEntityRenderDispatcher().getRenderer(proxy);
            var state = renderer.createRenderState(proxy, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
            state.arm = arm; state.lightCoords = light;
            state.addGeckolibData(com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer.CONTENTS, List.of());
            renderer.submit(state, pose, tasks, new net.minecraft.client.renderer.state.level.CameraRenderState());
            return true;
        } catch (RuntimeException | LinkageError e) { disable(proxy.definition.id(), e); return false; }
    }
    private CharacterRenderManager() { }
}
