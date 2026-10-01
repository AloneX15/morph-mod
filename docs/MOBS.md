# Mobs

Los valores son los de la v0.1 antes de aplicar la configuración. `maxHealthCap` limita la vida y `damageMultiplier` multiplica el daño.
Valores base del jugador: 20 de vida, 1 de daño, velocidad 0.1 y altura de paso 0.6.

## Con poderes

### Warden
| Stat | Valor |
|---|---|
| Vida | 500 |
| Daño | 30 |
| Empuje de ataque | 1.5 |
| Resistencia al empuje | 100 % |
| Velocidad | 0.085 |
| Altura de paso | 1.0 |
| Hitbox | 0.9 × 2.9 |

- **R · Estallido sónico** (3 s): rayo de 15 bloques que daña **a todas** las criaturas en la línea con 10 de daño `sonic_boom` (ignora armadura y encantamientos) y las empuja.
- **G · Pulso de oscuridad** (15 s): ruge y aplica *Oscuridad* durante 12 s a todas las criaturas en 20 bloques.
- **Pasivas:** inmune a *Oscuridad*. **Percibe vibraciones**: las criaturas que se mueven a menos de 24 bloques brillan a través de las paredes, salvo que vayan agachadas.

### Creeper
- Vida 20, hitbox 0.6 × 1.7.
- **R · Explotar** (10 s): explosión de potencia 3 centrada en ti que **no te daña ni te empuja**. Rompe bloques si `abilitiesBreakBlocks` y `mobGriefing` lo permiten.
- **Pasivas:** sin daño por caída.

### Enderman
- Vida 40, daño 7, velocidad 0.12, altura de paso 1.0, alcance de bloques 6.5 y de entidades 4. Hitbox 0.6 × 2.9.
- **R · Teletransporte** (3 s): te lleva al bloque que miras, a un máximo de 32 bloques, y anula el daño de caída acumulado.
- **Pasivas:** el agua y la lluvia te dañan.

### Blaze
- Vida 20, daño 6.
- **R · Bolas de fuego** (2 s): 3 bolas pequeñas con algo de dispersión.
- **Pasivas:** inmune al fuego y a la lava, caída lenta, el agua le daña.

### Araña
- Vida 16, daño 2, velocidad 0.12, hitbox 1.4 × 0.9.
- **Pasivas:** trepa paredes y tiene visión nocturna.

### Murciélago
- Vida 6, hitbox 0.5 × 0.9.
- **Pasivas:** **vuela**, también en supervivencia (velocidad de vuelo 0.06). Tiene visión nocturna y no recibe daño por caída.

### Gólem de hierro
- Vida 100, daño 15, resistencia al empuje 100 %, velocidad 0.08, altura de paso 1.0, hitbox 1.4 × 2.7.
- **R · Lanzar por los aires** (4 s): golpea a la criatura que tienes delante (5 bloques) con tu daño de ataque y la lanza hacia arriba.
- **Pasivas:** sin daño por caída.

### Esqueleto
- Vida 20.
- **R · Disparar flecha** (1 s): flecha sin arco ni munición, que no se puede recoger.
- **Pasivas:** arde bajo el sol si no llevas casco.

### Ghast
- Vida 10, hitbox 4 × 4.
- **R · Bola de fuego explosiva** (3 s): la explosión tiene potencia 1, o 0 si `abilitiesBreakBlocks` está desactivado.
- **Pasivas:** vuela despacio (0.03), inmune al fuego, sin daño por caída.

### Ajolote
- Vida 14, daño 2, eficiencia de movimiento en el agua al 100 %.
- **R · Hacerse el muerto** (60 s): Regeneración II y Lentitud IV durante 10 s.
- **Pasivas:** respira bajo el agua y nada rápido (*Gracia de delfín*).

## Forma genérica

Cualquier otro mob vivo (vaca, zombi, pollo, aldeano, Wither, ...) toma automáticamente:

- su **hitbox** y su **modelo**,
- su **vida**, **daño**, **armadura**, **dureza de armadura** y **resistencia al empuje** por defecto (si los tiene).

No tiene poderes ni pasivas. La velocidad no cambia, porque la velocidad interna de la IA de los mobs no es comparable con la del jugador.

## Lista negra

No se puede tomar la forma de: jugador, maniquí, soporte de armadura y dragón del End (es una entidad multiparte).
