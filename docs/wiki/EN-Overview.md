# Overview

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Overview.md)

Morph lets you adopt mob appearances and mechanics, or use custom characters animated with GeckoLib. This wiki describes **Morph 0.3.0** as implemented.

## Two kinds of transformation

| Type | Appearance | Statistics and collision | Abilities |
|---|---|---|---|
| Mob | Mob model and animations | Defined by its morph | Active and passive abilities where defined |
| Custom character | Imported geometry, texture and animations | Human by default | Human unless explicitly linked to a mob |

Characters may include full or overlay dances. The server selects forms and enforces permissions; clients render models and receive catalog resources.

## Start here

- Players: [installation](EN-Installation.md) and [getting started](EN-Getting-started.md).
- Administrators: [servers](EN-Server-setup.md), [configuration](EN-Configuration.md) and [permissions](EN-Permissions.md).
- Creators: [importing](EN-Importing-characters.md), [animations](EN-Animations.md) and [Otter](EN-Otter-tutorial.md).
- Developers: [building](EN-Building.md) and [architecture](EN-Architecture.md).

![Otter in game](images/otter.png)

## Available and planned features

There are ten dedicated mob definitions, generic support for other compatible mobs, a server character catalog, emotes and optional LuckPerms. Datapack morph definitions, Mod Menu/Cloth Config integration and automatic friendliness from mobs of your species are not implemented. Dances require animation clips in your resources: the mod does not generate animations.

This wiki accompanies the local 0.3.0 implementation; it does not imply that a public release of this version already exists.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
