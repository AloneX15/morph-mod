# Changelog

Todos los cambios importantes del proyecto se documentan aquí.
El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el proyecto usa [Versionado Semántico](https://semver.org/lang/es/).

## [Unreleased]

- Documentación: `demo_dance` se presenta como emote de ejemplo añadido por el mod; la animación propia de la nutria es su caminar/idle.
- Los morphs de mobs reproducen el golpe del jugador: el Golem de hierro levanta los brazos, el Warden, Ravager, Hoglin y Zoglin usan su animación de ataque y los humanoides balancean el brazo.
- La explosión del Creeper rompe bloques como el mob real, respetando `mobGriefing` (nueva opción `creeperBreaksBlocks`, configuración versión 2).
- Armadura de personajes GeckoLib ajustada al tamaño real de cada hueso; los anclajes de armadura usan `scale` como multiplicador y `position` como desplazamiento. Nutria corregida.
- Pestaña en el inventario, solo con el permiso `morphmod.equipment.toggle` (OP 2 por defecto), para mostrar u ocultar la armadura y los objetos en mano del propio morph. Oculto por defecto; también se aplica a los morphs de mobs.

## 0.3.0 - 2026-10-03

- Personajes GeckoLib independientes, con estadísticas humanas o vínculo opcional a un mob.
- Catálogo del servidor, transferencia fragmentada con SHA-256 y caché local acotada.
- Menús de personajes y emotes, comandos y permisos opcionales con LuckPerms.
- Animaciones de movimiento, manos y combate con perfiles de nombres de Forge/Fabric.
- Bailes completos y superpuestos mediante máscaras de huesos.
- Plantilla de nutria con textura y sus animaciones (caminar/idle, nadar, dormir, poses con objetos), más `demo_dance` como emote de ejemplo, adaptada sin modificar los originales.
- Render de equipo, armadura y élitros mediante anclajes; brazos personalizados en primera persona.
- Protocolo 2 y pruebas de recursos, permisos, cliente y sincronización multijugador.
- Wiki completa en español e inglés, con 28 temas, imágenes, recursos de ejemplo y validación en CI.

Creado por TakumiStudios.

## 0.2.0 - 2026-10-03

- Autoría TakumiStudios, paquetes com.takumistudios.morphmod y manifest del jar.
- Stonecutter para Minecraft 26.1.2, 26.2 y 26.3; Java 25 y Loom estable.
- Configuración versionada con copia de corrupción y escritura atómica fuera del hilo del juego.
- Protocolo de red 1, validación de paquetes y límite por jugador.
- Contención de errores por función, tick limitado a jugadores transformados y HUD cacheado.
- Atajos J/R/K, destransformación sin asignar y resolución de conflictos en valores por defecto.
- Gametests de servidor, pruebas de cliente y compat pack; CI y releases multiversión.
- Corregida la sincronización de vida tras recargar recursos y los cooldowns antiguos al cambiar de forma.

Creado por TakumiStudios.

## [0.1.0] - 2026-10-01

### Añadido
- Transformación en cualquier mob vivo: modelo, animaciones, hitbox y altura de ojos.
- Stats por forma mediante modificadores de atributo, con reescalado de la vida al cambiar de forma.
- 10 mobs con poderes: Warden, Creeper, Enderman, Blaze, Araña, Murciélago, Gólem de hierro, Esqueleto, Ghast y Ajolote.
- Pasivas: vuelo, trepar, inmunidad al fuego, sin daño por caída, visión nocturna, caída lenta, respirar bajo el agua, nado rápido, daño por agua, arder al sol, inmunidad a oscuridad y sentido de vibraciones.
- Desbloqueo de formas al matar mobs, que se guarda y se conserva al morir.
- Menú de formas (M) con buscador, páginas y vista previa 3D.
- HUD con la forma actual y los cooldowns.
- Teclas configurables: M, R, G y N.
- Comandos `/morph list|into|clear|unlock|reset` y `/demorph`.
- Configuración `config/morphmod.json`.
- Traducciones en español e inglés.
- Tests unitarios y test de cliente dentro del juego.
- CI con GitHub Actions.

[Unreleased]: https://github.com/AloneX15/morph-mod/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/AloneX15/morph-mod/releases/tag/v0.1.0
