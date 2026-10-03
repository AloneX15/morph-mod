# Creating emotes and dances

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Creating-emotes.md)

## Create the clip

In Blockbench, create a clip such as `emote.saludo` and animate existing bones. Export the animation file. The clip name can differ from the emote ID entered by players.

## Register the emote

Add this entry to your manifest's `emotes`:

~~~json
{
  "emotes": {
    "saludo": {
      "animation": "emote.saludo",
      "loop": false,
      "seconds": 3,
      "modes": ["full", "overlay"],
      "bones": ["right_arm", "right_hand"],
      "free": true
    }
  }
}
~~~

The `emote.saludo` clip and both bones must exist. For a full-only dance you may omit `bones` and use only `"modes": ["full"]`.

## Overlay masks

The client prepares a filtered variant containing channels for listed bones. Include children with their own channels when you want those channels retained. A parent transforms its descendants through the hierarchy even without direct descendant channels.

To walk during a dance, avoid animating legs, the root or parents that displace the entire body unless intentional. A torso mask does not automatically prevent a torso rotation from affecting arms and head.

## Looping and duration

`loop: true` repeats until stopped or access is lost. `loop: false` ends when the server reaches `seconds`; match this value to the actual clip length because it is not derived automatically from the animation file. Declared duration must be greater than zero and at most 120 seconds.

## Test and restrict

~~~text
/morph character reload
/morph character select mis_personajes:explorador
/morph emote play saludo full
/morph emote stop
/morph emote play saludo overlay
~~~

Test walking and item use in both modes and observe from another client. The server synchronizes ID, mode and starting time; observers calculate animation locally.

To restrict the emote, use `free: false` and a positive permission such as `morphmod.emote.use.mis_personajes.explorador.saludo`. The user also needs access to the character.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
