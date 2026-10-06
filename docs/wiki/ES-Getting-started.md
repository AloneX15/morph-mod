# Primeros pasos

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Getting-started.md)

## Tu primera forma de mob

1. En supervivencia, mata un mob compatible. Con la configuración predeterminada desbloqueas su forma y recibes un mensaje.
2. Pulsa **J** y busca el mob.
3. Selecciónalo y pulsa **Transformarse**.
4. Mira el HUD: muestra la forma y las habilidades disponibles.
5. Usa **R** para la habilidad principal y **K** para la secundaria.
6. Vuelve a humano con el botón del menú o una tecla que hayas asignado.

Un administrador puede concederte una forma con `/morph unlock minecraft:bat Nombre`. Si el servidor desactiva `requireUnlock`, no necesitas matar mobs para elegirlos.

## Tu primer personaje

Abre **J → Personajes**, selecciona **Otter** y pulsa Transformarse. Esta plantilla es gratuita por defecto y conserva las estadísticas y colisión humanas. No necesitas matar una nutria: los personajes tienen su propio sistema de acceso.

En **Emotes**, elige `demo_dance`, un emote de ejemplo añadido por Morph (no forma parte de las animaciones propias de la nutria, que son su caminar/idle, nadar, dormir y poses con objetos). Usa `full` para reproducirlo en todo el cuerpo u `overlay` para reproducirlo mientras caminas. Detén el emote desde el menú o con:

~~~text
/morph emote stop
~~~

También puedes probar todo mediante comandos personales:

~~~text
/morph character select morphmod:otter
/morph emote play demo_dance overlay
/morph character clear
~~~

Si Otter no aparece, consulta [resolución de problemas](ES-Troubleshooting.md). Si el servidor tiene LuckPerms, una denegación explícita puede bloquear incluso la plantilla gratuita.

## Qué esperar

El personaje cambia tu apariencia; vincularlo a un mob puede cambiar tus mecánicas. Transformarte en un mob cambia también dimensiones, vida máxima y atributos definidos. La transformación conserva el porcentaje de vida al cambiar entre formas, en lugar de curarte por completo.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
