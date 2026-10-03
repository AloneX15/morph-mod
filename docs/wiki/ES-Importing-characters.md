# Importar personajes con Blockbench

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Importing-characters.md)

## Antes de exportar

Necesitas una geometría compatible con GeckoLib, una textura PNG y un archivo de animaciones. Usa un proyecto GeckoLib en Blockbench y exporta los recursos Bedrock/GeckoLib; un proyecto de skin o un modelo Java de bloques no sustituye una geometría de personaje.

El modelo debe contener **una sola geometría**, nombres únicos de huesos, padres existentes y una jerarquía sin ciclos. Cada canal de animación debe apuntar a un hueso del modelo.

## Importación paso a paso

1. Abre la geometría en Blockbench e importa su textura y animaciones.
2. Reproduce idle y los movimientos; comprueba que no haya canales de huesos borrados.
3. Exporta `explorador.geo.json`, `explorador.animation.json` y `explorador.png`.
4. Crea `config/morphmod/characters/explorador/` en el servidor.
5. Añade `character.json` con rutas relativas y bindings que correspondan a clips reales.
6. Ejecuta `/morph character reload` y revisa el mensaje y el log.
7. Selecciona `mis_personajes:explorador` y prueba movimiento, objetos, armadura y ambas vistas.

## Manifiesto inicial

~~~json
{
  "schemaVersion": 1,
  "id": "mis_personajes:explorador",
  "name": "Explorador",
  "free": true,
  "model": "explorador.geo.json",
  "texture": "explorador.png",
  "animations": "explorador.animation.json",
  "bindings": {
    "idle": "animation.explorador.idle",
    "walk": "animation.explorador.walk"
  }
}
~~~

Este ejemplo requiere que el archivo de animaciones contenga esos dos clips. Sustituye sus nombres por los reales, o usa el [tutorial Otter](ES-Otter-tutorial.md), cuyos recursos completos están incluidos.

## Prueba incremental

Carga primero geometría, textura e idle. Después añade locomoción, manos, anclajes y emotes. Así puedes localizar si un error procede de la jerarquía, un binding o una expresión MoLang.

No renombres un hueso solo en el modelo: actualiza también sus canales y anclajes. No modifiques los recursos mientras una recarga está preparándose. Consulta [manifiesto](ES-Character-manifest.md) y [problemas](ES-Troubleshooting.md).

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
