# Frequently asked questions

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-FAQ.md)

## Does it run on Forge or Minecraft 1.20/1.21?

No. This implementation targets Fabric on the 26.1.x, 26.2 and 26.3 matrix. `forge-1.20.1` and `fabric-1.21.1` are resource animation profiles, not executable mod versions.

## Does every mob have powers?

No. Ten forms have dedicated definitions, some with passives only. Other compatible mobs use a generic attribute definition without inheriting their entire AI or attacks.

## Will mobs of my species stop attacking me?

That feature is not currently implemented. Appearance and passives do not automatically change AI relationships.

## Does a large character have large collision?

Not from visual scale alone. Without `mob` it keeps human collision. Linking a mob changes mechanics and dimensions according to that mob.

## Can I walk while dancing?

Yes, if the emote supports `overlay` and its mask preserves locomotion. A `full` dance stops on horizontal movement or actions.

## Can I use custom animation names?

Yes, through `bindings` and the emote `animation` field. Clips must exist. Morph does not generate idle, walk or dance animations from geometry.

## Do friends need to copy all characters?

No. The server transfers resources to clients with Morph, Fabric API and GeckoLib. Resources are validated and cached. Client-only files do not replace the server catalog.

## Is LuckPerms required?

No. Without it, characters and emotes use default access rules and administration requires OP 2. With it, you can manage groups, users and contexts.

## Why does /demorph require permissions?

It is an OP 2 administrative command. Without OP, use the menu button, your configured key or `/morph character clear`.

## Are unlocks lost on death?

Mob and character unlocks are copied on death. The current form is not copied; emotes are transient.

## Does character reload also update morphmod.json?

No. It updates the character catalog. Restart the server for general configuration changes.

## Where should I report problems?

See [support and security](EN-Contributing.md), including versions, reproduction steps and logs to attach. Never publish credentials or private data.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
