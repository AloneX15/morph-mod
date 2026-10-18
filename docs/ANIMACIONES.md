# Perfiles de animación del jugador

Creado por **TakumiStudios**. Fabric 26.1.x, 26.2 y 26.3, con Morph y GeckoLib en los clientes.

## Administración rápida

El servidor elige las animaciones del cuerpo humano. La skin, estadísticas, velocidad,
colisión y cámara permanecen intactas. Los perfiles se distribuyen automáticamente;
los jugadores no tienen que instalar sus archivos a mano.

```text
/morph animation list
/morph animation inspect morphmod:dedsafio_4
/morph animation default morphmod:dedsafio_4
/morph animation set Nombre morphmod:vanilla
/morph animation set @a morphmod:dedsafio_4
/morph animation status Nombre
/morph animation reset Nombre
/morph animation force morphmod:vanilla
/morph animation force off
/morph animation reload
```

`default` establece la base para todos; `set` guarda excepciones por UUID y `reset`
las elimina. Usar `set @a` crea excepciones para los jugadores conectados, no para
quienes entren después; para todos los jugadores presentes y futuros usa `default`.
`force` prevalece temporalmente sobre todas las reglas, hasta `force off` o reiniciar.

Todas estas operaciones requieren `morphmod.animations.admin`. Sin un valor definido
en LuckPerms, OP nivel 2 tiene acceso. Una denegación explícita prevalece sobre OP.
La consola también puede usarlos. Espera el mensaje de operación completada antes
de ejecutar otro cambio de configuración; la escritura es asíncrona y serializada.

Perfiles incluidos:

| ID | Comportamiento |
|---|---|
| `morphmod:vanilla` | Animaciones originales de Minecraft. |
| `morphmod:legacy_walk` | Caminar y agacharse del port anterior; perfil global inicial. |
| `morphmod:dedsafio_4` | Archivo original completo, adaptado al cuerpo vanilla, con avisos de importación. |

## Subir otro perfil

Crea `config/morphmod/animations/elegante/` **en el servidor**:

```text
elegante/
  profile.json
  elegante.animation.json
```

Ejemplo de `profile.json`:

```json
{
  "schemaVersion": 1,
  "id": "servidor:elegante",
  "name": "Elegante",
  "animations": "elegante.animation.json",
  "bindings": {
    "idle": "idle",
    "walk": "idle",
    "run": "idle",
    "sneak_walk": "sneaking"
  },
  "boneAliases": {}
}
```

El ejemplo de bindings corresponde a un archivo procedural como Dedsafio: su `idle`
ya contiene el caminar mediante `query.ground_speed`. Ajusta los nombres a los clips
de tu archivo. `bindings` y `boneAliases` pueden omitirse. No subas un modelo ni una
textura: el perfil anima la skin normal o slim de cada jugador.

Ejecuta `/morph animation reload`, revisa `/morph animation inspect servidor:elegante`
y asigna el perfil. Las recargas fallidas conservan el catálogo y las asignaciones
anteriores. Los archivos de animación se leen solamente al arrancar o recargar.

## LuckPerms y prioridades

`config/morphmod/animations.json` contiene el perfil global, las reglas y las
excepciones. Ejemplo:

```json
{
  "schemaVersion": 1,
  "defaultProfile": "morphmod:legacy_walk",
  "rules": [
    {
      "permission": "morphmod.animation.profile.elegante",
      "profile": "servidor:elegante",
      "priority": 100
    }
  ],
  "overrides": {}
}
```

Después de editarlo, recarga. Para vincularlo con un grupo:

```text
/lp group vip permission set morphmod.animation.profile.elegante true
/lp user Nombre permission set morphmod.animation.profile.elegante false
```

El orden es **forzado → excepción individual → regla LuckPerms → perfil global**.
Gana la regla con mayor número; en empate se usa el ID de perfil alfabéticamente y,
si coincide, el nodo alfabéticamente. La herencia y los contextos son los de LuckPerms.
Los cambios se revisan una vez por segundo. Una denegación excluye la regla automática;
no impide que un administrador establezca una excepción explícita.

Las excepciones sobreviven a la muerte, reconexión y reinicio. Un ID que ya no exista
se omite al resolver las prioridades; no se borra la excepción del archivo, por lo
que volverá a funcionar si se reinstala ese perfil. Si el perfil global tampoco
existe, se utiliza vanilla. Sin LuckPerms funcionan el valor global y las excepciones.

## Acciones e importación

| Acción interna | Clips/alias automáticos |
|---|---|
| Reposo, caminar y correr | `idle`, `walk`, `run` / `sprint`; correr puede usar caminar y caminar puede usar `idle`. |
| Agacharse y caminar agachado | `sneak`, `sneak_walk` / `sneaking`. |
| Agua, arrastrarse y élitros | `swim` / `swimming`, `crawl` (usa nadar si falta), `elytra` / `elytras`. |
| Monturas, sueño y otros | `sit`, `sleep`, `riptide`, `jump`, `fall`, `climb`, `climb_idle`. |
| Objetos | `item`, `eat`, `shield`, `bow`, `crossbow`, `crossbow_charge`, `trident`, `spyglass`, `brush`, `goat_horn`, con sufijo `_rightArm` o `_leftArm`. |
| Comer y golpear | `eat.main_hand`, `eat.off_hand`, `swing.main_hand`, `swing.off_hand`. |

En clips de golpe, `main_hand` corresponde al brazo derecho y `off_hand` al izquierdo,
como el archivo Dedsafio. Comer usa la mano principal/secundaria lógica. Se admiten
los alias de uso antiguos de arco, ballesta, escudo y tridente y `specialpose.<acción>`
cuando se lleva un objeto. Los bindings explícitos tienen prioridad.

La locomoción se aplica primero; luego los objetos, con la mano activa en último
lugar; por último el golpe. Solo se reemplazan los canales presentes. Las acciones
sin clip conservan vanilla, salvo los fallbacks de movimiento documentados arriba.

Se admiten vectores constantes, expresiones MoLang, keyframes, `pre`/`post`, interpolación
`linear` y `catmullrom`, bucles y `hold_on_last_frame`. Se usa el parser de GeckoLib,
sin modificar sus queries globales. Se rechazan asignaciones/guiones de MoLang,
variables externas y queries desconocidas. Queries disponibles:

```text
query.anim_time, query.ground_speed, query.pitch, query.yaw,
query.right_hand_swing, query.left_hand_swing, query.is_sneaking,
query.is_sprinting, query.is_swimming, query.is_on_ground, query.life_time,
query.head_x_rotation, query.head_y_rotation, query.limb_swing,
query.limb_swing_amount, query.is_first_person
```

`q.` es alias de `query.`. La trigonometría MoLang usa grados, el tiempo segundos y
`ground_speed` las unidades de GeckoLib: bloques por tick.

Los huesos canónicos son `root`, `waist`, `body`, `body_upper`, `head`, `right_arm`,
`left_arm`, `right_leg`, `left_leg`, `right_foot` y `left_foot`. Usa `boneAliases`
para traducir otros nombres. Los padres virtuales se componen sobre el esqueleto
humano; los pies se fusionan con la pierna correspondiente.
En el rig canónico, `root` pivota en los pies y `waist` a altura de la cabeza para
adaptar las poses de natación y élitros de Dedsafio. Rotación usa los ejes del modelo
humano y posición invierte Y al pasar del archivo a Minecraft.

Un torso segmentado o
un pie independiente no tienen reproducción exacta sobre una skin vanilla.
Los huesos desconocidos y eventos de sonido/partículas/timeline se omiten con aviso.
Escalas no uniformes combinadas con rotaciones jerárquicas se aproximan sin cizalla.

En primera persona se adaptan brazos y objetos desde los hombros y se limitan sus
ángulos y desplazamientos para mantenerlos delante de la cámara. No se aplican los
desplazamientos globales de `root`/`waist`. Mapas y el visor del catalejo conservan
su render específico de Minecraft. Armadura y capas de la skin siguen las poses en
tercera persona. Los morphs y personajes GeckoLib suspenden el perfil humano.

## Compatibilidad y fallos

El canal de perfiles tiene versión propia: clientes antiguos pueden conectarse,
pero no reproducen estos perfiles. Los clientes nuevos reciben el catálogo en juego,
validan su SHA-256 y lo almacenan en `.morphmod-animation-cache/`, hasta 64 MiB.
Las transferencias se fragmentan en 32 KiB y se limitan a 256 KiB por jugador/tick.
Límites: 128 perfiles, 16 MiB por perfil, 64 MiB de catálogo y 512 clips por perfil.

El cliente conserva la última revisión válida ante una transferencia o recarga
incorrecta. Si un perfil falla al evaluarse, se desactiva ese perfil localmente y
se muestra vanilla hasta recargar o reconectar; consulta `logs/latest.log`.

El servidor administrado prevalece sobre `playerWalkAnimation` del cliente.
Fuera de él, `config/morphmod-client.json` sigue controlando el caminar anterior.
La decisión es visual: un cliente modificado siempre puede dibujar algo distinto.
