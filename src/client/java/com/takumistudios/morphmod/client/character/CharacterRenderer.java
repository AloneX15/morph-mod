package com.takumistudios.morphmod.client.character;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.*;
import com.geckolib.renderer.base.*;
import com.geckolib.renderer.layer.builtin.*;
import com.geckolib.cache.model.GeoBone;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.takumistudios.morphmod.character.*;
import java.util.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

public final class CharacterRenderer extends GeoEntityRenderer<CharacterEntity, CharacterRenderer.State> {
    public static final class State extends LivingEntityRenderState implements GeoRenderState {
        private final Map<com.geckolib.constant.dataticket.DataTicket<?>, Object> data = new IdentityHashMap<>();
        @Override public Map<com.geckolib.constant.dataticket.DataTicket<?>, Object> getDataMap() { return data; }
        @Override public <D> void addGeckolibData(com.geckolib.constant.dataticket.DataTicket<D> ticket, D value) { data.put(ticket, value); }
        @Override public boolean hasGeckolibData(com.geckolib.constant.dataticket.DataTicket<?> ticket) { return data.containsKey(ticket); }
        public CharacterDefinition character;
        public String arm = "";
    }
    @Override public void submit(State state, PoseStack pose, SubmitNodeCollector tasks, net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        try { super.submit(state, pose, tasks, camera); }
        catch (RuntimeException | LinkageError e) { CharacterRenderManager.disable(state.character.id(), e); }
    }
    @Override public void adjustRenderPose(RenderPassInfo<State> pass) {
        if (pass.renderState().arm.isEmpty()) { super.adjustRenderPose(pass); return; }
        var anchor = pass.renderState().character.anchors().get(pass.renderState().arm);
        pass.poseStack().translate((pass.renderState().arm.equals("right_arm") ? -5 : 5) / 16.0, 2 / 16.0, 0);
        pass.poseStack().scale(-1, -1, 1);
        pass.model().getBone(anchor.bone()).ifPresent(bone -> {
            double x = 0, y = 0, z = 0;
            for (GeoBone parent = bone; parent != null; parent = parent.parent()) { x += parent.pivotX(); y += parent.pivotY(); z += parent.pivotZ(); }
            pass.poseStack().translate(-x / 16, -y / 16, -z / 16);
        });
    }
    public CharacterRenderer(EntityRendererProvider.Context context) {
        super(context, new Model()); shadowRadius = 0.3F;
        withRenderLayer(new Hands(context, this)); withRenderLayer(new Armor(context, this));
    }
    @Override public State createRenderState(CharacterEntity entity, Void related) { return new State(); }
    @Override public void addRenderData(CharacterEntity entity, Void related, State state, float partialTick) {
        state.character = entity.definition;
        state.nameTag = entity.owner == net.minecraft.client.Minecraft.getInstance().player ? null : entity.owner == null ? null : entity.owner.getDisplayName();
    }
    @Override public void scaleModelForRender(RenderPassInfo<State> pass, float width, float height) {
        super.scaleModelForRender(pass, width * pass.renderState().character.scale(), height * pass.renderState().character.scale());
    }
    @Override public void adjustModelBonesForRender(RenderPassInfo<State> pass, BoneSnapshots snapshots) {
        String arm = pass.renderState().arm;
        if (!arm.isEmpty()) {
            // Hide cubes, not descendants: the entire rig still drives the chosen arm.
            Set<String> allowed = new HashSet<>(); collect(pass.model().getBone(pass.renderState().character.anchors().get(arm).bone()).orElse(null), allowed);
            for (String name : pass.renderState().character.bones()) snapshots.ifPresent(name, s -> s.skipRender(!allowed.contains(name)));
        }
    }
    private static void collect(GeoBone bone, Set<String> names) { if (bone != null) { names.add(bone.name()); for (GeoBone child : bone.children()) collect(child, names); } }
    private static final class Model extends GeoModel<CharacterEntity> {
        @Override public Identifier getModelResource(GeoRenderState state) { return com.geckolib.cache.GeckoLibResources.stripPrefixAndSuffix(ClientCharacters.resource(((State)state).character, "model")); }
        @Override public Identifier getTextureResource(GeoRenderState state) { return ClientCharacters.resource(((State)state).character, "texture"); }
        @Override public Identifier getAnimationResource(CharacterEntity entity) { return com.geckolib.cache.GeckoLibResources.stripPrefixAndSuffix(ClientCharacters.resource(entity.definition, "animation")); }
    }
    private static void transform(PoseStack pose, CharacterDefinition.Anchor anchor) {
        if (anchor == null) return;
        pose.translate(anchor.position()[0] / 16, anchor.position()[1] / 16, anchor.position()[2] / 16);
        //? if >=26.3 {
        pose.rotate(Axis.XP.rotationDegrees(anchor.rotation()[0])); pose.rotate(Axis.YP.rotationDegrees(anchor.rotation()[1])); pose.rotate(Axis.ZP.rotationDegrees(anchor.rotation()[2]));
        //?} else {
        /*pose.mulPose(Axis.XP.rotationDegrees(anchor.rotation()[0])); pose.mulPose(Axis.YP.rotationDegrees(anchor.rotation()[1])); pose.mulPose(Axis.ZP.rotationDegrees(anchor.rotation()[2]));
        *///?}
        pose.scale(anchor.scale()[0], anchor.scale()[1], anchor.scale()[2]);
    }
    private static final class Hands extends ItemInHandGeoLayer<CharacterEntity, Void, State> {
        Hands(EntityRendererProvider.Context context, CharacterRenderer renderer) { super(context, renderer, null, null); }
        @Override protected List<RenderData> getRelevantBones(CharacterEntity entity, Void related, State state, float tick) {
            if (!state.arm.isEmpty()) return List.of(); // Vanilla held-item pass remains in first person.
            List<RenderData> result = new ArrayList<>(2);
            var right = state.character.anchors().get("right_hand"); var left = state.character.anchors().get("left_hand");
            if (right != null) result.add(renderDataForHand(right.bone(), net.minecraft.world.entity.HumanoidArm.RIGHT, entity, state));
            if (left != null) result.add(renderDataForHand(left.bone(), net.minecraft.world.entity.HumanoidArm.LEFT, entity, state));
            return result;
        }
        @Override protected void submitItemStackRender(PoseStack pose, GeoBone bone, ItemStackRenderState item, ItemDisplayContext context, State state, SubmitNodeCollector tasks, int light) {
            pose.pushPose();
            transform(pose, state.character.anchors().get(context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND ? "right_hand" : "left_hand"));
            super.submitItemStackRender(pose, bone, item, context, state, tasks, light); pose.popPose();
        }
    }
    private static final class Armor extends ItemArmorGeoLayer<CharacterEntity, Void, State> {
        Armor(EntityRendererProvider.Context context, CharacterRenderer renderer) { super(renderer, context); }
        @Override protected List<RenderData> getRelevantBones(RenderPassInfo<State> pass) {
            State state = pass.renderState(); List<RenderData> result = new ArrayList<>();
            Map<String, CharacterDefinition.Anchor> anchors = state.character.anchors();
            for (var entry : anchors.entrySet()) {
                if (!state.arm.isEmpty() && !entry.getKey().equals(state.arm)) continue;
                String bone = entry.getValue().bone();
                if (entry.getKey().equals("chest") && state.getGeckolibData(com.geckolib.constant.DataTickets.EQUIPMENT_BY_SLOT).get(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() == net.minecraft.world.item.Items.ELYTRA) {
                    var wings = anchors.get("elytra"); if (wings == null) continue; bone = wings.bone();
                }
                RenderData data = switch (entry.getKey()) {
                    case "head" -> RenderData.head(bone); case "chest" -> RenderData.body(bone);
                    case "right_arm" -> RenderData.rightArm(bone); case "left_arm" -> RenderData.leftArm(bone);
                    case "right_leg" -> RenderData.rightLeg(bone); case "left_leg" -> RenderData.leftLeg(bone);
                    case "right_foot" -> RenderData.rightFoot(bone); case "left_foot" -> RenderData.leftFoot(bone); default -> null;
                };
                if (data != null) result.add(data);
            }
            return result;
        }
        @Override protected void submitRenderForBone(RenderPassInfo<State> pass, SubmitNodeCollector tasks, RenderData data, GeoBone bone) {
            pass.poseStack().pushPose();
            var anchors = pass.renderState().character.anchors();
            String key = switch (data.armorSegment()) {
                case HEAD -> "head"; case CHEST -> "chest"; case RIGHT_ARM -> "right_arm"; case LEFT_ARM -> "left_arm";
                case RIGHT_LEG -> "right_leg"; case LEFT_LEG -> "left_leg"; case RIGHT_FOOT -> "right_foot"; case LEFT_FOOT -> "left_foot";
            };
            boolean wing = key.equals("chest") && pass.renderState().getGeckolibData(com.geckolib.constant.DataTickets.EQUIPMENT_BY_SLOT).get(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() == net.minecraft.world.item.Items.ELYTRA;
            transform(pass.poseStack(), anchors.get(wing ? "elytra" : key));
            super.submitRenderForBone(pass, tasks, data, bone); pass.poseStack().popPose();
        }
    }
}
