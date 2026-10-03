# Personajes GeckoLib y emotes

Creado por **TakumiStudios**. Disponible desde Morph 0.3.0, en Fabric 26.1.2, 26.2 y 26.3.
Los perfiles Forge/Fabric describen nombres de animaciones; no añaden soporte para esos loaders o versiones antiguas.

## Importar un personaje

El servidor lee `config/morphmod/characters/<carpeta>/`. Cada carpeta contiene un
`character.json`, una geometría Bedrock exportada para GeckoLib, sus animaciones y
una textura PNG. Al primer arranque se copia la plantilla de nutria incluida en el jar.
Los clientes reciben los recursos automáticamente durante la conexión y tras
`/morph character reload`; necesitan Morph, Fabric API y GeckoLib 5.

Ejemplo mínimo:

```json
{
  "schemaVersion": 1,
  "id": "mis_personajes:explorador",
  "name": "Explorador",
  "free": true,
  "scale": 1,
  "model": "explorador.geo.json",
  "texture": "explorador.png",
  "animations": "explorador.animation.json",
  "profile": "fabric-1.21.1",
  "bindings": {"idle": "animation.explorador.idle", "walk": "animation.explorador.walk"},
  "anchors": {
    "right_hand": {"bone": "right_hand_item"},
    "left_hand": {"bone": "left_hand_item"},
    "right_arm": {"bone": "right_arm"},
    "left_arm": {"bone": "left_arm"},
    "head": {"bone": "head"},
    "chest": {"bone": "body"},
    "elytra": {"bone": "body"}
  },
  "emotes": {
    "saludo": {
      "animation": "animation.explorador.saludo",
      "loop": false,
      "seconds": 3,
      "modes": ["full", "overlay"],
      "bones": ["right_arm", "right_hand"],
      "free": true
    }
  }
}
```

Omitir `mob` conserva estadísticas, poderes y colisión humanos. Añadir, por ejemplo,
`"mob": "minecraft:bat"` asocia las mecánicas del murciélago al aspecto del personaje.
`scale` (0.1 a 4) cambia el tamaño visual; no cambia por sí solo la colisión.
`free: false` exige desbloqueo o permiso positivo. Los emotes tienen su propio `free`.

Los originales de la nutria se conservaron fuera del repositorio. La copia incluida
elimina los canales de `waist`, hueso ausente de la geometría, y añade `emote.demo_dance`.
La animación `idle` original ya incluye movimiento procedural mediante MoLang.

## Animaciones

`bindings` asocia una acción interna a cualquier nombre de clip. Tiene prioridad
sobre la detección automática. Los perfiles admitidos son `fabric-1.21.1`,
`fabric-1.20.4` y `forge-1.20.1`.

| Acción | Nombres detectados o alias |
|---|---|
| Reposo / caminar / correr | `idle`, `walk`, `run` / `sprint` |
| Agacharse / caminar agachado | `sneak` / `sneaking`, `sneak_walk` / `sneaking.walk` |
| Nadar / volar con élitros | `swim` / `swimming` / `swim_sprint`, `elytra` / `elytras` / `elytra_fly` |
| Trepar / reposo al trepar | `climb`, `climb_idle` / `climb.idle` |
| Otros estados | `sleep`, `sit`, `riptide`, `crawl`, `jump`, `fall` |
| Manos | `trident`, `bow`, `crossbow`, `crossbow_charge`, `shield`, `spyglass`, `brush`, `goat_horn`, `eat`, `item`, con sufijo `_rightArm` o `_leftArm` |
| Combate | `swing.main_hand`, `swing.off_hand` |
| Poses especiales | `specialpose.<estado>`, `special_pose.hold_right/left`, poses de objetos y `special_pose.hold_both` |

Se reconocen además alias antiguos: `bow_aim`, `bow_aim.offhand`, `crossbow_aim`,
`trident_aim`, `block`, `eat`, `eat.offhand`, `use_item`, `use_item.offhand`,
`hand_right_interact` y `hand_left_interact`. Para rigs con convenciones distintas,
define `bindings` explícitos. En Forge los nombres de swing designan mano principal
/secundaria; en los perfiles Fabric designan brazo derecho/izquierdo.

Si falta correr se usa caminar; caminar agachado usa agacharse, trepar quieto usa
subir y arrastrarse usa nadar. Como último recurso se usa `idle`. Los clips de manos
sin equivalente se omiten. No se generan animaciones ausentes del archivo.

MoLang admite expresiones matemáticas de GeckoLib y las queries de tiempo, velocidad,
cabeza, swing y estados reconocidas por el cargador. Las queries de Morpher `pitch`,
`yaw`, `right_hand_swing` y `left_hand_swing` se proporcionan explícitamente.
Una query desconocida rechaza el catálogo cliente y deja el anterior disponible.

## Equipo y primera persona

Los anclajes son opcionales. Cada uno declara `bone` y puede añadir `position`,
`rotation` y `scale`, vectores de tres valores. Posición usa unidades de modelo
(16 por bloque); rotación, grados. Ajusta los anclajes al rig en Blockbench.

Anclajes disponibles: `right_hand`, `left_hand`, `head`, `chest`, `right_arm`,
`left_arm`, `right_leg`, `left_leg`, `right_foot`, `left_foot`, `elytra`.
Las manos colocan los objetos; los segmentos restantes colocan armadura y élitros.
Los anclajes de brazo se usan también para mostrar brazos en primera persona.
Si un anclaje apunta a un hueso inexistente se omite su render; el equipo mantiene
sus efectos de juego.

## Bailes

`full` reemplaza movimiento y poses de manos. Se cancela al moverse, atacar, usar
objetos, montar o dormir. `overlay` mantiene el movimiento y aplica únicamente los
canales de los huesos enumerados en `bones`; enumera también los hijos que tengan
canales propios. La transformación de un padre afecta naturalmente a sus hijos.
Evita incluir las piernas para un baile que permita caminar.

`loop: true` repite hasta detenerlo o perder acceso. `loop: false` termina tras
`seconds` (máximo 120). El servidor sincroniza ID, modo e instante inicial con los
observadores. Al morir, cambiar de forma o reconectar se detiene el emote.

## Comandos y permisos

| Comando | Acceso |
|---|---|
| `/morph character list`, `select <id>`, `clear` | Jugador; selección según permisos |
| `/morph emote list`, `play <id> [full\|overlay]`, `stop` | Jugador; emote según permisos |
| `/morph character reload` | `morphmod.characters.admin`, OP 2 por defecto |
| `/morph character unlock <id> <jugadores>` | Mismo permiso administrativo |

LuckPerms es opcional, instalado únicamente en el servidor. Para `morphmod:otter`:

```text
/lp group default permission set morphmod.character.use.morphmod.otter false
/lp group artistas permission set morphmod.character.use.morphmod.otter true
/lp user Nombre permission set morphmod.emote.use.morphmod.otter.demo_dance true
```

Los dos puntos y las barras del ID se convierten en puntos en los nodos. Los
contextos e herencia los resuelve LuckPerms. Si el nodo está indefinido, el personaje
usa `free` o su desbloqueo; el emote usa su propio `free`. Una denegación explícita
prevalece sobre ese acceso predeterminado. La pérdida de acceso retira la forma o
el emote durante la siguiente revisión de permisos (cada segundo).

## Recursos, caché y errores

Límites: 256 personajes, 64 archivos y 16 MiB descomprimidos por personaje, catálogo
transferido de 64 MiB, PNG hasta 4096×4096, 512 huesos/animaciones. Los metadatos de
catálogo y permisos deben caber en 32 KiB. Las rutas son relativas, sin `..`.
Se rechazan IDs duplicados, recursos faltantes, ciclos de huesos y animaciones que
apunten a huesos inexistentes.

El envío usa fragmentos de 32 KiB, hasta 256 KiB por jugador y tick. Cada archivo
ZIP se valida con SHA-256 antes de usarse. Disco y preparación de recursos se hacen
fuera del hilo del juego. La caché `.morphmod-cache` se depura al superar 256 MiB;
los paquetes generados antiguos también se eliminan.

Una recarga de servidor inválida conserva el catálogo anterior. Si el cliente
rechaza recursos conserva el catálogo previo; los errores de render deshabilitan
el personaje afectado y muestran el jugador vanilla. Consulta `logs/latest.log`.
No borres archivos mientras se está preparando una recarga.
