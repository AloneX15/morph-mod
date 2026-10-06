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

An anchor contains a bone name and adjustments. Armor pieces are fitted automatically to the largest cube of the bone (or of its nearest parent with cubes, for empty locator bones such as `armorBipedHead`). For armor anchors, `scale` multiplies that automatic fit (1 = fitted to the bone) and `position` moves the piece in model units (16 units equal one block); `rotation` is not used for armor. For held items (`right_hand`, `left_hand`) position, rotation (degrees) and scale are local transforms of the item.

## Otter rig example

~~~json
{
  "anchors": {
    "right_hand": {"bone": "right_hand_item"},
    "left_hand": {"bone": "left_hand_item"},
    "head": {"bone": "armorBipedHead"},
    "chest": {"bone": "body_upper", "position": [0, 3.6, 0]},
    "right_arm": {"bone": "right_arm", "position": [0.5, 0.4, 0], "scale": [1.3, 1.0, 1.3]},
    "left_arm": {"bone": "left_arm", "position": [-0.5, 0.4, 0], "scale": [1.3, 1.0, 1.3]},
    "right_leg": {"bone": "right_leg", "position": [0, 0.7, 0]},
    "left_leg": {"bone": "left_leg", "position": [0, 0.7, 0]},
    "right_foot": {"bone": "right_leg", "position": [0, 0.7, 0]},
    "left_foot": {"bone": "left_leg", "position": [0, 0.7, 0]},
    "elytra": {"bone": "body_upper", "position": [0, 3.6, 0]}
  }
}
~~~

The offsets compensate for pivots that are not where a player's are: `body_upper` pivots near the bottom of its cube, so the chestplate is raised 3.6 units. The X axis is mirrored by GeckoLib, so a positive X offset moves the right arm piece outwards.

Merge this fragment into a manifest using those bones. For your own rig, change names and offsets. Unknown anchors are omitted; they do not alone invalidate the character, and equipment keeps its gameplay effects.

## Showing equipment on the morph

Armor and held items are **hidden** on every morph (mobs and characters) by default; gameplay effects are unchanged. Players with the `morphmod.equipment.toggle` permission (operators level 2 without LuckPerms) see a chestplate tab to the right of the inventory. It toggles their own morph between showing and hiding armor and held items, and every player sees the result. The setting is kept across deaths and reconnections; it is removed if the player loses the permission.

## In-game adjustment

1. Turn the equipment tab on, load the model without equipment and verify visual scale.
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
