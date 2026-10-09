package com.takumistudios.morphmod.character;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.*;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import com.takumistudios.morphmod.MorphMod;
import java.util.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** Detached render proxy. Never spawned or ticked in a server world. */
public final class CharacterEntity extends ArmorStand implements GeoEntity {
    public static final EntityType<CharacterEntity> TYPE = Registry.register(BuiltInRegistries.ENTITY_TYPE, MorphMod.id("character_proxy"),
        EntityType.Builder.<CharacterEntity>of(CharacterEntity::new, MobCategory.MISC).sized(0.6F, 1.8F).clientTrackingRange(0)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, MorphMod.id("character_proxy"))));
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Map<String, RawAnimation> loops = new HashMap<>();
    private final Map<String, RawAnimation> singles = new HashMap<>();
    public CharacterDefinition definition;
    public Player owner;
    public String locomotion = "idle";
    public boolean specialPose;
    public CharacterManager.EmoteState emote;
    private String lastEmote = "";
    public CharacterEntity(EntityType<? extends ArmorStand> type, Level level) { super(type, level); setNoGravity(true); setSilent(true); }
    public static void init() { FabricDefaultAttributeRegistry.register(TYPE, ArmorStand.createAttributes()); }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<CharacterEntity>("movement", 4, test -> {
            if (definition == null || (emote != null && emote.mode().equals("full"))) return stop(test);
            return clip(test, AnimationResolver.movement(definition, locomotion, specialPose), true);
        }));
        controllers.add(new AnimationController<CharacterEntity>("right_arm", 2, test -> arm(test, true)));
        controllers.add(new AnimationController<CharacterEntity>("left_arm", 2, test -> arm(test, false)));
        controllers.add(new AnimationController<CharacterEntity>("both_hands", 2, test -> {
            if (definition == null || owner == null || owner.isUsingItem() || owner.getMainHandItem().isEmpty() || owner.getOffhandItem().isEmpty()
                || (emote != null && emote.mode().equals("full"))) return stop(test);
            return clip(test, AnimationResolver.resolve(definition, "special_pose.hold_both"), true);
        }));
        controllers.add(new AnimationController<CharacterEntity>("combat", 0, test -> {
            if (owner == null || definition == null || !com.takumistudios.morphmod.compat.PlayerAnimationState.swinging(owner) || (emote != null && emote.mode().equals("full"))) return stop(test);
            return clip(test, AnimationResolver.swing(definition, com.takumistudios.morphmod.compat.PlayerAnimationState.hand(owner) == net.minecraft.world.InteractionHand.MAIN_HAND, owner.getMainArm() == HumanoidArm.RIGHT), false);
        }));
        controllers.add(new AnimationController<CharacterEntity>("emote", 0, test -> {
            if (emote == null || definition == null) { lastEmote = ""; return stop(test); }
            var entry = definition.emotes().get(emote.id()); if (entry == null) return stop(test);
            String name = emote.mode().equals("overlay") ? "__morph_overlay_" + emote.id() : entry.animation();
            PlayState result = clip(test, name, entry.loop());
            String key = emote.encode();
            if (!key.equals(lastEmote)) {
                test.controller().setAnimationTime(Math.max(0, (level().getGameTime() - emote.start()) / 20.0)); lastEmote = key;
            }
            return result;
        }));
    }
    /** GeckoLib keeps looping a stopped clip and holds its pose over later controllers (a finished dance froze the arms); reset clears it. */
    private static PlayState stop(AnimationTest<CharacterEntity> test) { test.controller().reset(); return PlayState.STOP; }
    private PlayState clip(AnimationTest<CharacterEntity> test, String clip, boolean loop) {
        if (clip.isEmpty()) return stop(test);
        return test.setAndContinue((loop ? loops : singles).computeIfAbsent(clip, name -> loop ? RawAnimation.begin().thenLoop(name) : RawAnimation.begin().thenPlay(name)));
    }
    private PlayState arm(AnimationTest<CharacterEntity> test, boolean right) {
        if (owner == null || definition == null || (emote != null && emote.mode().equals("full"))) return stop(test);
        boolean main = (owner.getMainArm() == HumanoidArm.RIGHT) == right;
        boolean active = owner.isUsingItem() && (owner.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND) == main;
        ItemStack stack = main ? owner.getMainHandItem() : owner.getOffhandItem();
        String action = "item";
        if (active) action = switch (stack.getUseAnimation()) {
            case EAT, DRINK -> "eat"; case BLOCK -> "shield"; case BOW -> "bow"; case CROSSBOW -> "crossbow_charge";
            case SPEAR -> "trident"; case SPYGLASS -> "spyglass"; case BRUSH -> "brush"; case TOOT_HORN -> "goat_horn"; default -> "item";
        };
        else if (stack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(stack)) action = "crossbow";
        String clip = AnimationResolver.arm(definition, action, right, main, active);
        if (active && action.equals("eat")) {
            String explicit = AnimationResolver.resolve(definition, main ? "eat.main_hand" : "eat.off_hand");
            if (!explicit.isEmpty()) clip = explicit;
        }
        if (clip.isEmpty() && specialPose) clip = AnimationResolver.resolve(definition, "special_pose." + (action.equals("item") ? "hold" : action) + (right ? "_right" : "_left"));
        return stack.isEmpty() ? stop(test) : clip(test, clip, true);
    }
    public void copy(Player player) {
        owner = player; emote = CharacterManager.emote(player);
        specialPose = !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty();
        setPos(player.position()); xo = player.xo; yo = player.yo; zo = player.zo; xOld = player.xOld; yOld = player.yOld; zOld = player.zOld;
        setYRot(player.getYRot()); setXRot(player.getXRot()); yRotO = player.yRotO; xRotO = player.xRotO;
        yBodyRot = player.yBodyRot; yBodyRotO = player.yBodyRotO; yHeadRot = player.yHeadRot; yHeadRotO = player.yHeadRotO;
        tickCount = player.tickCount; hurtTime = player.hurtTime; deathTime = player.deathTime;
        setDeltaMovement(player.getDeltaMovement()); setOnGround(player.onGround()); setPose(player.getPose()); setShiftKeyDown(player.isShiftKeyDown());
        setSprinting(player.isSprinting()); setSwimming(player.isSwimming());
        setInvisible(player.isInvisible()); setRemainingFireTicks(player.getRemainingFireTicks());
        boolean equipment = com.takumistudios.morphmod.data.MorphAttachments.showsEquipment(player);
        for (EquipmentSlot slot : EquipmentSlot.values()) setItemSlot(slot, equipment ? player.getItemBySlot(slot) : ItemStack.EMPTY);
        elytraAnimationState.tick();
        boolean moving = player.walkAnimation.isMoving() || player.getDeltaMovement().horizontalDistanceSqr() > 0.0001;
        locomotion = player.isSleeping() ? "sleep" : player.isPassenger() ? "sit" : player.isAutoSpinAttack() ? "riptide" : player.isFallFlying() ? "elytra"
            : player.isSwimming() ? "swim" : player.getPose() == Pose.SWIMMING ? "crawl" : player.onClimbable() ? (moving ? "climb" : "climb_idle")
            : !player.onGround() ? (player.getDeltaMovement().y > 0 ? "jump" : "fall") : player.isCrouching() ? (moving ? "sneak_walk" : "sneak")
            : moving ? (player.isSprinting() ? "run" : "walk") : "idle";
    }
    @Override public HumanoidArm getMainArm() { return owner == null ? HumanoidArm.RIGHT : owner.getMainArm(); }
}
