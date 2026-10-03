# Mob catalog and statistics

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Mob-catalog.md)

The following values belong to Morph 0.3.0's ten dedicated definitions before configuration, equipment and other effects. Unspecified attributes keep player behavior, apart from entity-type dimensions.

| Minecraft ID | Maximum health | Defined damage | Defined speed | Other statistics |
|---|---:|---:|---:|---|
| `minecraft:warden` | 500 | 30 | 0.085 | Attack knockback 1.5; knockback resistance 1; step height 1 |
| `minecraft:creeper` | 20 | — | — | — |
| `minecraft:enderman` | 40 | 7 | 0.12 | Step height 1; block reach 6.5; entity reach 4 |
| `minecraft:blaze` | 20 | 6 | — | — |
| `minecraft:spider` | 16 | 2 | 0.12 | — |
| `minecraft:bat` | 6 | — | — | Flying speed 0.06 |
| `minecraft:iron_golem` | 100 | 15 | 0.08 | Knockback resistance 1; step height 1 |
| `minecraft:skeleton` | 20 | — | — | — |
| `minecraft:ghast` | 10 | — | — | Flying speed 0.03 |
| `minecraft:axolotl` | 14 | 2 | — | Water movement efficiency 1 |

Health is measured in points: 20 points equal ten normal hearts. Speed is an attribute value, not blocks per second. Resistance 1 represents 100%.

## Dimensions

Collision and eye height come from the installed Minecraft entity type. Common width × height references: Warden 0.9 × 2.9, Creeper 0.6 × 1.7, Enderman 0.6 × 2.9, spider 1.4 × 0.9, bat 0.5 × 0.9, golem 1.4 × 2.7 and Ghast 4 × 4. The entity type is authoritative for exact values in your version.

## Server adjustments

`maxHealthCap` caps maximum health; `damageMultiplier` adjusts defined damage. These options do not change this base table. For example, with a cap of 100 a Warden does not receive 500 health.

Other compatible mobs use generic definitions without a promise of dedicated powers. See [abilities and passives](EN-Abilities.md) for the mechanics of these ten forms.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
