# Seguridad

Versiones mantenidas: la versión más reciente de Morph para Minecraft 26.1.x,
26.2 y 26.3, con Java 25 y Fabric API.

Para una vulnerabilidad, usa [el reporte privado de GitHub](https://github.com/AloneX15/morph-mod/security/advisories/new).
Incluye versión del mod y de Minecraft, pasos de reproducción e impacto. No publiques
datos de jugadores, tokens ni instrucciones de explotación en una issue pública.

La disponibilidad del formulario depende de que el propietario habilite los reportes
privados en Settings → Security del repositorio.

Los paquetes se validan en el servidor: protocolo, estado del jugador, formas desbloqueadas,
identificadores de hasta 256 caracteres, slots y límite de ocho solicitudes por segundo.
Las habilidades calculan alcance y objetivos en el servidor. Las explosiones no rompen
bloques por defecto. La configuración corrupta se conserva en `.bak` antes de regenerarse.

Creado por **TakumiStudios**.
