# Cómo contribuir

¡Gracias por querer ayudar! Estas son las normas básicas.

## Entorno

- **JDK 25.** Si no lo tienes, Gradle lo descarga.
- IntelliJ IDEA (recomendado) con el plugin *Minecraft Development*. Importa el proyecto como proyecto Gradle.
- `./gradlew genSources` genera el código de Minecraft para poder navegar por él.

## Flujo

1. Abre un issue con la plantilla correspondiente antes de un cambio grande.
2. Crea una rama desde `main`: `feat/phantom-morph`, `fix/hitbox-crouch`, etc.
3. Antes de abrir el PR:
   - `./gradlew build` en verde (compila y ejecuta los tests unitarios),
   - `./gradlew runClientGameTest` en verde si tocas render, red o el núcleo del morph,
   - una entrada en `CHANGELOG.md` (sección *Unreleased*) y, si aporta algo, en `docs/BITACORA.md`.
4. Abre el PR rellenando la plantilla.

## Mensajes de commit

Se usa [Conventional Commits](https://www.conventionalcommits.org/es/v1.0.0/):

```
feat: add Phantom morph with dive ability
fix: restore flying speed when demorphing in creative
docs: document config options
test: cover StatMath edge cases
```

## Estilo de código

- Tabulaciones, como la plantilla de Fabric.
- El código, los identificadores y los comentarios van en **inglés**. La documentación del repo va en **español**.
- Los nombres de los métodos de mixin llevan el prefijo `morphmod$`.
- La lógica que no depende de Minecraft va en `util/` y lleva su test en `src/test`.
- Todo lo que llega del cliente se valida en el servidor.

## Añadir un mob

Sigue la guía de [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md#cómo-añadir-un-mob-con-poderes).
