# Animations and profiles

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Animations.md)

## Clip selection

The server does not transmit a walking animation every tick. Clients derive player state and select locomotion, hand, combat and emote clips. `bindings` supports custom names and takes priority over automatic detection.

~~~json
{
  "bindings": {
    "idle": "animation.explorador.idle",
    "walk": "animation.explorador.walk",
    "run": "animation.explorador.sprint",
    "bow_rightArm": "animation.explorador.bow"
  }
}
~~~

Merge this fragment into the manifest; every referenced clip must exist. It is not a complete manifest.

## Locomotion

| Internal action | Detected names / aliases |
|---|---|
| `idle` | idle |
| `walk` | walk |
| `run` | run, sprint |
| `sneak` | sneak, sneaking |
| `sneak_walk` | sneak_walk, sneaking.walk |
| `swim` | swim, swimming, swim_sprint |
| `elytra` | elytra, elytras, elytra_fly |
| `climb` | climb |
| `climb_idle` | climb_idle, climb.idle |
| Other | sleep, sit, riptide, crawl, jump, fall |

State priority: sleeping, riding, riptide, elytra, swimming, crawling, climbing, jumping/falling and ground movement. Missing running falls back to walking; sneak walking to sneaking; stationary climbing to climbing; crawl to swimming. The final fallback is idle.

## Hands and combat

Physical actions: `trident`, `bow`, `crossbow`, `crossbow_charge`, `shield`, `spyglass`, `brush`, `goat_horn`, `eat` and `item` with `_rightArm` or `_leftArm` suffixes.

Legacy aliases: `bow_aim`, `bow_aim.offhand`, `crossbow_aim`, `trident_aim`, `block`, `eat.main_hand`, `eat.off_hand`, `eat`, `eat.offhand`, `use_item`, `use_item.offhand`, `hand_right_interact` and `hand_left_interact`. The main hand may be left: logical hands and physical arms differ.

Combat: `swing.main_hand` and `swing.off_hand`. In `forge-1.20.1` they represent main/offhand; in Fabric profiles they represent right/left arms.

## Special poses and profiles

With items in the hands, `specialpose.<state>` clips and aliases are attempted. Equipment poses include `special_pose.hold_right`, `hold_left`, `bow_right`, `bow_left`, `crossbow_right`, `crossbow_left`, `shield_right`, `shield_left`, `spyglass_right`, `spyglass_left` and `hold_both` under the `special_pose.` prefix.

Valid profiles are `fabric-1.21.1`, `fabric-1.20.4` and `forge-1.20.1`. They define conventions, not other loaders. Every historical Morpher animation is not guaranteed to have identical semantics here: use bindings and test your rig's actions. Missing clips are not generated.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
