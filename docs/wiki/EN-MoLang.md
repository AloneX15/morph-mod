# MoLang expressions

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-MoLang.md)

MoLang allows animation channels to react to time, movement or player state. Morph validates channel expressions before accepting a client catalog.

## Supported queries

| Group | Queries |
|---|---|
| Time | `query.anim_time`, `query.life_time` |
| Movement | `query.ground_speed`, `query.limb_swing`, `query.limb_swing_amount` |
| Look | `query.pitch`, `query.yaw`, `query.head_x_rotation`, `query.head_y_rotation` |
| Combat | `query.right_hand_swing`, `query.left_hand_swing` |
| State | `query.is_sneaking`, `query.is_sprinting`, `query.is_swimming`, `query.is_on_ground` |

`pitch`, `yaw` and hand swings are provided explicitly for Morph characters. Standard queries come from GeckoLib context and the render proxy. Do not assume universal physical units for speed or swing values: verify their visual effect on your character.

## Channel example

~~~json
{
  "rotation": [
    "math.sin(query.anim_time * 180) * 8",
    0,
    0
  ]
}
~~~

This is a bone-channel fragment inside a clip, not a complete animation file. Start with a small oscillation to confirm the channel is active before adding complex expressions.

## Validation and diagnosis

- Bone names must exist in the geometry.
- A query outside the supported list causes client catalog rejection.
- Expressions must compile with GeckoLib's parser.
- Format interpolation values, such as linear/catmullrom, are not treated as queries.
- Test walking, sprinting, crouching, swimming and changing your main arm.

If a model works in Blockbench but fails on connection, inspect its queries and client log. Blockbench's simulator does not establish that a query exists in Morph.

Otter uses a procedural idle that already responds to movement. Adding empty walk and sprint clips is unnecessary to simulate that behavior.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
