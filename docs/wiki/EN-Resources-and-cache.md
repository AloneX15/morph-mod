# Resource distribution and cache

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Resources-and-cache.md)

## Resource flow

The server loads `config/morphmod/characters/` and validates each directory. It publishes an immutable catalog and offers IDs, SHA-256 hashes and sizes. The client identifies cached resources, receives missing archives, validates every ZIP and prepares a session GeckoLib resource pack before activating the catalog.

This happens on connection and after `/morph character reload`. Distribution works for modded clients; manually copying each character onto every computer is unnecessary.

## Limits

| Resource | Limit |
|---|---|
| Characters per catalog | 256 |
| Files per character | 64 |
| Uncompressed size per character | 16 MiB |
| Transferred catalog | 64 MiB |
| Catalog / permission metadata | 32 KiB each |
| Network fragment | 32 KiB |
| Transfer per player per tick | Up to 256 KiB |
| PNG texture | Up to 4096 × 4096 |
| Bones / clips per character | 512 each |
| Declared emote duration | Greater than 0, up to 120 s |

Combined limits matter: 256 characters with long IDs may exceed metadata limits before reaching the character-count maximum.

## Cache

Clients store resources in `.morphmod-cache` inside the game instance. ZIPs are identified by hash; edits change that hash. Cleanup during preparation removes old resources when the cache exceeds 256 MiB while retaining the offered/active catalog's required resources. Old session packs are also removed.

Do not edit this directory to add characters. If recovery requires clearing it, close the game, preserve the log and remove only that instance's cache; the server will transfer resources again on connection.

## Errors and safety

An invalid server reload retains the previous catalog. Clients reject incorrect hashes, truncated files, unsafe paths and unknown queries. Render failures disable the affected character and allow vanilla player rendering. Malformed resources are not automatically repaired.

Disk access and preparation run off the game thread. Configuration transfer has a bounded wait. Preserve `logs/latest.log` if a connection or reload fails.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
