# Updating and backups

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Updating-and-backups.md)

## What to back up

With the server stopped, copy the complete world including player data, `config/morphmod.json` and `config/morphmod/characters/`. With LuckPerms, include its data/configuration or the backup appropriate to its storage backend.

Unlocks and selection are stored as attachments in player data, not a separate Morph permissions file. Client cache is regenerable and does not replace a catalog backup.

## Updating Morph or Minecraft

1. Read the changelog and compatibility matrix.
2. Preserve current JARs and make the backup.
3. Test on a copy of the server and world.
4. Install the correct JAR and new-version dependencies on server and clients.
5. Remove duplicates.
6. Start and inspect logs; test selection, returning to human, permissions and emotes from two clients.
7. Update production after successful testing.

Loading worlds converted by newer Minecraft into older versions is not guaranteed.

## Updating characters

Keep IDs stable to retain the association with unlocks and permission nodes. Changing `id` creates another identity even with the same display name. Update channels and anchors when renaming bones.

Keep a copy of the old catalog, edit resources and run `/morph character reload`. Failed validation retains the previous version. Inspect client logs too: server acceptance does not guarantee all expressions or rendering are valid on clients.

## Rolling back

Stop the server and restore the coherent world, configuration, characters and JAR set you backed up. Do not restore only an older JAR onto an already converted world without testing.

Configuration uses schema 1 and preserves future-schema files. Current network protocol: 2. Character manifests use schema 1. This wiki does not promise compatibility with future versions.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
