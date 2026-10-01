# Plan inicial

> Documento de arranque del proyecto (2026-10-01). Las decisiones que cambien después se registran en la [bitácora](BITACORA.md).

## 1. Visión

Un mod de Fabric en el que el jugador puede **convertirse en cualquier mob** y jugar como él, con su aspecto, su tamaño, sus estadísticas y sus poderes. La forma de referencia es el **Warden**, que debe sentirse tan peligroso como el original.

## 2. Objetivos de la v0.1

| # | Objetivo | Criterio de aceptación |
|---|---|---|
| O1 | Transformarse en cualquier mob vivo | `/morph into <mob>` funciona con todos los mobs vivos salvo los de la lista negra |
| O2 | Forma visible | Los demás jugadores y tú (en tercera persona) veis el modelo y las animaciones del mob |
| O3 | Hitbox real | La altura y anchura del jugador pasan a ser las del mob |
| O4 | Stats | La vida, el daño, la velocidad, etc. cambian, y se restauran exactamente al volver a forma humana |
| O5 | Poderes | 10 mobs con habilidades activas (R/G) y pasivas |
| O6 | Progresión | Matar un mob desbloquea su forma, y los desbloqueos se guardan |
| O7 | Interfaz | Menú (M) con buscador y vista previa 3D, y HUD con cooldowns |
| O8 | Multijugador | Todo se valida en el servidor y el cliente nunca decide |
| O9 | Repositorio | README, plan, bitácora, arquitectura, CI y tests |

## 3. Alcance

**Dentro:** sistema de morph, 10 mobs con poderes (Warden, Creeper, Enderman, Blaze, Araña, Murciélago, Gólem de hierro, Esqueleto, Ghast y Ajolote), forma genérica para el resto, menú, HUD, comandos, configuración JSON, idiomas español e inglés.

**Fuera (de momento):** IA de mobs que reaccione a tu forma, animaciones de ataque del mob, morphs por datapack, mano en primera persona con el aspecto del mob y compatibilidad con otras versiones de Minecraft.

## 4. Decisiones técnicas

| Tema | Decisión | Motivo |
|---|---|---|
| Versión | Minecraft **26.3**, Fabric Loader 0.19.5, Fabric API 0.161.0, Loom 1.18, **Java 25** | Es la última versión estable al empezar |
| Nombres | Mojang oficiales (26.x ya no está ofuscado; no se usa Yarn) | Es lo que usa la plantilla oficial de Fabric para 26.x |
| Datos del jugador | Fabric **Data Attachment API**: `current_morph` sincronizado a todos y `unlocked_morphs` solo al dueño | Persistencia y sincronización sin escribir NBT ni paquetes a mano |
| Stats | Un `AttributeModifier` permanente (`morphmod:morph`) en cada atributo cambiado | Se guarda con el jugador, así la vida por encima de 20 no se recorta al cargar. Se elimina con un solo id |
| Hitbox | Mixin en `Avatar#getDefaultDimensions` | Funciona en los dos lados, y el cliente lo necesita para sus colisiones |
| Render | Una entidad "disfraz" por jugador, solo en el cliente, y un mixin en `LevelExtractor#extractVisibleEntities` | Reutiliza el renderer del propio mob, así que cualquier mob funciona sin código específico |
| Red | 3 payloads C2S (elegir forma, destransformar, usar habilidad) y 1 S2C (cooldown) | El servidor valida todo |
| Poderes | Interfaz `MorphAbility` y enum `Passive` | Añadir un mob consiste en una entrada en `MorphRegistry` |

## 5. Hitos

| Hito | Contenido | Estado |
|---|---|---|
| M0 | Andamiaje: Gradle, Loom, Java 25, CI | ✅ |
| M1 | Núcleo: attachments, `MorphManager`, stats, hitbox, comandos | ✅ |
| M2 | Render del disfraz | ✅ |
| M3 | Habilidades y pasivas de los 10 mobs | ✅ |
| M4 | Menú, HUD y teclas | ✅ |
| M5 | Tests unitarios + test en el juego, documentación, primera release | ✅ v0.1.0 |

## 6. Riesgos

| Riesgo | Impacto | Mitigación |
|---|---|---|
| El sistema de render de 26.x (render states + extractors) cambia entre versiones | Alto | Un único mixin de render, aislado y cubierto por el test en el juego con capturas |
| `tick()` de un mob que no está en el mundo lanza excepciones | Medio | Se envuelve en try/catch y ese tipo de mob se renderiza sin animaciones |
| Balance (500 de vida del Warden) | Medio | `maxHealthCap`, `damageMultiplier` y `cooldownMultiplier` en la configuración |
| Mobs enormes (Ghast) que se atascan | Bajo | Es parte del diseño. Se puede volver a forma humana en cualquier momento |
| Trampas en multijugador | Medio | El servidor comprueba desbloqueos, cooldowns y que el jugador esté vivo |

## 7. Cómo se verifica

- `./gradlew build`: compila y ejecuta los tests unitarios (`CooldownTracker`, `StatMath`, `MorphConfig`).
- `./gradlew runClientGameTest`: abre el juego, crea un mundo, se transforma en Warden y en murciélago, comprueba la hitbox, la vida, el vuelo, el cooldown, el menú y la vuelta a forma humana, y saca capturas en `build/run/clientGameTest/screenshots`.
