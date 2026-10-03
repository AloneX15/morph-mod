# Referencia de character.json

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Character-manifest.md)

`character.json` es el manifiesto de una carpeta del catálogo. Usa JSON sin comentarios. Los archivos de modelo, textura y animaciones deben existir dentro de esa carpeta.

## Campos principales

| Campo | Obligatorio / default | Significado |
|---|---|---|
| `schemaVersion` | Obligatorio: 1 | Versión del formato |
| `id` | Obligatorio | Identificador único `namespace:path`, minúsculas; máximo 160 caracteres, sin `..` |
| `name` | Obligatorio | Nombre visible, no vacío, máximo 128 caracteres |
| `model` | Obligatorio | Ruta relativa de geometría |
| `texture` | Obligatorio | Ruta relativa de PNG |
| `animations` | Obligatorio | Ruta relativa de animaciones |
| `free` | Opcional: true | Acceso por defecto sin desbloqueo |
| `scale` | Opcional: 1 | Escala visual finita entre 0.1 y 4 |
| `mob` | Opcional: sin vínculo | ID de mob cuyas mecánicas se utilizarán |
| `profile` | Opcional: `fabric-1.21.1` | Convención de animación |
| `bindings` | Opcional: vacío | Acción interna → nombre de clip |
| `anchors` | Opcional: vacío | Puntos de equipo y primera persona |
| `emotes` | Opcional: vacío | Emotes disponibles |

Las rutas usan letras minúsculas, números, `_`, `.`, `/` y `-`; máximo 160 caracteres. No admiten rutas absolutas, espacios, barras invertidas ni `..`. Los IDs permiten esos caracteres de ruta y un namespace con letras minúsculas, números, `_`, `.` o `-`.

## Ejemplo mínimo válido con los recursos Otter

~~~json
{
  "schemaVersion": 1,
  "id": "mis_personajes:explorador",
  "name": "Explorador",
  "model": "otter.geo.json",
  "texture": "otter.png",
  "animations": "otter.animation.json"
}
~~~

Coloca los tres archivos de Otter junto a este manifiesto. Se crea otro personaje con aspecto de nutria; cambiar el nombre no genera geometría nueva.

## Anclajes

Cada entrada de `anchors` contiene `bone` y, opcionalmente, `position`, `rotation` y `scale`. Son vectores de tres números finitos, con valor absoluto máximo 128. Defaults: posición y rotación [0,0,0], escala [1,1,1]. Un hueso inexistente hace que se omita ese anclaje.

## Emotes

| Campo | Default / requisito |
|---|---|
| `animation` | Obligatorio; clip existente |
| `loop` | false |
| `modes` | [`"full"`]; admite full/overlay y no puede estar vacío |
| `bones` | Vacío; obligatorio no vacío si admite overlay |
| `free` | true |
| `seconds` | 5; finito, mayor que 0 y hasta 120 |

El ID de un emote admite minúsculas, números, `_`, `.` y `-`, hasta 64 caracteres. Los huesos de máscara deben existir. Para referencias de bindings y equipo, consulta [animaciones](ES-Animations.md) y [anclajes](ES-Equipment.md).

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
