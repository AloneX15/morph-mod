# Animaciones y perfiles

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Animations.md)

## Selección de clips

El servidor no transmite una animación de caminar en cada tick. El cliente determina el estado del jugador y elige clips de locomoción, manos, combate y emotes. `bindings` permite usar nombres propios y tiene prioridad sobre detección automática.

~~~json
{
  "bindings": {
    "idle": "animation.explorador.idle",
    "walk": "animation.explorador.walk",
    "run": "animation.explorador.sprint",
    "bow_rightArm": "animation.explorador.bow"
  }
}
~~~

Este fragmento se incorpora al manifiesto; todos los clips indicados deben existir. No es un manifiesto completo.

## Locomoción

| Acción interna | Nombres / alias detectados |
|---|---|
| `idle` | idle |
| `walk` | walk |
| `run` | run, sprint |
| `sneak` | sneak, sneaking |
| `sneak_walk` | sneak_walk, sneaking.walk |
| `swim` | swim, swimming, swim_sprint |
| `elytra` | elytra, elytras, elytra_fly |
| `climb` | climb |
| `climb_idle` | climb_idle, climb.idle |
| Otros | sleep, sit, riptide, crawl, jump, fall |

Prioridad de estado: dormir, montar, riptide, élitros, nadar, arrastrarse, trepar, salto/caída y movimiento terrestre. Si falta correr, se intenta caminar; agachado en movimiento intenta agacharse; trepar quieto intenta trepar; crawl intenta nadar. Último recurso: idle.

## Manos y combate

Acciones físicas: `trident`, `bow`, `crossbow`, `crossbow_charge`, `shield`, `spyglass`, `brush`, `goat_horn`, `eat` y `item` con sufijo `_rightArm` o `_leftArm`.

Alias antiguos: `bow_aim`, `bow_aim.offhand`, `crossbow_aim`, `trident_aim`, `block`, `eat.main_hand`, `eat.off_hand`, `eat`, `eat.offhand`, `use_item`, `use_item.offhand`, `hand_right_interact` y `hand_left_interact`. La mano principal puede ser izquierda: mano lógica y brazo físico no son lo mismo.

Combate: `swing.main_hand` y `swing.off_hand`. En `forge-1.20.1` representan mano principal/secundaria; en perfiles Fabric representan brazo derecho/izquierdo.

## Poses especiales y perfiles

Con equipo en las manos se intentan clips `specialpose.<estado>` y sus alias. Para poses de equipo se admiten `special_pose.hold_right`, `hold_left`, `bow_right`, `bow_left`, `crossbow_right`, `crossbow_left`, `shield_right`, `shield_left`, `spyglass_right`, `spyglass_left` y `hold_both` bajo el prefijo `special_pose.`.

Los perfiles válidos son `fabric-1.21.1`, `fabric-1.20.4` y `forge-1.20.1`. Definen convenciones; no ejecutan otros loaders. No hay garantía de que toda la lista histórica de Morpher tenga la misma semántica aquí: usa bindings y prueba las acciones de tu rig. No se generan clips ausentes.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
