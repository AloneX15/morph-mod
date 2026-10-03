# Installation and compatibility

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Installation.md)

## Requirements

Use Fabric and Java 25. Install Morph, Fabric API and GeckoLib on both client and server for the complete feature set. LuckPerms is optional and server only.

| Tested Minecraft | Morph JAR | Development Fabric API | Tested GeckoLib |
|---|---|---|---|
| 26.1.2 | `morphmod-0.3.0+mc26.1.2.jar` | 0.155.3+26.1.2 | 5.5.2 |
| 26.2 | `morphmod-0.3.0+mc26.2.jar` | 0.161.0+26.2 | 5.5.5 |
| 26.3 | `morphmod-0.3.0+mc26.3.jar` | 0.161.0+26.3 | 5.5.7 |

Minimum Fabric Loader: **0.19.5**. The 26.1.2 JAR declares the 26.1.x range; local tests used 26.1.2, not individual 26.1 and 26.1.1 clients. Forge, versions before 26.1 and snapshots are unsupported.

## Client installation

1. Install Fabric Loader for your Minecraft version.
2. Place the matching JAR, Fabric API and GeckoLib in that instance's `mods/` directory.
3. Remove duplicate JARs and JARs for other Minecraft versions.
4. Launch the Fabric profile and open **Options → Controls → Morph**.
5. Try **J → Characters → Otter** in a local world.

Get dependencies from their official projects: [Fabric](https://fabricmc.net/use/), [Fabric API](https://modrinth.com/mod/fabric-api) and [GeckoLib](https://modrinth.com/mod/geckolib). For Morph, use an available repository release or [build it](EN-Building.md); this documentation does not claim Morph has been published on Modrinth or CurseForge.

## Tested compatibility

Server tests cover Lithium and FerriteCore, plus a separate LuckPerms profile. They do not guarantee other modpacks. Mods changing player size, movement, rendering or first person may conflict: see [troubleshooting](EN-Troubleshooting.md).

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
