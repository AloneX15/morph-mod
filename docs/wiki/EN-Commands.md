# Command reference

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Commands.md)

Values inside `< >` are required; `[ ]` indicates optional arguments. Enter values without those symbols. `targets` accepts players and Minecraft selectors. Use complete IDs such as `minecraft:bat` and `morphmod:otter`.

## Mobs

| Command | Access | Result |
|---|---|---|
| `/morph list` | Player | Lists unlocked forms |
| `/morph into <mob> [targets]` | OP 2 | Forces a mob form |
| `/morph clear [targets]` | OP 2 | Returns to human |
| `/demorph` | OP 2, player | Personal return-to-human alias |
| `/morph unlock <mob> [targets]` | OP 2 | Unlocks one mob |
| `/morph unlock all [targets]` | OP 2 | Unlocks compatible mobs |
| `/morph reset <targets>` | OP 2 | Clears mob unlocks and returns to human |

Without `targets`, optional-target commands affect the executing player. The console must provide targets where applicable. Forced transformations do not require prior unlocks.

## Characters and emotes

| Command | Access | Result |
|---|---|---|
| `/morph character list` | Player | Lists allowed characters |
| `/morph character select <id>` | Authorized player | Selects your character |
| `/morph character clear` | Player | Returns to human, including from a mob form |
| `/morph character reload` | Catalog administrator | Reloads and transfers resources |
| `/morph character unlock <id> <targets>` | Catalog administrator | Grants a character unlock |
| `/morph emote list` | Player | Lists allowed emotes of the current character |
| `/morph emote play <id> [mode]` | Authorized player | Plays an emote; default mode `full` |
| `/morph emote stop` | Player | Stops your emote |

`mode` is `full` or `overlay` and must be allowed by the emote. Catalog administrator means `morphmod.characters.admin` with OP 2 as the default fallback. An explicit LuckPerms denial blocks that access.

## Examples

~~~text
/morph unlock minecraft:bat @a
/morph into minecraft:warden Name
/morph clear Name
/morph character reload
/morph character unlock mis_personajes:explorador Name
/morph character select morphmod:otter
/morph emote play demo_dance overlay
/morph emote stop
~~~

Personal character commands do not accept targets. There is no `/morph config reload` command or command that generates missing dance clips.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
