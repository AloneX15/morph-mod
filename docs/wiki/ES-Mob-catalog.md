# Catálogo de mobs y estadísticas

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Mob-catalog.md)

Los valores siguientes pertenecen a las diez definiciones propias de Morph 0.3.0, antes de configuración, equipo y otros efectos. Los atributos no indicados conservan el comportamiento del jugador, salvo dimensiones del tipo de mob.

| ID de Minecraft | Vida máxima | Daño definido | Velocidad definida | Otras estadísticas |
|---|---:|---:|---:|---|
| `minecraft:warden` | 500 | 30 | 0.085 | Empuje de ataque 1.5; resistencia al empuje 1; paso 1 |
| `minecraft:creeper` | 20 | — | — | — |
| `minecraft:enderman` | 40 | 7 | 0.12 | Paso 1; alcance de bloques 6.5; entidades 4 |
| `minecraft:blaze` | 20 | 6 | — | — |
| `minecraft:spider` | 16 | 2 | 0.12 | — |
| `minecraft:bat` | 6 | — | — | Velocidad de vuelo 0.06 |
| `minecraft:iron_golem` | 100 | 15 | 0.08 | Resistencia al empuje 1; paso 1 |
| `minecraft:skeleton` | 20 | — | — | — |
| `minecraft:ghast` | 10 | — | — | Velocidad de vuelo 0.03 |
| `minecraft:axolotl` | 14 | 2 | — | Eficiencia de movimiento acuático 1 |

Vida se expresa en puntos: 20 puntos equivalen a diez corazones normales. La velocidad es el valor del atributo, no bloques por segundo. Resistencia 1 representa el 100 %.

## Dimensiones

La colisión y altura de ojos se consultan del tipo de entidad de Minecraft instalado. Referencias comunes de ancho × alto: Warden 0.9 × 2.9, Creeper 0.6 × 1.7, Enderman 0.6 × 2.9, araña 1.4 × 0.9, murciélago 0.5 × 0.9, gólem 1.4 × 2.7 y Ghast 4 × 4. Para valores exactos de tu versión, el tipo de entidad es la fuente de verdad.

## Ajustes de servidor

`maxHealthCap` limita la vida máxima; `damageMultiplier` modifica el daño definido. Estos cambios no reescriben esta tabla base. Por ejemplo, con un límite de 100 el Warden no obtiene 500 de vida.

Otros mobs compatibles aparecen mediante la definición genérica, sin promesa de poderes propios. Consulta [habilidades y pasivas](ES-Abilities.md) para las mecánicas de estas diez formas.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
