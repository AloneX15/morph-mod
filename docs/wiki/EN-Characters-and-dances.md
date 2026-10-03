# Characters and dances

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Characters-and-dances.md)

## Selecting a character

Open **J → Characters**. The server owns the catalog; placing files only on a remote server's client is insufficient. The server transfers resources automatically. In a local world, your instance also acts as the server.

Free characters are available unless permissions explicitly deny access. Other characters require an unlock or positive permission. The bundled `morphmod:otter` character is free by default.

## Two dance modes

| Mode | Animated content | Movement and hands |
|---|---|---|
| `full` | The entire clip | Replaces locomotion and hand poses; stops on horizontal movement or actions |
| `overlay` | Channels belonging to masked bones | Keeps locomotion and allows walking |

The server also stops full dances on attacks, item use or riding, and stops emotes on sleeping, death, form changes or reconnecting. Activating an ability stops the emote. Non-looping emotes end after `seconds`; looping emotes continue until stopped or access is lost.

The manifest must allow the chosen mode. For overlay-only emotes, specify `overlay`: omitting the command mode attempts `full`.

## Try the template

~~~text
/morph character select morphmod:otter
/morph emote list
/morph emote play demo_dance full
/morph emote stop
/morph emote play demo_dance overlay
/morph character clear
~~~

## Mechanics and equipment

Without `mob`, the character retains human statistics, collision and abilities. With `mob`, its custom appearance can accompany that mob's mechanics. Items, armor and elytra need model anchors to render in the correct locations; they keep their gameplay effects even when an anchor is missing.

See [creating emotes](EN-Creating-emotes.md) to add your own dances.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
