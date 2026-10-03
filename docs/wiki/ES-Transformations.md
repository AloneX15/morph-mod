# Transformaciones y desbloqueos

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Transformations.md)

## Formas de mobs

Con `requireUnlock: true`, debes desbloquear una forma antes de seleccionarla desde el menú. Con `unlockOnKill: true`, matar un mob compatible añade su tipo a tus formas desbloqueadas. Los desbloqueos de mobs persisten y se copian al morir.

Los comandos administrativos pueden desbloquear o forzar transformaciones. `requireUnlock: false` permite usar formas sin ese requisito. No sustituye los permisos de personajes.

## Estadísticas y salud

Los atributos definidos se aplican mediante modificadores del jugador. La configuración limita vida máxima y multiplica daño. Al cambiar de forma se conserva la proporción de salud: 10/20 equivale a 250/500 antes de otros ajustes. Volver a humano retira los modificadores y pasivas del morph y restaura el vuelo de acuerdo con el modo de juego.

Las dimensiones y altura de ojos de una forma de mob proceden de su tipo de entidad. Un murciélago puede pasar por espacios menores; un mob grande necesita espacio. No se promete que cualquier transformación forzada encuentre automáticamente una posición segura.

## Mobs genéricos

El mod acepta tipos de entidad con atributos de entidad viva, salvo exclusiones. Los mobs sin definición propia reciben los atributos disponibles de vida, daño, armadura, dureza y resistencia al empuje. No heredan automáticamente toda la IA, ataques o habilidades del mob.

Se excluyen jugadores, soportes para armaduras, maniquíes, dragón y el proxy interno de personajes. Los mobs de otros mods dependen de su registro y compatibilidad; no están garantizados.

## Personajes y persistencia

Los personajes usan desbloqueos separados, `free` y permisos. Sin vínculo a mob mantienen colisión humana; `scale` solo cambia su apariencia. La selección se guarda y se valida al entrar; no se copia la forma actual al morir. Los desbloqueos sobreviven a la muerte. Los emotes no persisten y se detienen al reconectar o cambiar de forma.

`/morph reset` borra desbloqueos de **mobs** y destransforma; no es un comando para borrar todos los desbloqueos de personajes.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
