# Equipo y brazos en primera persona

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Equipment.md)

## Anclajes disponibles

| Nombre | Uso |
|---|---|
| `right_hand`, `left_hand` | Objetos en las manos |
| `head` | Casco |
| `chest` | Pechera |
| `right_arm`, `left_arm` | Segmentos de armadura y selección de brazo en primera persona |
| `right_leg`, `left_leg` | Segmentos de piernas |
| `right_foot`, `left_foot` | Botas |
| `elytra` | Élitros |

Un anclaje contiene un nombre de hueso y transformaciones locales. Posición utiliza unidades del modelo: 16 unidades equivalen a un bloque. Rotación utiliza grados. Escala es un multiplicador.

## Ejemplo con el rig Otter

~~~json
{
  "anchors": {
    "right_hand": {"bone": "right_hand_item"},
    "left_hand": {"bone": "left_hand_item"},
    "head": {"bone": "armorBipedHead"},
    "right_arm": {"bone": "right_arm", "scale": [0.6, 0.6, 0.6]},
    "left_arm": {"bone": "left_arm", "scale": [0.6, 0.6, 0.6]},
    "chest": {"bone": "body_upper", "scale": [0.6, 0.6, 0.6]},
    "elytra": {"bone": "body_upper", "scale": [0.6, 0.6, 0.6]}
  }
}
~~~

Incorpora este fragmento a un manifiesto que use esos huesos. En tu propio rig, cambia nombres y offsets. Un anclaje inexistente se omite; no invalida por sí solo el personaje y el equipo mantiene sus efectos de juego.

## Ajustar en el juego

1. Carga el modelo sin equipo y verifica su escala visual.
2. Sujeta una espada y un objeto en la otra mano; comprueba ambos anclajes.
3. Usa arco, escudo y consumibles para revisar poses y mano activa.
4. Equipa casco, pechera, pantalones, botas y élitros.
5. Comprueba primera persona con la mano vacía y con objetos, en ambos brazos.
6. Ajusta offsets y recarga el catálogo.

La primera persona conserva la cámara y objetos de Minecraft mientras sustituye el brazo cuando el rig lo permite. La jerarquía de pivotes y los huesos descendientes importan. No todos los rigs admiten offsets idénticos ni una armadura visual perfecta sin ajustes.

![Brazo Otter en primera persona](images/first-person-arm.png)

`scale` del personaje cambia tamaño visual, no la colisión humana. Las escalas de anclajes afectan la colocación del equipo, no sus atributos.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
