# Abilities and passives

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Abilities.md)

## Active abilities

Base cooldowns before `cooldownMultiplier`; 20 ticks equal one second at normal tick rate.

| Form / slot | Ability | Cooldown | Behavior |
|---|---|---:|---|
| Warden / R | Sonic boom | 3 s | 15-block beam; 10 base sonic boom damage to hit creatures, excluding yourself |
| Warden / K | Darkness pulse | 15 s | Darkness for 12 seconds within 20 blocks |
| Creeper / R | Explosion | 10 s | Power 3; no self damage or knockback |
| Enderman / R | Teleport | 3 s | Up to 32 blocks; validates destination and collision |
| Blaze / R | Fireballs | 2 s | Three scattered small fireballs |
| Golem / R | Fling | 4 s | Living target ahead, up to 5 blocks; attack damage and upward force |
| Skeleton / R | Arrow | 1 s | No bow or ammunition; cannot be picked up |
| Ghast / R | Explosive fireball | 3 s | Large fireball; explosion power follows configuration |
| Axolotl / R | Play dead | 60 s | Regeneration II and Slowness IV for 10 seconds |

Spider and bat have passives but no dedicated active powers. An unsuccessful activation does not consume cooldown if the ability returns failure. The server decides targets, distances and effects. Sonic boom uses vanilla's `sonic_boom` damage type.

## Passives by form

| Form | Passives |
|---|---|
| Warden | Darkness immunity; vibration sense |
| Creeper | No fall damage |
| Enderman | Water and rain sensitivity |
| Blaze | Fire immunity; slow falling; water sensitivity |
| Spider | Wall climbing; night vision |
| Bat | Flight; night vision; no fall damage |
| Golem | No fall damage |
| Skeleton | Burns in sunlight; a helmet prevents that check |
| Ghast | Flight; fire immunity; no fall damage |
| Axolotl | Water breathing; fast swimming |

The Warden highlights nearby moving entities, excluding crouching entities, within its 24-block vibration range. It does not make other mobs friendly. Flight depends on `allowFlight`.

Explosion block destruction depends on `abilitiesBreakBlocks`. The Creeper explosion breaks blocks like the real mob (`creeperBreaksBlocks`, on by default) as long as the `mobGriefing` gamerule allows it; set `creeperBreaksBlocks: false` to keep blocks intact.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
