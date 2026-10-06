# Configuración

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Configuration.md)

## Archivo y aplicación

`config/morphmod.json` se crea al arrancar. Lo lee el servidor, incluido el servidor integrado de un mundo local. Reinicia para aplicar cambios. La recarga de personajes no recarga este archivo.

~~~json
{
  "schemaVersion": 2,
  "requireUnlock": true,
  "unlockOnKill": true,
  "maxHealthCap": 500.0,
  "damageMultiplier": 1.0,
  "cooldownMultiplier": 1.0,
  "allowFlight": true,
  "abilitiesBreakBlocks": false,
  "creeperBreaksBlocks": true
}
~~~

## Referencia completa

| Campo | Predeterminado | Rango / efecto |
|---|---|---|
| `schemaVersion` | 2 | Versión del formato; conserva 2 (los archivos de la versión 1 se actualizan solos) |
| `requireUnlock` | true | Exige desbloqueos para seleccionar mobs |
| `unlockOnKill` | true | Desbloquea el tipo de mob al matarlo |
| `maxHealthCap` | 500 | Número finito, 1–1024; limita vida máxima de morphs |
| `damageMultiplier` | 1 | Número finito, 0–100; multiplica daño definido y efectos que lo utilizan |
| `cooldownMultiplier` | 1 | Número finito, 0–100; escala cooldowns |
| `allowFlight` | true | Permite vuelo de formas con esa pasiva |
| `abilitiesBreakBlocks` | false | Permite destrucción por habilidades que respetan esta opción |
| `creeperBreaksBlocks` | true | La explosión del Creeper rompe bloques como el mob real, sujeta a la regla `mobGriefing` |

`requireUnlock` no concede personajes: para ellos usa `free`, desbloqueos y permisos. Desactivar `unlockOnKill` no borra las formas existentes.

## Ejemplos de administración

Para un servidor creativo con todos los mobs disponibles, usa `requireUnlock: false`. Para una progresión administrada, deja `requireUnlock: true`, desactiva `unlockOnKill` y concede formas con comandos.

Para limitar el Warden a 100 puntos, cambia `maxHealthCap` a 100. Para duplicar cooldowns, usa `cooldownMultiplier: 2`. La regla `mobGriefing` también limita la explosión del Creeper.

## Recuperación

Los números fuera de rango se acotan; los no finitos se sustituyen por defaults. Un JSON corrupto se conserva en una copia `.bak` antes de regenerar valores predeterminados. Una versión de esquema futura se conserva sin sobrescribir. El tamaño máximo del archivo es 64 KiB.

Haz cambios con el servidor detenido, usa JSON sin comentarios y revisa el log al reiniciar.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
