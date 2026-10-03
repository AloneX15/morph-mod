# Resolución de problemas

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Troubleshooting.md)

## Diagnóstico rápido

| Síntoma | Comprobación / solución |
|---|---|
| No arranca | Revisa Minecraft, Java 25, Loader y JAR de dependencias; elimina duplicados |
| J no abre menú | Revisa Controles → Morph y conflictos de teclas |
| No puedo elegir mob | Comprueba desbloqueo y `requireUnlock`; usa `/morph list` |
| Personaje no aparece | Verifica carpeta del servidor, recarga, mensajes y permisos |
| Reload rechazado | Revisa JSON, archivos, IDs duplicados, huesos y límites |
| Modelo vanilla tras seleccionar | Revisa log cliente: carga GeckoLib, query o error de render |
| Baile no aparece | Selecciona personaje; revisa registro del emote y permiso |
| Full termina inmediatamente | Deja de moverte/usar objetos; espera a quedar quieto |
| Overlay mueve piernas | Revisa máscara y padres que transforman piernas |
| Equipo desplazado | Ajusta anclajes, escala y jerarquía del rig |
| Manos intercambiadas | Comprueba brazo principal y convención de swing del perfil |
| Configuración no cambia | Reinicia; reload de personajes no recarga opciones generales |
| Recursos antiguos | Espera a terminar la recarga; reconecta y revisa hashes/cache |

## Recursos inválidos

Mensajes como `unknown bone` indican un canal o máscara que referencia un hueso ausente. `Unknown bound animation` indica un binding hacia un clip inexistente. `Unsupported MoLang query` requiere eliminar o adaptar esa query. No añadas huesos o clips vacíos solo para ocultar errores sin verificar la animación.

Una textura debe ser PNG válido, no un archivo renombrado, y cumplir dimensiones máximas. Las rutas son relativas y en minúsculas. Una recarga inválida conserva el catálogo anterior: no concluyas que tus nuevos recursos se aceptaron porque todavía ves el modelo viejo.

## Conflictos de mods

Reproduce primero en una instancia de prueba con Morph y sus dependencias. Después añade los demás mods por grupos, especialmente los que afectan tamaño, render, movimiento o cámara. No retires mods de un mundo principal sin copia de seguridad.

## Información para un reporte

Incluye versión de Morph/Minecraft, Loader, Fabric API, GeckoLib, mods adicionales, si ocurre en local o dedicado, pasos mínimos y `logs/latest.log` de cliente/servidor. Adjunta el manifiesto y recursos mínimos si tienes permiso para compartirlos. Oculta datos privados.

Para recuperar caché, cierra el juego y elimina solo `.morphmod-cache` de la instancia afectada. Conserva antes el log. No borres el mundo, los desbloqueos o el catálogo como primer intento.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
