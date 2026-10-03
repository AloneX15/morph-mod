# Pruebas y CI

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Testing-and-CI.md)

## Validación local

Ejecuta cada perfil de servidor en una llamada separada. Gradle deduplica la misma tarea dentro de una llamada.

~~~powershell
node scripts/check-standards.mjs
.\gradlew.bat :26.1.2:build :26.2:build :26.3:build
.\gradlew.bat :26.1.2:runGameTest :26.2:runGameTest :26.3:runGameTest -PcompatPack
.\gradlew.bat :26.1.2:runGameTest :26.2:runGameTest :26.3:runGameTest -PluckPerms
.\gradlew.bat :26.1.2:runClientGameTest :26.2:runClientGameTest :26.3:runClientGameTest --no-parallel
~~~

Los clientes se ejecutan secuencialmente porque el arnés dedicado utiliza el mismo puerto 25565. La prueba de dos clientes lanza un observador separado, comprueba estados y cierra sus procesos.

## Qué cubren

| Nivel | Escenarios |
|---|---|
| Unitarias | Configuración, cooldowns, límites, recursos corruptos, hashes, fragmentos, aliases y máscaras |
| Servidor | Atributos, dimensiones, pasivas, poderes, selección, emotes y permisos |
| Cliente | Menús, UI pequeña/español, modelos, equipo, brazos, cooldown y destransformación |
| Multijugador | Transferencia, observador, full/overlay, reconexión, recargas y catálogo inválido |
| Compatibilidad | Servidor con Lithium/FerriteCore; perfil LuckPerms separado |

Resultado local del 3 de octubre de 2026 para 0.3.0: 30 unitarias por versión; ocho gametests por versión (incluido smoke) correctos en los perfiles de servidor; cliente y multijugador correctos en las tres versiones. Eso no equivale a una ejecución remota verde de estos cambios.

## Informes

Los informes unitarios están en `versions/<mc>/build/reports/tests/test/`. Logs y capturas de arneses están en `versions/<mc>/build/run/`. La revisión local conservó capturas en `build/verification/0.3.0/`. La limpieza de otro arnés puede borrar originales: cópialos antes.

## GitHub Actions

El workflow de build comprueba estándares, matriz de servidor y cliente; usa pantalla virtual y Mesa para pruebas gráficas. Un push a main puede publicar la prerelease dev cuando la matriz pasa. El workflow de release comprueba tag `vX.Y.Z` contra `mod.version`, obtiene notas del changelog y construye los JAR.

Modrinth/CurseForge requieren IDs y secretos configurados. No publiques un tag solo para comprobar la documentación. Los workflows están preparados; verifica sus ejecuciones reales tras publicar cambios del mod.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
