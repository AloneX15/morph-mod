# Arquitectura

## Estructura del código

```
src/
├── main/java/io/github/jmarc/morph/          ← común (cliente + servidor)
│   ├── MorphMod.java                         entrypoint: registra todo
│   ├── ability/                              MorphAbility, AbilitySlot
│   │   └── impl/                             SonicBoom, DarknessPulse, Explode, Teleport, Fireball, Arrow, Fling, PlayDead
│   ├── command/MorphCommand.java             /morph, /demorph
│   ├── config/MorphConfig.java               config/morphmod.json (Gson)
│   ├── data/MorphAttachments.java            current_morph, unlocked_morphs
│   ├── event/MorphEvents.java                desbloqueo al matar, inmunidades, tick de pasivas, join/respawn
│   ├── mixin/                                AvatarMixin (hitbox), PlayerMixin (trepar)
│   ├── morph/                                MorphDefinition, MorphRegistry, MorphManager, Passive
│   ├── network/                              payloads + MorphNetworking (handlers del servidor)
│   └── util/                                 CooldownTracker, StatMath (Java puro, con tests)
├── client/java/io/github/jmarc/morph/client/ ← solo cliente
│   ├── MorphModClient.java                   entrypoint de cliente
│   ├── MorphKeybinds.java                    M / R / G / N
│   ├── ClientMorphState.java                 cooldowns para el HUD
│   ├── hud/MorphHud.java
│   ├── screen/MorphSelectScreen.java
│   ├── render/DisguiseManager.java           entidades disfraz
│   └── mixin/                                LevelExtractorMixin (render), MinecraftMixin (vibraciones), WalkAnimationStateAccessor
├── test/java/                                JUnit (lógica pura)
└── gametest/java/                            test de cliente dentro del juego
```

## Flujo de una transformación

```mermaid
sequenceDiagram
    participant C as Cliente
    participant S as Servidor
    participant O as Otros clientes
    C->>S: SelectMorphPayload(minecraft:warden)
    S->>S: ¿desbloqueado? ¿vivo? ¿morfable?
    S->>S: MorphManager.morph()<br/>• attachment current_morph<br/>• modificadores de atributo<br/>• vuelo, vida, refreshDimensions
    S-->>C: sync attachment + atributos (automático)
    S-->>O: sync attachment (AttachmentSyncPredicate.all)
    C->>C: DisguiseManager crea un Warden disfraz<br/>y refreshDimensions()
    O->>O: igual: ven al jugador como Warden
```

## Flujo de una habilidad

```mermaid
sequenceDiagram
    participant C as Cliente
    participant S as Servidor
    C->>S: UseAbilityPayload(slot=0)
    S->>S: forma actual → ability(PRIMARY)
    S->>S: CooldownTracker.isReady()?
    S->>S: ability.activate(player)
    S-->>C: CooldownPayload(slot, ticks)
    C->>C: HUD dibuja el cooldown
```

## Piezas clave

### Datos (`MorphAttachments`)
- `current_morph : Identifier`: persistente, se sincroniza **a todos** (los demás necesitan saber qué dibujar) y **no** se copia al morir.
- `unlocked_morphs : List<Identifier>`: persistente, se sincroniza **solo al dueño** (para el menú) y se copia al morir.

### Stats (`MorphManager.applyStats`)
Cada `MorphDefinition` guarda valores **absolutos**, por ejemplo `MAX_HEALTH = 500`. Para cada uno se calcula
`modificador = objetivo − base del jugador` y se aplica un `AttributeModifier(ADD_VALUE)` con el id `morphmod:morph`.
Al volver a forma humana se borra ese id en todos los atributos. La vida se reescala para conservar el porcentaje (10/20 → 250/500).

### Hitbox (`AvatarMixin`)
`Avatar#getDefaultDimensions(Pose)` devuelve `EntityType#getDimensions()` del mob, que ya incluye la altura de ojos.
Cuando cambia el morph, el cliente llama a `refreshDimensions()` desde `DisguiseManager.tick`.

### Render (`DisguiseManager` + `LevelExtractorMixin`)
1. Por cada jugador transformado se crea una entidad del tipo del mob que **nunca se añade al mundo**.
2. En cada tick se le llama a `tick()` para que avancen sus animaciones (alas del murciélago, tentáculos del Warden). Si falla, ese tipo de mob se marca y se dibuja sin animaciones.
3. En cada frame se copian posición, rotaciones, `walkAnimation`, `hurtTime`, fuego, etc. del jugador.
4. `LevelExtractorMixin` envuelve la extracción del estado de render de las entidades del mundo. Si la entidad es un jugador transformado, extrae el estado del disfraz, y el renderer del propio mob lo dibuja.

> En 26.x el jugador local también se extrae aparte para las manos en primera persona, y ese estado **tiene** que ser de jugador.
> Por eso el mixin está en `extractVisibleEntities` y no en `EntityRenderDispatcher#extractEntity`.

### Pasivas
| Pasiva | Implementación |
|---|---|
| Inmune al fuego / sin daño por caída | `ServerLivingEntityEvents.ALLOW_DAMAGE` |
| Visión nocturna, caída lenta, nado rápido | Efecto ambiental oculto que se renueva cada tick |
| Respira bajo el agua | `setAirSupply(max)` |
| Trepa paredes | `PlayerMixin#onClimbable` (en los dos lados) |
| Vuelo | `Abilities.mayfly` + `onUpdateAbilities()` |
| El agua daña / arde al sol | Comprobación periódica en `tickPassives` |
| Inmune a oscuridad | Se elimina el efecto Darkness |
| Percibe vibraciones | `MinecraftMixin#shouldEntityAppearGlowing` (solo cliente) |

## Cómo añadir un mob con poderes

1. Si necesitas una habilidad nueva, crea una clase en `ability/impl` que implemente `MorphAbility`:
   ```java
   public final class MiPoder implements MorphAbility {
       public String id() { return "mi_poder"; }           // ability.morphmod.mi_poder
       public int cooldownTicks() { return 5 * 20; }
       public boolean activate(ServerPlayer player) { ...; return true; }
   }
   ```
2. Regístralo en `MorphRegistry.init()`:
   ```java
   register(MorphDefinition.builder(EntityTypes.PHANTOM)
       .stat(Attributes.MAX_HEALTH, 20.0)
       .passive(Passive.FLIGHT, Passive.BURNS_IN_SUN)
       .primary(new MiPoder())
       .build());
   ```
3. Añade las traducciones `ability.morphmod.mi_poder` en `en_us.json` y `es_es.json`.
4. Documenta el mob en [MOBS.md](MOBS.md) y en el [CHANGELOG](../CHANGELOG.md).
