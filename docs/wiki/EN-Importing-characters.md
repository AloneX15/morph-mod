# Importing characters with Blockbench

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Importing-characters.md)

## Before exporting

You need GeckoLib-compatible geometry, a PNG texture and an animation file. Use a GeckoLib project in Blockbench and export Bedrock/GeckoLib resources; a skin project or Java block model does not replace character geometry.

The model must contain **exactly one geometry**, unique bone names, existing parents and an acyclic hierarchy. Every animation channel must reference a model bone.

## Step-by-step import

1. Open the geometry in Blockbench and import its texture and animations.
2. Play idle and movement clips; check for channels referencing deleted bones.
3. Export `explorador.geo.json`, `explorador.animation.json` and `explorador.png`.
4. Create `config/morphmod/characters/explorador/` on the server.
5. Add `character.json` using relative paths and bindings matching real clips.
6. Run `/morph character reload` and inspect the message and log.
7. Select `mis_personajes:explorador` and test movement, items, armor and both camera views.

## Initial manifest

~~~json
{
  "schemaVersion": 1,
  "id": "mis_personajes:explorador",
  "name": "Explorador",
  "free": true,
  "model": "explorador.geo.json",
  "texture": "explorador.png",
  "animations": "explorador.animation.json",
  "bindings": {
    "idle": "animation.explorador.idle",
    "walk": "animation.explorador.walk"
  }
}
~~~

This example requires those two clips in the animation file. Replace their names with your actual clips or follow the [Otter tutorial](EN-Otter-tutorial.md), which includes complete resources.

## Incremental testing

Load geometry, texture and idle first. Then add locomotion, hands, anchors and emotes. This helps locate errors in the hierarchy, a binding or a MoLang expression.

Renaming a bone requires updating its channels and anchors too. Do not modify resources while a reload is being prepared. See [manifest](EN-Character-manifest.md) and [troubleshooting](EN-Troubleshooting.md).

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
