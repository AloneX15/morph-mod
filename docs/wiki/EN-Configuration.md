# Configuration

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Configuration.md)

## File and application

`config/morphmod.json` is created on startup. The server reads it, including a local world's integrated server. Restart to apply changes. Character reload does not reload this file.

~~~json
{
  "schemaVersion": 1,
  "requireUnlock": true,
  "unlockOnKill": true,
  "maxHealthCap": 500.0,
  "damageMultiplier": 1.0,
  "cooldownMultiplier": 1.0,
  "allowFlight": true,
  "abilitiesBreakBlocks": false
}
~~~

## Complete reference

| Field | Default | Range / effect |
|---|---|---|
| `schemaVersion` | 1 | Format version; retain 1 |
| `requireUnlock` | true | Requires unlocks for mob selection |
| `unlockOnKill` | true | Unlocks a mob type when killed |
| `maxHealthCap` | 500 | Finite number, 1–1024; caps morph maximum health |
| `damageMultiplier` | 1 | Finite number, 0–100; scales defined damage and effects that use it |
| `cooldownMultiplier` | 1 | Finite number, 0–100; scales cooldowns |
| `allowFlight` | true | Enables flight for forms with that passive |
| `abilitiesBreakBlocks` | false | Enables destruction for abilities respecting this option |

`requireUnlock` does not grant characters: they use `free`, unlocks and permissions. Disabling `unlockOnKill` does not erase existing forms.

## Administrative examples

For a creative server with all mobs available, use `requireUnlock: false`. For administrator-controlled progression, retain `requireUnlock: true`, disable `unlockOnKill` and grant forms using commands.

To cap Warden health at 100 points, set `maxHealthCap` to 100. To double cooldowns, use `cooldownMultiplier: 2`. The `mobGriefing` rule additionally limits Creeper explosions.

## Recovery

Out-of-range numbers are clamped; non-finite values are replaced with defaults. Corrupt JSON is preserved in a `.bak` copy before defaults are generated. Future schema versions are preserved without overwriting. Maximum file size is 64 KiB.

Edit while the server is stopped, use JSON without comments and inspect the log after restarting.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
