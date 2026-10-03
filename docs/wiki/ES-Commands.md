# Referencia de comandos

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Commands.md)

Los valores entre `< >` son obligatorios; `[ ]` indica argumentos opcionales. Escribe los valores sin esos signos. `targets` acepta jugadores y selectores de Minecraft. Usa IDs completos, por ejemplo `minecraft:bat` y `morphmod:otter`.

## Mobs

| Comando | Acceso | Resultado |
|---|---|---|
| `/morph list` | Jugador | Lista formas desbloqueadas |
| `/morph into <mob> [targets]` | OP 2 | Fuerza la forma del mob |
| `/morph clear [targets]` | OP 2 | Vuelve a humano |
| `/demorph` | OP 2, jugador | Alias personal para volver a humano |
| `/morph unlock <mob> [targets]` | OP 2 | Desbloquea un mob |
| `/morph unlock all [targets]` | OP 2 | Desbloquea mobs compatibles |
| `/morph reset <targets>` | OP 2 | Borra desbloqueos de mobs y destransforma |

Sin `targets`, los comandos opcionales afectan al jugador que los ejecuta. La consola debe proporcionar objetivos cuando corresponda. Forzar una forma no exige haberla desbloqueado.

## Personajes y emotes

| Comando | Acceso | Resultado |
|---|---|---|
| `/morph character list` | Jugador | Lista personajes permitidos |
| `/morph character select <id>` | Jugador autorizado | Selecciona tu personaje |
| `/morph character clear` | Jugador | Vuelve a humano, también desde un mob |
| `/morph character reload` | Administrador del catálogo | Recarga y distribuye recursos |
| `/morph character unlock <id> <targets>` | Administrador del catálogo | Desbloquea personaje para jugadores |
| `/morph emote list` | Jugador | Lista emotes permitidos del personaje actual |
| `/morph emote play <id> [mode]` | Jugador autorizado | Reproduce emote; modo predeterminado `full` |
| `/morph emote stop` | Jugador | Detiene tu emote |

`mode` es `full` u `overlay` y debe estar permitido por el emote. Administrador del catálogo significa `morphmod.characters.admin`, con OP 2 como acceso predeterminado. La denegación explícita de LuckPerms bloquea ese acceso.

## Ejemplos

~~~text
/morph unlock minecraft:bat @a
/morph into minecraft:warden Nombre
/morph clear Nombre
/morph character reload
/morph character unlock mis_personajes:explorador Nombre
/morph character select morphmod:otter
/morph emote play demo_dance overlay
/morph emote stop
~~~

Los comandos personales de personajes no aceptan objetivos. No existe `/morph config reload` ni un comando para cargar automáticamente clips de baile inexistentes.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
