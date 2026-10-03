# Architecture and synchronization

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Architecture.md)

## Server authority

`MorphRegistry` resolves dedicated and fallback mob definitions. `MorphManager` manages transformation, attributes, flight, passives and cooldowns. `CharacterManager` controls selection, emotes and permissions. Commands and packets call these operations; clients do not decide targets or damage.

Current form and character are stored as synchronized attachments. Mob unlocks synchronize only to their owner; character catalog access uses metadata. Emotes are transient and contain ID, mode and starting time.

## Catalog

`CharacterCatalog` performs IO outside ticks and publishes immutable snapshots. `CharacterBundle` validates resources and generates ZIPs/hashes. `CharacterNetworking` negotiates configuration/play resources, transmits fragments within per-tick budgets and sends per-user access.

On the client, `ClientCharacters` validates files, MoLang and hashes, prepares a session pack and activates the catalog. Disconnects and new offers invalidate old work to prevent applying stale sessions.

## Rendering

Mobs use representation entities that are never added to the world. Characters use `CharacterEntity`, a separate GeckoLib proxy reflecting player pose, equipment, rotation and movement. Controllers combine locomotion, hands, combat and emotes.

World rendering replaces visible render states; inventory uses a dedicated hook. Local avatar extraction is preserved for camera and vanilla items. A hand hook draws custom arms. Do not globally replace the local avatar state with a generic state: Minecraft depends on its original type.

## Networking and reliability

Current protocol: **2**. The server validates negotiation, state, IDs, unlocks, slots and request limits. Commands remain available without client channels. Attachments transmit state changes; complete poses are not sent every frame.

Critical callbacks use guards and renderers retain fallback paths. Selection updates attributes on transitions and ticks process active UUIDs. The eight mixins are documented in the repository's MIXINS document.

## LuckPerms

The integration is isolated and optional. It queries effective permissions/contexts; undefined states use defaults. Avoid optional-class references in paths loaded without LuckPerms.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
