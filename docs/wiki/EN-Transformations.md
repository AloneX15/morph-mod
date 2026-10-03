# Transformations and unlocks

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Transformations.md)

## Mob forms

With `requireUnlock: true`, a form must be unlocked before selecting it from the menu. With `unlockOnKill: true`, killing a compatible mob adds its type to your unlocked forms. Mob unlocks persist and are copied on death.

Administrative commands can grant unlocks or force transformations. `requireUnlock: false` removes the mob unlock requirement. It does not replace character permissions.

## Statistics and health

Defined attributes are applied through player modifiers. Configuration caps maximum health and multiplies damage. Switching forms preserves the health ratio: 10/20 corresponds to 250/500 before other adjustments. Returning to human removes morph modifiers and passive effects and restores flight according to the game mode.

Mob dimensions and eye height come from the entity type. A bat fits smaller gaps; a large mob needs space. Forced transformations are not guaranteed to automatically find a safe position.

## Generic mobs

The mod accepts entity types with living attributes, excluding its blacklist. Mobs without dedicated definitions inherit available health, damage, armor, toughness and knockback resistance attributes. Their full AI, attacks and special powers are not automatically inherited.

Players, armor stands, mannequins, the dragon and the internal character proxy are excluded. Other mods' mobs depend on their registration and compatibility and are not guaranteed.

## Characters and persistence

Characters use separate unlocks, `free` and permissions. Without a mob link they retain human collision; `scale` only changes appearance. Selection is saved and validated on join; the current form is not copied on death. Unlocks survive death. Emotes do not persist and stop on reconnect or form changes.

`/morph reset` clears **mob** unlocks and returns players to human; it does not clear all character unlocks.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
