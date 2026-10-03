# Equipment and first-person arms

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Equipment.md)

## Available anchors

| Name | Purpose |
|---|---|
| `right_hand`, `left_hand` | Held items |
| `head` | Helmet |
| `chest` | Chestplate |
| `right_arm`, `left_arm` | Armor segments and first-person arm selection |
| `right_leg`, `left_leg` | Leg segments |
| `right_foot`, `left_foot` | Boots |
| `elytra` | Elytra |

An anchor contains a bone name and local transforms. Position uses model units: 16 units equal one block. Rotation uses degrees. Scale is a multiplier.

## Otter rig example

~~~json
{
  "anchors": {
    "right_hand": {"bone": "right_hand_item"},
    "left_hand": {"bone": "left_hand_item"},
    "head": {"bone": "armorBipedHead"},
    "right_arm": {"bone": "right_arm", "scale": [0.6, 0.6, 0.6]},
    "left_arm": {"bone": "left_arm", "scale": [0.6, 0.6, 0.6]},
    "chest": {"bone": "body_upper", "scale": [0.6, 0.6, 0.6]},
    "elytra": {"bone": "body_upper", "scale": [0.6, 0.6, 0.6]}
  }
}
~~~

Merge this fragment into a manifest using those bones. For your own rig, change names and offsets. Unknown anchors are omitted; they do not alone invalidate the character, and equipment keeps its gameplay effects.

## In-game adjustment

1. Load the model without equipment and verify visual scale.
2. Hold a sword and an offhand item; inspect both anchors.
3. Use a bow, shield and consumables to check poses and the active hand.
4. Equip helmet, chestplate, leggings, boots and elytra.
5. Check first person with empty and occupied hands, using both arms.
6. Adjust offsets and reload the catalog.

First person preserves Minecraft's camera and items while replacing the arm where the rig supports it. Pivot hierarchy and descendant bones matter. Different rigs may require different offsets and armor adjustments.

![Otter first-person arm](images/first-person-arm.png)

Character `scale` changes visual size, not human collision. Anchor scales affect equipment placement, not its attributes.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
