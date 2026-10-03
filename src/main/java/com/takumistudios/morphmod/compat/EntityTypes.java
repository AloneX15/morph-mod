package com.takumistudios.morphmod.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

/** Registry IDs are stable across the EntityType / EntityTypes split in 26.2. */
public final class EntityTypes {
	public static final EntityType<?> PLAYER = type("player");
	public static final EntityType<?> MANNEQUIN = type("mannequin");
	public static final EntityType<?> ARMOR_STAND = type("armor_stand");
	public static final EntityType<?> ENDER_DRAGON = type("ender_dragon");
	public static final EntityType<?> WARDEN = type("warden");
	public static final EntityType<?> CREEPER = type("creeper");
	public static final EntityType<?> ENDERMAN = type("enderman");
	public static final EntityType<?> BLAZE = type("blaze");
	public static final EntityType<?> SPIDER = type("spider");
	public static final EntityType<?> BAT = type("bat");
	public static final EntityType<?> IRON_GOLEM = type("iron_golem");
	public static final EntityType<?> SKELETON = type("skeleton");
	public static final EntityType<?> GHAST = type("ghast");
	public static final EntityType<?> AXOLOTL = type("axolotl");
	private EntityTypes() { }
	private static EntityType<?> type(String id) {
		return BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.fromNamespaceAndPath("minecraft", id)).orElseThrow();
	}
}
