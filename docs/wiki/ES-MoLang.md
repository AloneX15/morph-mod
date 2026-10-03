# Expresiones MoLang

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-MoLang.md)

MoLang permite que un canal de animación responda al tiempo, movimiento o estado del jugador. Morph valida las expresiones de los canales antes de aceptar el catálogo cliente.

## Queries admitidas

| Grupo | Queries |
|---|---|
| Tiempo | `query.anim_time`, `query.life_time` |
| Movimiento | `query.ground_speed`, `query.limb_swing`, `query.limb_swing_amount` |
| Mirada | `query.pitch`, `query.yaw`, `query.head_x_rotation`, `query.head_y_rotation` |
| Combate | `query.right_hand_swing`, `query.left_hand_swing` |
| Estado | `query.is_sneaking`, `query.is_sprinting`, `query.is_swimming`, `query.is_on_ground` |

`pitch`, `yaw` y los swings de mano se proporcionan explícitamente para personajes de Morph. Las queries estándar proceden del contexto GeckoLib y del proxy de render. No supongas unidades físicas universales para velocidad o valores de swing: comprueba visualmente su efecto en tu personaje.

## Ejemplo de canal

~~~json
{
  "rotation": [
    "math.sin(query.anim_time * 180) * 8",
    0,
    0
  ]
}
~~~

Es un fragmento para un hueso dentro de un clip, no un archivo de animaciones completo. Usa una oscilación pequeña para comprobar que el canal está activo antes de añadir expresiones más complejas.

## Validación y diagnóstico

- Los nombres de huesos deben existir en la geometría.
- Una query fuera de la lista admitida hace que el cliente rechace el catálogo.
- Las expresiones deben poder compilarse con el parser de GeckoLib.
- Valores de interpolación del formato, como linear/catmullrom, no se tratan como queries.
- Prueba caminar, correr, agacharte, nadar y cambiar mano principal.

Si un modelo funciona en Blockbench pero falla al conectarte, revisa las queries y el log del cliente. El simulador de Blockbench no demuestra que una query exista en Morph.

La plantilla Otter usa un idle procedural que ya responde a movimiento. No necesita que añadas clips vacíos llamados walk y sprint para simular esa función.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
