# Getting started

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Getting-started.md)

## Your first mob form

1. Kill a compatible mob in survival. Default configuration unlocks its form and displays a message.
2. Press **J** and find the mob.
3. Select it and press **Transform**.
4. Check the HUD for your form and available abilities.
5. Use **R** for the primary ability and **K** for the secondary.
6. Return to human using the menu button or a key you assigned.

An administrator can grant a form with `/morph unlock minecraft:bat Name`. If the server disables `requireUnlock`, you do not need to kill mobs before selecting them.

## Your first character

Open **J → Characters**, select **Otter**, and transform. This template is free by default and retains human statistics and collision. Killing an otter is not required: characters have a separate access system.

Under **Emotes**, choose `demo_dance`. Use `full` for a complete dance or `overlay` to dance while walking. Stop through the menu or with:

~~~text
/morph emote stop
~~~

You can also try the complete flow using personal commands:

~~~text
/morph character select morphmod:otter
/morph emote play demo_dance overlay
/morph character clear
~~~

If Otter is missing, see [troubleshooting](EN-Troubleshooting.md). On a LuckPerms server, an explicit denial can block even the free template.

## What to expect

A character changes your appearance; linking it to a mob may change your mechanics. A mob transformation also changes dimensions, maximum health and defined attributes. Switching forms preserves your health percentage rather than fully healing you.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
