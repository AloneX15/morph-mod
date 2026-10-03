# Habilidades y pasivas

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Abilities.md)

## Habilidades activas

Cooldowns base, antes de `cooldownMultiplier`; 20 ticks equivalen a un segundo a velocidad normal.

| Forma / ranura | Habilidad | Cooldown | Comportamiento |
|---|---|---:|---|
| Warden / R | Estallido sónico | 3 s | Haz de 15 bloques; 10 de daño base sonic boom a criaturas alcanzadas, sin golpearte |
| Warden / K | Pulso de oscuridad | 15 s | Oscuridad durante 12 s en radio de 20 bloques |
| Creeper / R | Explosión | 10 s | Potencia 3; no te daña ni empuja |
| Enderman / R | Teletransporte | 3 s | Hasta 32 bloques; valida destino y colisión |
| Blaze / R | Bolas de fuego | 2 s | Tres bolas pequeñas con dispersión |
| Gólem / R | Lanzar por los aires | 4 s | Objetivo vivo delante, hasta 5 bloques; daño de ataque y empuje |
| Esqueleto / R | Flecha | 1 s | Sin arco ni munición; no recogible |
| Ghast / R | Bola explosiva | 3 s | Bola grande; potencia según configuración |
| Ajolote / R | Hacerse el muerto | 60 s | Regeneración II y Lentitud IV durante 10 s |

Araña y murciélago tienen pasivas, sin poderes activos propios. Un lanzamiento fallido no consume cooldown si la habilidad devuelve fallo. Los objetivos, distancias y efectos los decide el servidor. El estallido sónico usa el tipo de daño vanilla `sonic_boom`.

## Pasivas por forma

| Forma | Pasivas |
|---|---|
| Warden | Inmunidad a oscuridad; sentido de vibraciones |
| Creeper | Sin daño por caída |
| Enderman | Sensible al agua y lluvia |
| Blaze | Inmune al fuego; caída lenta; sensible al agua |
| Araña | Trepar paredes; visión nocturna |
| Murciélago | Vuelo; visión nocturna; sin daño por caída |
| Gólem | Sin daño por caída |
| Esqueleto | Arde al sol; casco evita esa comprobación |
| Ghast | Vuelo; inmune al fuego; sin daño por caída |
| Ajolote | Respiración acuática; nado rápido |

El Warden resalta entidades cercanas que se mueven, excepto las agachadas, dentro de su alcance de vibraciones de 24 bloques. No convierte a otros mobs en aliados. El vuelo depende de `allowFlight`.

La destrucción por explosiones depende de `abilitiesBreakBlocks` y, para la explosión del Creeper, de `mobGriefing`. Mantén el valor predeterminado si no quieres que las habilidades alteren bloques.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
