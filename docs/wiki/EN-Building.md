# Environment and building

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Building.md)

## Requirements and structure

Use JDK 25 and the repository's Gradle wrapper. IntelliJ IDEA can import the Gradle project; the Minecraft Development plugin is optional.

Stonecutter shares code across 26.1.2, 26.2 and 26.3. The active version is 26.3. Dependencies and `mod.version` are centralized in `stonecutter.properties.toml`; common and client code are separated into `src/main` and `src/client`.

## Build

On Windows PowerShell use `.\gradlew.bat`; on Linux/macOS or Git Bash use `./gradlew`.

~~~powershell
.\gradlew.bat :26.1.2:build :26.2:build :26.3:build
.\gradlew.bat :26.3:runClient
.\gradlew.bat :26.3:genSources
~~~

`build` compiles and runs configured checks, including unit and server tests. Installable JARs are in `versions/<mc>/build/libs/`. `-sources.jar` is for development, not installation in mods.

## Optional tests

~~~powershell
.\gradlew.bat :26.3:runGameTest -PcompatPack
.\gradlew.bat :26.3:runGameTest -PluckPerms
.\gradlew.bat :26.3:runClientGameTest
~~~

`compatPack` adds Lithium/FerriteCore to the development environment. `luckPerms` adds LuckPerms for permission regression; it does not make it a mandatory product dependency.

## Version differences

Use Stonecutter branches for incompatible APIs and compile the full matrix. Do not treat generated version copies as the primary source. Review [architecture](EN-Architecture.md) before changing rendering, attachments or negotiation.

Client tests require graphics. Preserve screenshots before running same-version server tests: Loom cleanup removes both harness directories.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
