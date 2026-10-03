# Server setup

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Server-setup.md)

## Preparation

Install a Fabric server with Java 25 and Minecraft-matching Morph, Fabric API and GeckoLib JARs. Use the same Morph version on both sides. LuckPerms is optional and server only; the tested development profile uses LuckPerms Fabric 5.5.85.

1. Back up an existing world.
2. Place dependencies in the server's `mods/` directory.
3. Start it and inspect the log.
4. Connect using a client with matching dependencies.
5. Test a mob form and `morphmod:otter`.

## Created files

~~~text
config/
  morphmod.json
  morphmod/
    characters/
      otter/
        character.json
        otter.geo.json
        otter.animation.json
        otter.png
~~~

The template is copied when the Otter directory does not exist. Edit the server catalog, not client caches.

## Managing resources

Place each character in a separate directory and run `/morph character reload`. This validates and transfers the entire catalog. It does not reload `morphmod.json`: restart the server to change general configuration.

~~~text
/morph character reload
/morph character unlock mis_personajes:explorador Name
/morph unlock minecraft:bat Name
~~~

The console can reload and perform operations accepting targets. Personal commands require a player: `character select` from the console does not transform another player.

## Access and diagnosis

Administrative operations require OP 2 by default. LuckPerms controls catalog administration and character/emote access but does not replace OP requirements on administrative mob commands.

Mob commands remain available without the client's mod channels; such clients lack the mod UI and models. Install Morph on both sides for the complete feature set. See [network and cache](EN-Resources-and-cache.md) and [troubleshooting](EN-Troubleshooting.md).

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
