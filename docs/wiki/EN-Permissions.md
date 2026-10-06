# LuckPerms permissions

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Permissions.md)

LuckPerms is optional. Without it, access follows `free` and unlocks; catalog administration requires OP 2. With LuckPerms, Morph queries the user's effective permissions, including groups, inheritance and current contexts.

## Nodes

| Node | Purpose |
|---|---|
| `morphmod.characters.admin` | Reload catalogs and grant character unlocks |
| `morphmod.character.use.<namespace>.<path>` | Use a character |
| `morphmod.emote.use.<namespace>.<path>.<emote>` | Use a character's emote |
| `morphmod.equipment.toggle` | Show the inventory tab that shows or hides armor and held items on the own morph |

In IDs, `:` and `/` become dots. `mis_personajes:grupo/explorador` becomes `morphmod.character.use.mis_personajes.grupo.explorador`.

## Access rules

- Effective permission **true**: grants access.
- Effective permission **false**: blocks access even when `free` or an unlock would permit it.
- **Undefined** permission: characters follow `free` or unlocks; emotes follow their own `free`.
- Emotes additionally require access to their character.
- Undefined administration permission: OP 2 fallback.
- Undefined equipment toggle permission: OP 2 fallback.

An individual grant may override inherited denial according to LuckPerms resolution. Morph uses the effective result; it does not make every false node from every group win universally. Access loss is checked once per second; selection and playback check access immediately.

## Examples

~~~text
/lp group default permission set morphmod.character.use.morphmod.otter false
/lp creategroup artistas
/lp group artistas permission set morphmod.character.use.morphmod.otter true
/lp user Name parent add artistas
/lp user Name permission set morphmod.emote.use.morphmod.otter.demo_dance true
/lp user Name permission set morphmod.characters.admin true
~~~

To restore default behavior, remove the node using `permission unset`; false is a denial, not a reset. Use real player names.

For contexts, for example a server identified as `survival` in LuckPerms:

~~~text
/lp group artistas permission set morphmod.character.use.morphmod.otter true server=survival
~~~

The context must exist and be active in your LuckPerms configuration. This integration does not add LuckPerms permissions to mob `/morph into`, `unlock` or `reset` commands; those retain OP 2.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
