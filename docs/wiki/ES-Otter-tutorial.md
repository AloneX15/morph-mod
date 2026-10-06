# Tutorial completo de Otter

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Otter-tutorial.md)

## Recursos incluidos

Otter es una plantilla lista para probar. Descarga los recursos de esta wiki o usa la carpeta creada automáticamente al arrancar:

- [character.json](examples/otter/character.json)
- [otter.geo.json](examples/otter/otter.geo.json)
- [otter.animation.json](examples/otter/otter.animation.json)
- [otter.png](examples/otter/otter.png)

Guárdalos juntos en `config/morphmod/characters/otter/`. No cambies nombres sin actualizar el manifiesto.

## Probar la plantilla original

~~~text
/morph character reload
/morph character select morphmod:otter
/morph emote play demo_dance full
/morph emote stop
/morph emote play demo_dance overlay
~~~

Mira el modelo en tercera persona, camina con overlay y comprueba el brazo en primera persona. Equipa objetos, casco y élitros para probar anclajes.

## Crear una copia independiente

1. Copia los cuatro archivos a `config/morphmod/characters/explorador/`.
2. Sustituye `character.json` por [el manifiesto Explorador](examples/explorador/character.json).
3. El ID cambia a `mis_personajes:explorador`, el nombre a Explorador y se conserva el rig Otter.
4. Recarga y selecciona `mis_personajes:explorador`.
5. Abre la copia en Blockbench y modifica textura o geometría manteniendo canales y anclajes coherentes.

El manifiesto descargable conserva `demo_dance`, un emote de ejemplo añadido por Morph para mostrar los dos modos de emote; el archivo de animación propio de la nutria aporta su caminar (dentro de `idle`), nadar, dormir y poses con objetos, no un baile. Para vincular mecánicas, añade `"mob": "minecraft:bat"`: eso proporciona mecánicas del murciélago al aspecto de nutria. No transforma el modelo en un murciélago.

## Qué se adaptó

La geometría original no contiene `waist`. Se eliminaron los canales que apuntaban a ese hueso de la copia incluida; los archivos originales del usuario no se modificaron. Se añadió `emote.demo_dance` y su registro full/overlay.

El idle original incluye movimiento procedural mediante MoLang. La plantilla no tiene clips separados walk/sprint generados por Morph. Cambiar los bindings hacia nombres ausentes hará fallar la validación.

Esta plantilla es un punto de partida técnico, no una concesión de derechos sobre personajes o recursos de terceros. Conserva los permisos que correspondan a los recursos que utilices.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
