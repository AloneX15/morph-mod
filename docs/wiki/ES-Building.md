# Entorno y compilación

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Building.md)

## Requisitos y estructura

Usa JDK 25 y el wrapper Gradle del repositorio. IntelliJ IDEA puede importar el proyecto Gradle; el plugin Minecraft Development es opcional.

Stonecutter comparte código entre 26.1.2, 26.2 y 26.3. La versión activa es 26.3. Dependencias y `mod.version` están centralizadas en `stonecutter.properties.toml`; el código común y cliente están separados en `src/main` y `src/client`.

## Compilar

En Windows PowerShell usa `.\gradlew.bat`; en Linux/macOS o Git Bash usa `./gradlew`.

~~~powershell
.\gradlew.bat :26.1.2:build :26.2:build :26.3:build
.\gradlew.bat :26.3:runClient
.\gradlew.bat :26.3:genSources
~~~

`build` compila y ejecuta los checks configurados, incluidas pruebas unitarias y de servidor. Los JAR instalables están en `versions/<mc>/build/libs/`. El archivo `-sources.jar` es para desarrollo, no para instalar en mods.

## Pruebas opcionales

~~~powershell
.\gradlew.bat :26.3:runGameTest -PcompatPack
.\gradlew.bat :26.3:runGameTest -PluckPerms
.\gradlew.bat :26.3:runClientGameTest
~~~

`compatPack` añade Lithium/FerriteCore al entorno de desarrollo. `luckPerms` añade LuckPerms para su regresión de permisos; no lo convierte en dependencia obligatoria del producto.

## Diferencias entre versiones

Usa ramas Stonecutter para APIs incompatibles y compila la matriz completa. No edites copias generadas por versión como fuente principal. Revisa la [arquitectura](ES-Architecture.md) antes de modificar render, adjuntos o negociación.

Las pruebas de cliente necesitan un entorno gráfico. Guarda capturas antes de lanzar tests de servidor de la misma versión: la limpieza de Loom elimina directorios de ambos arneses.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
