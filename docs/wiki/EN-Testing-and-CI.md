# Testing and CI

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Testing-and-CI.md)

## Local validation

Run each server profile in a separate invocation. Gradle deduplicates identical tasks within one invocation.

~~~powershell
node scripts/check-standards.mjs
.\gradlew.bat :26.1.2:build :26.2:build :26.3:build
.\gradlew.bat :26.1.2:runGameTest :26.2:runGameTest :26.3:runGameTest -PcompatPack
.\gradlew.bat :26.1.2:runGameTest :26.2:runGameTest :26.3:runGameTest -PluckPerms
.\gradlew.bat :26.1.2:runClientGameTest :26.2:runClientGameTest :26.3:runClientGameTest --no-parallel
~~~

Clients run sequentially because the dedicated harness uses the same port, 25565. The two-client test launches a separate observer, checks state and shuts down its processes.

## Coverage

| Level | Scenarios |
|---|---|
| Unit | Configuration, cooldowns, limits, corrupt resources, hashes, fragments, aliases and masks |
| Server | Attributes, dimensions, passives, abilities, selection, emotes and permissions |
| Client | Menus, small/Spanish UI, models, equipment, arms, cooldown and return to human |
| Multiplayer | Transfer, observer, full/overlay, reconnects, reloads and invalid catalogs |
| Compatibility | Server with Lithium/FerriteCore; separate LuckPerms profile |

Local result on October 3, 2026 for 0.3.0: 30 unit tests per version; eight gametests per version including smoke passed on server profiles; client and multiplayer passed on all three versions. This does not imply a green remote CI run for these changes.

## Reports

Unit reports are in `versions/<mc>/build/reports/tests/test/`. Harness logs and screenshots are in `versions/<mc>/build/run/`. The local review preserved screenshots under `build/verification/0.3.0/`. Another harness cleanup may remove originals: copy them first.

## GitHub Actions

The build workflow checks standards, server and client matrices; graphics tests use a virtual display and Mesa. A main push may publish the dev prerelease after the matrix passes. The release workflow checks `vX.Y.Z` against `mod.version`, extracts changelog notes and builds JARs.

Modrinth/CurseForge require configured IDs and secrets. Do not publish a tag merely to check documentation. Workflows are prepared; verify actual runs after publishing mod changes.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
