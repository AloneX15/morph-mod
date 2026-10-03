# character.json reference

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Character-manifest.md)

`character.json` is a catalog directory's manifest. Use JSON without comments. Geometry, texture and animation files must exist inside that directory.

## Main fields

| Field | Required / default | Meaning |
|---|---|---|
| `schemaVersion` | Required: 1 | Format version |
| `id` | Required | Unique lowercase `namespace:path` identifier; maximum 160 characters, no `..` |
| `name` | Required | Nonempty display name, maximum 128 characters |
| `model` | Required | Relative geometry path |
| `texture` | Required | Relative PNG path |
| `animations` | Required | Relative animation path |
| `free` | Optional: true | Default access without unlock |
| `scale` | Optional: 1 | Finite visual scale from 0.1 to 4 |
| `mob` | Optional: no link | Mob ID providing mechanics |
| `profile` | Optional: `fabric-1.21.1` | Animation naming convention |
| `bindings` | Optional: empty | Internal action → clip name |
| `anchors` | Optional: empty | Equipment and first-person attachment points |
| `emotes` | Optional: empty | Available emotes |

Paths allow lowercase letters, digits, `_`, `.`, `/` and `-`, up to 160 characters. Absolute paths, spaces, backslashes and `..` are rejected. IDs allow those path characters and a namespace of lowercase letters, digits, `_`, `.` or `-`.

## Minimal valid example using Otter resources

~~~json
{
  "schemaVersion": 1,
  "id": "mis_personajes:explorador",
  "name": "Explorador",
  "model": "otter.geo.json",
  "texture": "otter.png",
  "animations": "otter.animation.json"
}
~~~

Place Otter's three resource files beside this manifest. It creates another character with the otter appearance; changing a name does not generate new geometry.

## Anchors

Each `anchors` entry contains `bone` and optionally `position`, `rotation` and `scale`. These are three-number finite vectors with maximum absolute value 128. Defaults: position and rotation [0,0,0], scale [1,1,1]. An unknown bone causes that anchor to be omitted.

## Emotes

| Field | Default / requirement |
|---|---|
| `animation` | Required; existing clip |
| `loop` | false |
| `modes` | [`"full"`]; accepts full/overlay and cannot be empty |
| `bones` | Empty; must be nonempty when overlay is allowed |
| `free` | true |
| `seconds` | 5; finite, greater than 0 and up to 120 |

Emote IDs allow lowercase letters, digits, `_`, `.` and `-`, up to 64 characters. Mask bones must exist. For bindings and equipment, see [animations](EN-Animations.md) and [anchors](EN-Equipment.md).

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
