<p align="center">
  <img src="docs/images/icon.png" width="96" alt="Morph icon">
</p>

<h1 align="center">Morph</h1>

<p align="center">
  Conviértete en cualquier mob de Minecraft: toma su <b>forma</b>, sus <b>stats</b> y sus <b>poderes</b>.
</p>

<p align="center">
  <img alt="Minecraft" src="https://img.shields.io/badge/Minecraft-26.3-62B47A">
  <img alt="Fabric" src="https://img.shields.io/badge/Fabric%20Loader-0.19.5-DBD0B4">
  <img alt="Java" src="https://img.shields.io/badge/Java-25-E76F00">
  <img alt="Licencia" src="https://img.shields.io/badge/licencia-MIT-blue">
  <img alt="Build" src="https://github.com/USUARIO/MINECRAFT-MORPH-MOD/actions/workflows/build.yml/badge.svg">
</p>

---

<p align="center">
  <img src="docs/images/warden.png" width="49%" alt="Jugador transformado en Warden">
  <img src="docs/images/menu.png" width="49%" alt="Menú de formas">
</p>

## ¿Qué hace?

**Morph** es un mod de [Fabric](https://fabricmc.net/) que te permite transformarte en los mobs del mundo.
Al transformarte:

- **Tomas su forma**: ves y te ven con el modelo, la textura y las animaciones del mob, y tu hitbox y altura de ojos pasan a ser las suyas. Un murciélago cabe por un agujero de un bloque; un Warden no cabe por una puerta.
- **Heredas sus stats**: vida máxima, daño, velocidad, armadura, resistencia al empuje, altura de paso...
- **Usas sus poderes**: habilidades activas en las teclas **R** y **G**, y pasivas siempre activas, como volar, trepar paredes, no quemarte o respirar bajo el agua.

Por ejemplo, como **Warden** tienes 500 de vida y 30 de daño, lanzas el **Estallido sónico** (R), que atraviesa la armadura, lanzas un **Pulso de oscuridad** (G) y **percibes las vibraciones**: las criaturas que se mueven cerca brillan a través de las paredes.

## Cómo se juega

1. **Mata un mob** para desbloquear su forma. Verás un mensaje en el chat.
2. Pulsa **M** para abrir el menú de formas, elige una y pulsa **Transformarse**.
3. Usa **R** y **G** para las habilidades. El HUD muestra la forma actual y los cooldowns.
4. Pulsa **N** para volver a tu forma humana.

| Tecla | Acción |
|---|---|
| `M` | Abrir el menú de formas |
| `R` | Habilidad principal |
| `G` | Habilidad secundaria |
| `N` | Volver a forma humana |

Puedes cambiar las teclas en *Opciones → Controles → Morph*.

## Mobs con poderes (v0.1)

| Mob | Vida | Poder R | Poder G | Pasivas |
|---|---|---|---|---|
| **Warden** | 500 | Estallido sónico | Pulso de oscuridad | Inmune a oscuridad, percibe vibraciones, sin empuje |
| Creeper | 20 | Explotar (sin dañarte) | — | Sin daño por caída |
| Enderman | 40 | Teletransporte (32 bloques) | — | Más alcance; el agua le daña |
| Blaze | 20 | 3 bolas de fuego | — | Inmune al fuego, caída lenta; el agua le daña |
| Araña | 16 | — | — | Trepa paredes, visión nocturna |
| Murciélago | 6 | — | — | Vuela, visión nocturna |
| Gólem de hierro | 100 | Lanzar por los aires | — | Sin empuje, sin daño por caída |
| Esqueleto | 20 | Disparar flecha | — | Arde bajo el sol |
| Ghast | 10 | Bola de fuego explosiva | — | Vuela, inmune al fuego |
| Ajolote | 14 | Hacerse el muerto | — | Respira bajo el agua, nado rápido |

**Cualquier otro mob** también se puede usar: toma su forma, su vida, su daño y su armadura, aunque no tiene poderes propios.
En el menú, los mobs con poderes aparecen en azul. La tabla completa está en [docs/MOBS.md](docs/MOBS.md).

## Comandos

| Comando | Permiso | Descripción |
|---|---|---|
| `/morph list` | todos | Lista tus formas desbloqueadas |
| `/morph into <mob> [jugadores]` | OP | Transforma sin necesidad de desbloquear |
| `/morph clear [jugadores]` · `/demorph` | OP | Vuelve a forma humana |
| `/morph unlock <mob\|all> [jugadores]` | OP | Desbloquea formas |
| `/morph reset <jugadores>` | OP | Borra formas desbloqueadas y destransforma |

## Configuración

El archivo `config/morphmod.json` se crea en el primer arranque y solo lo lee el servidor:

```json
{
  "requireUnlock": true,
  "unlockOnKill": true,
  "maxHealthCap": 500.0,
  "damageMultiplier": 1.0,
  "cooldownMultiplier": 1.0,
  "allowFlight": true,
  "abilitiesBreakBlocks": true
}
```

- `requireUnlock: false` desbloquea todas las formas para todo el mundo.
- `maxHealthCap` limita la vida de cualquier forma. Bájalo a `100` si el Warden te parece demasiado fuerte.
- Las explosiones respetan además la regla `mobGriefing`.

## Instalación

1. Instala [Fabric Loader](https://fabricmc.net/use/) **0.19.5+** para Minecraft **26.3**.
2. Descarga [Fabric API](https://modrinth.com/mod/fabric-api) y ponla en `mods/`.
3. Pon `morphmod-x.y.z.jar` en `mods/`.

El mod se necesita **en el cliente y en el servidor**.

## Compilar desde el código

Requisitos: **JDK 25**. Gradle lo descarga solo si no lo tienes instalado.

```bash
git clone https://github.com/USUARIO/MINECRAFT-MORPH-MOD.git
cd MINECRAFT-MORPH-MOD
./gradlew build               # compila y pasa los tests → build/libs/morphmod-0.1.0.jar
./gradlew runClient           # abre Minecraft con el mod
./gradlew runClientGameTest   # test automático dentro del juego (crea un mundo y saca capturas)
```

## Documentación

- [Plan inicial](docs/PLAN_INICIAL.md): visión, alcance, hitos y riesgos
- [Bitácora](docs/BITACORA.md): diario de desarrollo
- [Arquitectura](docs/ARQUITECTURA.md): cómo funciona por dentro y cómo añadir un mob
- [Mobs](docs/MOBS.md): stats y poderes de cada forma
- [Changelog](CHANGELOG.md) · [Cómo contribuir](CONTRIBUTING.md)

## Hoja de ruta

- [x] v0.1: sistema de morph, 10 mobs con poderes, menú, HUD, comandos y configuración
- [ ] Más mobs con poderes (Wither, Dragón, Phantom, Breeze, Guardian...)
- [ ] Mobs de la misma especie no te atacan (un creeper no te persigue si eres creeper)
- [ ] Animaciones de ataque del mob al usar poderes
- [ ] Definir morphs por datapack (JSON)
- [ ] Integración con Mod Menu y Cloth Config
- [ ] Publicación en Modrinth y CurseForge

## Licencia

[MIT](LICENSE)

---

### English summary

**Morph** is a Fabric mod for Minecraft 26.3. Kill a mob to unlock its form, press **M** to choose a form, and take on its model, hitbox, stats and powers. Use **R** and **G** for abilities and **N** to return to human form. For example, the Warden has 500 HP, a Sonic Boom that ignores armor, a Darkness pulse and vibration sense. Every living mob can be used; 10 of them have hand-made powers in v0.1. Build with JDK 25 and `./gradlew build`.
