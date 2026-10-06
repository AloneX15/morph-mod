# Permisos con LuckPerms

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Permissions.md)

LuckPerms es opcional. Sin él, se aplican `free` y desbloqueos; administración del catálogo requiere OP 2. Con LuckPerms se consultan los permisos efectivos del usuario, incluyendo grupos, herencia y contextos actuales.

## Nodos

| Nodo | Función |
|---|---|
| `morphmod.characters.admin` | Recargar catálogo y desbloquear personajes |
| `morphmod.character.use.<namespace>.<path>` | Usar el personaje |
| `morphmod.emote.use.<namespace>.<path>.<emote>` | Usar un emote del personaje |
| `morphmod.equipment.toggle` | Ver la pestaña del inventario que muestra u oculta armadura y objetos en mano en el propio morph |

En IDs, `:` y `/` se convierten en puntos. `mis_personajes:grupo/explorador` corresponde a `morphmod.character.use.mis_personajes.grupo.explorador`.

## Reglas de acceso

- Permiso efectivo **true**: concede acceso.
- Permiso efectivo **false**: bloquea acceso, incluso si `free` o un desbloqueo lo permitirían.
- Permiso **indefinido**: personaje según `free` o desbloqueo; emote según su propio `free`.
- Un emote requiere además acceso al personaje.
- Administración indefinida: OP 2 como fallback.
- Pestaña de equipo indefinida: OP 2 como fallback.

Una concesión individual puede superar una denegación heredada según la resolución de LuckPerms. Morph utiliza el resultado efectivo; no impone que cualquier nodo false de cualquier grupo gane siempre. La pérdida de acceso se revisa cada segundo; seleccionar o reproducir comprueba acceso inmediatamente.

## Ejemplos

~~~text
/lp group default permission set morphmod.character.use.morphmod.otter false
/lp creategroup artistas
/lp group artistas permission set morphmod.character.use.morphmod.otter true
/lp user Nombre parent add artistas
/lp user Nombre permission set morphmod.emote.use.morphmod.otter.demo_dance true
/lp user Nombre permission set morphmod.characters.admin true
~~~

Para volver al comportamiento predeterminado, elimina el nodo con `permission unset`; poner false es una denegación, no un reset. Usa nombres de usuario reales.

Para contextos, por ejemplo un servidor identificado como `survival` en LuckPerms:

~~~text
/lp group artistas permission set morphmod.character.use.morphmod.otter true server=survival
~~~

El contexto debe existir y estar activo en tu configuración de LuckPerms. Esta integración no añade permisos LuckPerms a `/morph into`, `unlock` o `reset` de mobs: esos comandos conservan OP 2.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
