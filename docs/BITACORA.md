# Bitácora de desarrollo

Diario del proyecto. Cada entrada sigue la plantilla **Hecho / Problemas / Decisiones / Siguiente**.
Las entradas más recientes van arriba.

<!--
## AAAA-MM-DD — Título

**Hecho**
-

**Problemas**
-

**Decisiones**
-

**Siguiente**
-
-->

---

## 2026-10-01 — v0.1.0: del proyecto vacío al primer morph jugable

**Hecho**
- Se instala el JDK 25 (Temurin 25.0.4) porque Minecraft 26.3 lo exige. El equipo solo tenía Java 21.
- La plantilla Java de IntelliJ se sustituye por el andamiaje oficial de Fabric (`fabric-example-mod`): Loom 1.18, Gradle 9.7.1, source sets separados de cliente y servidor, y toolchain de Java 25 con *foojay* para que Gradle descargue el JDK si falta.
- Núcleo del morph: `MorphRegistry` con 10 definiciones y una forma genérica para el resto, `MorphManager` con stats, vuelo, pasivas y habilidades, attachments persistentes y sincronizados, y mixin de hitbox.
- 8 habilidades: Estallido sónico, Pulso de oscuridad, Explotar, Teletransporte, Bolas de fuego (Blaze y Ghast), Flecha, Lanzar por los aires y Hacerse el muerto.
- Cliente: render del disfraz, menú con buscador y vista previa 3D, HUD con cooldowns, teclas M/R/G/N y "sentido de vibraciones" del Warden con el contorno de brillo de vanilla.
- Comandos `/morph` y `/demorph`, configuración `config/morphmod.json`, idiomas `es_es` y `en_us`, e icono.
- Tests: 11 unitarios y un test de cliente dentro del juego con capturas.
- Documentación (README, plan, arquitectura, mobs), CI de GitHub Actions y plantillas de issues y PR.

**Problemas**
- `org.lwjgl.glfw.GLFW` ya no está en el classpath de 26.3. Las teclas se definen con `InputConstants.KEY_*`.
- `Options.hideGui` ya no existe. Los elementos del HUD de Fabric ya respetan F1, así que se quita la comprobación.
- El test en el juego encontró dos fallos que no aparecían al compilar:
  1. `Tried to access entity ID before ID assignment`: los renderers leen el id de la entidad para las semillas de modelos de ítems. **Arreglo:** cada disfraz recibe un id negativo único.
  2. `Expected an AvatarRenderState for the local player`: en 26.3 el jugador local se extrae siempre como jugador (para las manos en primera persona), así que no se puede sustituir en `EntityRenderDispatcher#extractEntity`. **Arreglo:** el mixin pasa a `LevelExtractor#extractVisibleEntities` con `@WrapOperation`, que solo afecta al pase de entidades del mundo.
- En el menú, los botones se solapaban con el texto de detalles en resoluciones pequeñas. El panel se reordena: vista previa, botones y detalles.

**Decisiones**
- Se mantienen los stats reales del Warden (500 de vida). Quien lo quiera más equilibrado puede bajarlos con `maxHealthCap`.
- Los modificadores de atributo son **permanentes** (se guardan) y se resincronizan al entrar y al reaparecer. Si fueran transitorios, la vida por encima de 20 se recortaría al cargar el mundo.
- La forma actual **no** se conserva al morir. Los desbloqueos sí.
- Desbloqueo al matar (`unlockOnKill`) y posibilidad de desactivar el requisito (`requireUnlock: false`).
- Sin push automático a GitHub: falta la URL del repositorio remoto.

**Siguiente**
- Que los mobs de la misma especie no te ataquen.
- Animación de ataque del Warden al usar el Estallido sónico.
- Más mobs con poderes: Phantom, Breeze, Guardian, Wither...
- Probar en un servidor dedicado con dos clientes.
