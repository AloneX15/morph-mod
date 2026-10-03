# Troubleshooting

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Troubleshooting.md)

## Quick diagnosis

| Symptom | Check / remedy |
|---|---|
| Startup fails | Check Minecraft, Java 25, Loader and dependency JARs; remove duplicates |
| J does not open menu | Check Controls → Morph and binding conflicts |
| Cannot select mob | Check unlocks and `requireUnlock`; use `/morph list` |
| Character missing | Check server directory, reload, messages and permissions |
| Reload rejected | Check JSON, resources, duplicate IDs, bones and limits |
| Vanilla model after selection | Inspect client log for GeckoLib loading, queries or render failures |
| Dance missing | Select character; check emote registration and permission |
| Full dance immediately stops | Stop moving/using items; wait until stationary |
| Overlay moves legs | Check mask and parents that transform the legs |
| Equipment misplaced | Adjust anchors, scale and rig hierarchy |
| Hands swapped | Check main arm and the profile's swing convention |
| Configuration unchanged | Restart; character reload does not reload general options |
| Old resources displayed | Let reload finish; reconnect and inspect hashes/cache |

## Invalid resources

`unknown bone` indicates a channel or mask referencing a missing bone. `Unknown bound animation` indicates a binding to a missing clip. `Unsupported MoLang query` requires removing or adapting that query. Do not add empty bones or clips simply to hide errors without verifying animation.

Textures must be valid PNGs, not renamed files, and meet dimension limits. Paths are relative and lowercase. Invalid reloads retain the previous catalog: seeing the old model does not prove your new resources were accepted.

## Mod conflicts

Reproduce in a test instance with Morph and its dependencies first. Add other mods in groups, especially mods affecting size, rendering, movement or camera. Do not remove mods from your main world without a backup.

## Reporting information

Include Morph/Minecraft versions, Loader, Fabric API, GeckoLib, additional mods, local or dedicated environment, minimal reproduction steps and client/server `logs/latest.log`. Attach the manifest and minimal resources if you have permission to share them. Redact private information.

To recover the cache, close the game and remove only the affected instance's `.morphmod-cache`. Preserve logs first. Do not delete worlds, unlocks or catalogs as an initial troubleshooting step.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
