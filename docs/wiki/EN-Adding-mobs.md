# Adding mobs and abilities

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Adding-mobs.md)

## Choose the intervention

A registered mob with living attributes may work through fallback without dedicated code. Add a dedicated definition for specific statistics, passives or powers. This is not a stable public API or datapack definition loader.

## Register a definition

Use the project's builder in the common registry. This example does not add an active power:

~~~java
register(MorphDefinition.builder(EntityTypes.PHANTOM)
    .stat(Attributes.MAX_HEALTH, 20.0)
    .passive(Passive.FLIGHT, Passive.BURNS_IN_SUN)
    .build());
~~~

Place it inside `MorphRegistry.init()` with appropriate imports. Do not change exclusions to allow entities incompatible with player rendering or dimensions without reviewing those systems.

## Implement a power

`MorphAbility` requires `id()`, `cooldownTicks()` and `activate(ServerPlayer)`. Illustrative healing implementation to register as primary or secondary:

~~~java
public final class RecoverAbility implements MorphAbility {
    public String id() { return "recover"; }
    public int cooldownTicks() { return 100; }
    public boolean activate(ServerPlayer player) {
        if (!player.isAlive() || player.getHealth() >= player.getMaxHealth()) return false;
        player.heal(2.0F);
        return true;
    }
}
~~~

Add `.primary(new RecoverAbility())` to the builder. The example requires imports for MorphAbility and ServerPlayer. `true` starts cooldown; `false` indicates no activation. Never trust client-supplied damage, reach or targets.

## Translation and tests

Add `ability.morphmod.recover` to both mod languages. Document statistics, damage, cooldown and passives; update the changelog. Test unlocks, selection, attributes, dimensions, activation, cooldown and returning to human.

For projectiles and teleportation, test collision and invalid destinations. For rendering, test third/first person and a second client. Run the multiversion and compatibility matrix when changing the core.

Internal Minecraft classes may vary across versions: adapt through Stonecutter rather than unverified independent copies.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
