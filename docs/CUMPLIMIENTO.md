# Revisión del estándar TakumiStudios

Revisión del 3 de octubre de 2026 para Morph 0.3.0. Skill aplicada:
`takumistudios-fabric-mod` y su referencia `standards.md`, junto con
`fabric-mod-github-ci` para build, pruebas y publicación.

| Punto | Estado | Implementación y comprobación |
|---|---|---|
| Autoría | ✅ | Authors exacto TakumiStudios, contactos del repositorio, paquetes com.takumistudios.morphmod, README y releases con atribución. Manifest verificado en los tres jars y LICENSE_morphmod incluido. Se conserva el copyright previo de la licencia MIT y se añade TakumiStudios. SECURITY, CHANGELOG, MIXINS y registro de errores presentes. |
| Plataforma | ✅ | Java 25, Stonecutter 0.9.8, Loom estable 1.18.2, Loader 0.19.5. Builds 26.1.2 (rango 26.1.x), 26.2 y 26.3; Fabric API por versión. Dependencias de Minecraft acotadas. 26.4 excluida por ser snapshot en Fabric Meta al revisar. |
| Fiabilidad | ✅ | Guards por evento, comando, paquete, habilidad y render; validación de entradas, slots, IDs, unlocks y estado. Protocolo 2, límite por jugador y canSend. Configuración versionada, finitud/rangos, backups sin sobrescribir copias anteriores, escritura atómica en Morph-IO y espera acotada al cerrar. Pruebas de regresión en servidor/cliente y unitarias. |
| Rendimiento | ✅ | Tick solo sobre UUID transformados; textos del HUD y detalles del menú cacheados fuera del render. Red mediante deltas de adjuntos y atributos por transición; unlocks solo al dueño, forma a observadores que siguen la entidad. Preview LRU de 16, formas limitadas a 4096 y limpieza al desconectar. Gametest de 10 cambios de forma con presupuesto de 2 s. |
| Compatibilidad | ✅ | APIs de Fabric para eventos/red/adjuntos/teclas/HUD; ocho mixins documentados, sin Overwrite/Redirect. J/R/K por defecto, destransformación sin asignar y resolución de conflictos solo en defaults. Lithium/FerriteCore como dependencias de desarrollo opcionales; GeckoLib 5 obligatorio y LuckPerms opcional. |
| Calidad local | ✅ | Versionado 0.3.0 y changelog, claves iguales en inglés/español, símbolos además de color para formas especiales, texto envuelto y detalles desplazables. Pruebas de UI normal, 320×180 y español, con capturas por versión. Checker del estándar y actionlint para workflows. |
| CI en GitHub y reporte privado | ⚠️ | Workflows preparados y validados localmente; pendiente comprobar la ejecución remota del push. Reportes privados activados y confirmados por API en AloneX15/morph-mod. |

## Verificación reproducible

```bash
node scripts/check-standards.mjs
./gradlew :26.1.2:build :26.2:build :26.3:build
./gradlew :26.1.2:runGameTest :26.2:runGameTest :26.3:runGameTest -PcompatPack
./gradlew :26.1.2:runGameTest :26.2:runGameTest :26.3:runGameTest -PluckPerms
./gradlew :26.1.2:runClientGameTest :26.2:runClientGameTest :26.3:runClientGameTest --no-parallel
```

Cada versión ejecuta 30 pruebas unitarias y siete gametests propios (más el smoke test
del arnés). Cubren formas/atributos/dimensiones, vuelo, resync y datos inválidos,
desbloqueos, pasivas, cooldowns, proyectiles, daño dirigido, explosión y teletransporte.
Los tests de personajes comprueban recursos y archivos corruptos, límites, alias de
animación, estados de combate y permisos de grupos/usuarios con LuckPerms. El test
de cliente comprueba render, UI, brazos en primera persona, equipo, handshake
incompatible, slot inválido, cooldown y destransformación real en supervivencia.
El test multijugador abre un segundo cliente contra un servidor dedicado y comprueba
transferencia, emotes full/overlay, reconexión, recarga y rechazo de catálogos inválidos.
Los clientes de las tres versiones se ejecutan secuencialmente porque el servidor
dedicado del arnés utiliza el mismo puerto.

Resultado local del 3 de octubre de 2026: builds y 30 pruebas unitarias por versión
correctos; ocho gametests por versión correctos en servidor normal, con
Lithium/FerriteCore y con LuckPerms; pruebas de cliente y de dos clientes contra
servidor dedicado correctas en 26.1.2, 26.2 y 26.3. Checker del estándar y actionlint
correctos. Capturas y logs de cliente conservados en `build/verification/0.3.0/`;
logs de las matrices finales en `build/final-*-validation.log`.

Los tests de servidor y cliente de una misma versión se lanzan en llamadas separadas:
el task de limpieza de Loom elimina directorios de ambos arneses. Guardar capturas
antes de volver a lanzar tests de servidor; rutas en `versions/<mc>/build/run/`.

El jar de 26.1.2 declara 26.1.x conforme a la matriz de la plantilla de la skill;
las ejecuciones de esta revisión usan 26.1.2, 26.2 y 26.3. No se han ejecutado
individualmente los clientes 26.1 y 26.1.1, ni un modpack distinto a Lithium/FerriteCore.
Los resultados prueban los escenarios cubiertos; no garantizan compatibilidad con
cualquier mod ni ausencia absoluta de fallos de JVM o del propio juego.

## Comprobaciones externas pendientes

- Subir los cambios y confirmar las matrices de CI, las capturas y la pre-release dev.
- Reportes privados activados y confirmados mediante la API en `AloneX15/morph-mod`
  tras el cambio de nombre y visibilidad del repositorio.
- Configurar IDs/tokens de Modrinth o CurseForge cuando existan los proyectos, antes
  de publicar un tag. Los pasos correspondientes solo se ejecutan si hay variables.

Creado por **TakumiStudios**.
