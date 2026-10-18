package com.takumistudios.morphmod.client.animation;

import com.takumistudios.morphmod.animation.*;
import com.takumistudios.morphmod.character.PlayerLocomotion;
import com.takumistudios.morphmod.compat.PlayerAnimationState;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Per-render-state canonical rig. Snapshots contain no references to world entities. */
public final class PlayerAnimationPose {
    public static final List<String> NAMES = List.of("root", "waist", "body", "body_upper", "head", "right_arm", "left_arm", "right_leg", "left_leg", "right_foot", "left_foot");
    private static final int[] PARENT = {-1, 0, 1, 2, 3, 3, 3, 1, 1, 7, 8};
    private static final float[][] PIVOT = {{0,24,0},{0,0,0},{0,0,0},{0,4,0},{0,0,0},{-5,2,0},{5,2,0},{-1.9F,12,0},{1.9F,12,0},{-1.9F,24,0},{1.9F,24,0}};
    private final float[][] values = new float[11][9];
    private final int[] masks = new int[11];
    private final int[] look = new int[11], inheritedLook = new int[11];
    private final Matrix4f[] world = new Matrix4f[11];
    private final Quaternionf[] rotation = new Quaternionf[11];
    private final Vector3f[] scale = new Vector3f[11];
    private final AnimationMath.Context context = new AnimationMath.Context();
    private final Quaternionf scratchRotation = new Quaternionf();
    private final Vector3f scratchPosition = new Vector3f(), scratchAngles = new Vector3f();
    private final Matrix4f scratchMatrix = new Matrix4f();
    public boolean active;
    public boolean eligible;
    public boolean legacy;
    public boolean managed;
    public String profileId = "morphmod:vanilla";
    public PlayerAnimationPose() {
        for (int i = 0; i < 11; i++) { world[i] = new Matrix4f(); rotation[i] = new Quaternionf(); scale[i] = new Vector3f(1); }
    }
    public boolean replacesBodyOrientation() { return active && ((masks[0] | masks[1]) & 1) != 0; }
    public void reset() { active = false; eligible = false; legacy = false; managed = false; profileId = "morphmod:vanilla"; Arrays.fill(masks, 0); Arrays.fill(look, 0); }
    public void extract(Player player, float partialTick) {
        reset();
        eligible = player.isAlive() && !player.isSpectator()
            && player.getAttached(com.takumistudios.morphmod.data.MorphAttachments.CURRENT_MORPH) == null
            && player.getAttached(com.takumistudios.morphmod.data.MorphAttachments.CURRENT_CHARACTER) == null;
        if (!eligible) return;
        managed = ClientAnimationProfiles.managed();
        profileId = managed ? ClientAnimationProfiles.selected(player.getUUID()) : "morphmod:legacy_walk";
        legacy = profileId.equals("morphmod:legacy_walk");
        AnimationProfile profile = ClientAnimationProfiles.get(profileId);
        if (profile == null || ClientAnimationProfiles.failed(profile.hash())) return;
        try {
            for (float[] bone : values) { Arrays.fill(bone, 0); bone[6] = bone[7] = bone[8] = 1; }
            double age = (player.tickCount + partialTick) / 20.0;
            double clock = (player.level().getGameTime() + partialTick) / 20.0;
            double speed = PlayerLocomotion.speed(Math.hypot(player.getX() - player.xo, player.getZ() - player.zo));
            boolean swinging = PlayerAnimationState.swinging(player);
            float swing = swinging ? PlayerAnimationState.progress(player, partialTick) : 0;
            boolean swingRight = swinging && (PlayerAnimationState.hand(player) == InteractionHand.MAIN_HAND) == (player.getMainArm() == HumanoidArm.RIGHT);
            context.set("life_time", age).set("ground_speed", speed).set("pitch", player.getXRot()).set("yaw", net.minecraft.util.Mth.wrapDegrees(player.yHeadRot - player.yBodyRot))
                .set("head_x_rotation", player.getXRot()).set("head_y_rotation", net.minecraft.util.Mth.wrapDegrees(player.yHeadRot - player.yBodyRot))
                .set("right_hand_swing", swingRight ? swing : 0).set("left_hand_swing", swinging && !swingRight ? swing : 0)
                .set("is_sneaking", player.isCrouching() ? 1 : 0).set("is_sprinting", player.isSprinting() ? 1 : 0)
                .set("is_swimming", player.isSwimming() ? 1 : 0).set("is_on_ground", player.onGround() ? 1 : 0)
                .set("limb_swing", player.walkAnimation.position(partialTick)).set("limb_swing_amount", player.walkAnimation.speed(partialTick))
                .set("is_first_person", player == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson() ? 1 : 0);
            boolean moving = speed > 0.001;
            String action = player.isSleeping() ? "sleep" : player.isPassenger() ? "sit" : player.isAutoSpinAttack() ? "riptide" : player.isFallFlying() ? "elytra"
                : player.isSwimming() ? "swim" : player.getPose() == Pose.SWIMMING ? "crawl" : player.onClimbable() ? (moving ? "climb" : "climb_idle")
                : !player.onGround() ? (player.getDeltaMovement().y > 0 ? "jump" : "fall") : player.isCrouching() ? (moving ? "sneak_walk" : "sneak")
                : moving ? (player.isSprinting() ? "run" : "walk") : "idle";
            boolean holding = !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty();
            AnimationProfile.Clip movement = holding ? profile.resolve("specialpose." + action) : null;
            if (movement == null) movement = profile.resolve(action);
            double elapsed = ClientAnimationProfiles.phase(player.getUUID(), profile.hash(), action, clock);
            apply(movement, movement != null && movement.length() > 0 && !movement.loop() ? elapsed : clock);
            boolean priorityRight = player.isUsingItem()
                ? (player.getUsedItemHand() == InteractionHand.MAIN_HAND) == (player.getMainArm() == HumanoidArm.RIGHT)
                : (player.getMainArm() == HumanoidArm.RIGHT);
            if (!player.isUsingItem() && charged(player.getOffhandItem()) && !charged(player.getMainHandItem())) priorityRight = player.getMainArm() != HumanoidArm.RIGHT;
            arm(profile, player, !priorityRight, clock, partialTick);
            int activeArms = arm(profile, player, priorityRight, clock, partialTick);
            // Missing use/attack clips must retain vanilla arm poses, including in first person.
            if (player.isUsingItem()) { if ((activeArms & 1) == 0) masks[5] = 0; if ((activeArms & 2) == 0) masks[6] = 0; }
            if (!player.isUsingItem() && !player.getMainHandItem().isEmpty() && !player.getOffhandItem().isEmpty()) apply(profile.resolve("special_pose.hold_both"), clock);
            if (swinging) {
                AnimationProfile.Clip combat = profile.resolve(swingRight ? "swing.main_hand" : "swing.off_hand");
                int provided = arms(combat);
                if ((provided & (swingRight ? 1 : 2)) == 0) masks[swingRight ? 5 : 6] = 0;
                if (combat != null) apply(combat, combat.length() > 0 ? swing * combat.length() : swing);
            }
            // Feet have no independent vanilla joint: merge their channels into the corresponding leg.
            mergeFoot(9, 7); mergeFoot(10, 8);
            buildRig();
        } catch (RuntimeException | LinkageError error) { active = false; ClientAnimationProfiles.fail(profile.hash(), error); }
    }
    private int arm(AnimationProfile profile, Player player, boolean right, double age, float tick) {
        boolean main = (player.getMainArm() == HumanoidArm.RIGHT) == right;
        boolean using = player.isUsingItem() && (player.getUsedItemHand() == InteractionHand.MAIN_HAND) == main;
        ItemStack stack = main ? player.getMainHandItem() : player.getOffhandItem();
        if (stack.isEmpty()) return 0;
        String action = "item";
        if (using) action = switch (stack.getUseAnimation()) {
            case EAT, DRINK -> "eat"; case BLOCK -> "shield"; case BOW -> "bow"; case CROSSBOW -> "crossbow_charge";
            case TRIDENT, SPEAR -> "trident"; case SPYGLASS -> "spyglass"; case BRUSH -> "brush"; case TOOT_HORN -> "goat_horn"; default -> "item";
        };
        else if (stack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(stack)) action = "crossbow";
        AnimationProfile.Clip clip = profile.resolve(action + (right ? "_rightArm" : "_leftArm"));
        if (using && action.equals("eat")) {
            AnimationProfile.Clip logical = profile.resolve(main ? "eat.main_hand" : "eat.off_hand");
            if (logical == null) logical = profile.resolve(main ? "eat" : "eat.offhand");
            if (logical != null) clip = logical;
        }
        if (clip == null && (using || action.equals("crossbow"))) clip = profile.resolve(switch (action) {
            case "bow" -> main ? "bow_aim" : "bow_aim.offhand"; case "crossbow", "crossbow_charge" -> "crossbow_aim";
            case "shield" -> "block"; case "trident" -> "trident_aim"; case "item" -> right ? "hand_right_interact" : "hand_left_interact";
            default -> action;
        });
        if (clip == null && using && action.equals("item")) clip = profile.resolve(main ? "use_item" : "use_item.offhand");
        if (clip == null) clip = profile.resolve("special_pose." + (action.equals("item") ? "hold" : action) + (right ? "_right" : "_left"));
        double elapsed = using ? (player.getTicksUsingItem() + tick) / 20.0 : age;
        apply(clip, elapsed);
        return clip != null && !(clip.length() > 0 && elapsed > clip.length() && !clip.loop() && !clip.hold()) ? arms(clip) : 0;
    }
    private static boolean charged(ItemStack stack) { return stack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(stack); }
    private static int arms(AnimationProfile.Clip clip) {
        if (clip == null) return 0;
        return (clip.bones().containsKey("right_arm") ? 1 : 0) | (clip.bones().containsKey("left_arm") ? 2 : 0);
    }
    private void apply(AnimationProfile.Clip clip, double elapsed) {
        if (clip == null) return;
        if (clip.length() > 0 && elapsed > clip.length() && !clip.loop() && !clip.hold()) return;
        double time = clip.time(elapsed); context.set("anim_time", time);
        for (var entry : clip.bones().entrySet()) {
            int index = NAMES.indexOf(entry.getKey()); if (index < 0) continue;
            var bone = entry.getValue();
            sample(index, bone.rotation(), 0, 1, time); sample(index, bone.position(), 3, 2, time); sample(index, bone.scale(), 6, 4, time);
        }
    }
    private void sample(int bone, AnimationChannel channel, int offset, int bit, double time) {
        if (channel == null) return;
        active = true; masks[bone] |= bit;
        if (offset == 0) look[bone] = channel.lookQueries();
        for (int axis = 0; axis < 3; axis++) {
            float value = channel.sample(time, axis, context);
            if (!Float.isFinite(value)) throw new IllegalArgumentException("Invalid animation result");
            values[bone][offset + axis] = Math.clamp(value, offset == 6 ? 0 : offset == 0 ? -1440 : -128, offset == 6 ? 4 : offset == 0 ? 1440 : 128);
        }
    }
    private void mergeFoot(int foot, int leg) {
        for (int channel = 0; channel < 3; channel++) if ((masks[foot] & (1 << channel)) != 0) {
            for (int axis = 0; axis < 3; axis++) { int slot = channel * 3 + axis; if (channel == 2) values[leg][slot] *= values[foot][slot]; else values[leg][slot] += values[foot][slot]; }
            masks[leg] |= 1 << channel;
        }
    }
    private void buildRig() {
        for (int i = 0; i < 11; i++) {
            float[] value = values[i], pivot = PIVOT[i];
            inheritedLook[i] = look[i] | (PARENT[i] < 0 ? 0 : inheritedLook[PARENT[i]]);
            // These transforms run in vanilla model space, AFTER LivingEntityRenderer's axis flip.
            // Bedrock-facing humanoid angles therefore keep their signs; only position Y is inverted.
            Quaternionf local = scratchRotation.rotationZYX(rad(value[2]), rad(value[1]), rad(value[0]));
            scratchMatrix.identity().translate(pivot[0] + value[3], pivot[1] - value[4], pivot[2] + value[5]).rotate(local).scale(value[6], value[7], value[8]).translate(-pivot[0], -pivot[1], -pivot[2]);
            if (PARENT[i] < 0) { world[i].set(scratchMatrix); rotation[i].set(local); scale[i].set(value[6], value[7], value[8]); }
            else { int parent = PARENT[i]; world[i].set(world[parent]).mul(scratchMatrix); rotation[i].set(rotation[parent]).mul(local); scale[i].set(scale[parent]).mul(value[6], value[7], value[8]); }
        }
    }
    private static float rad(float degrees) { return degrees * ((float)Math.PI / 180); }
    public boolean hasArm(boolean right) { return active && masks[right ? 5 : 6] != 0; }
    public float armValue(boolean right, int slot) { return values[right ? 5 : 6][slot]; }
    public void apply(HumanoidModel<?> model) {
        if (!active) return;
        part(model.body, 2, false); part(model.head, 4, true); part(model.rightArm, 5, false); part(model.leftArm, 6, false); part(model.rightLeg, 7, false); part(model.leftLeg, 8, false);
    }
    public void applyEquipmentRoot(ModelPart root) {
        if (!active) return;
        world[2].transformPosition(scratchPosition.set(root.x, root.y, root.z)); root.setPos(scratchPosition.x, scratchPosition.y, scratchPosition.z);
        scratchRotation.set(rotation[2]).rotateZYX(root.zRot, root.yRot, root.xRot).getEulerAnglesZYX(scratchAngles);
        root.setRotation(scratchAngles.x, scratchAngles.y, scratchAngles.z);
        root.xScale *= scale[2].x; root.yScale *= scale[2].y; root.zScale *= scale[2].z;
    }
    private void part(ModelPart part, int index, boolean head) {
        int parent = PARENT[index]; float[] value = values[index]; int mask = masks[index];
        if ((mask & 1) != 0) {
            part.xRot = rad(value[0]) + (head && (inheritedLook[index] & 1) == 0 ? part.xRot : 0);
            part.yRot = rad(value[1]) + (head && (inheritedLook[index] & 2) == 0 ? part.yRot : 0); part.zRot = rad(value[2]);
        } else if (head) {
            if ((inheritedLook[index] & 1) != 0) part.xRot = 0;
            if ((inheritedLook[index] & 2) != 0) part.yRot = 0;
        }
        if ((mask & 2) != 0) {
            var initial = part.getInitialPose(); part.x = initial.x() + value[3]; part.y = initial.y() - value[4]; part.z = initial.z() + value[5];
        }
        world[parent].transformPosition(scratchPosition.set(part.x, part.y, part.z)); part.setPos(scratchPosition.x, scratchPosition.y, scratchPosition.z);
        scratchRotation.set(rotation[parent]).rotateZYX(part.zRot, part.yRot, part.xRot).getEulerAnglesZYX(scratchAngles);
        part.setRotation(scratchAngles.x, scratchAngles.y, scratchAngles.z);
        part.xScale = scale[parent].x * ((mask & 4) == 0 ? part.xScale : value[6]);
        part.yScale = scale[parent].y * ((mask & 4) == 0 ? part.yScale : value[7]);
        part.zScale = scale[parent].z * ((mask & 4) == 0 ? part.zScale : value[8]);
    }
}
