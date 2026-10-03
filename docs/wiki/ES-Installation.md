# Instalación y compatibilidad

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Installation.md)

## Requisitos

Usa Fabric y Java 25. Instala Morph, Fabric API y GeckoLib en cliente y servidor para disponer de todas las funciones. LuckPerms es opcional y solo se instala en el servidor.

| Minecraft probado | JAR de Morph | Fabric API de desarrollo | GeckoLib probado |
|---|---|---|---|
| 26.1.2 | `morphmod-0.3.0+mc26.1.2.jar` | 0.155.3+26.1.2 | 5.5.2 |
| 26.2 | `morphmod-0.3.0+mc26.2.jar` | 0.161.0+26.2 | 5.5.5 |
| 26.3 | `morphmod-0.3.0+mc26.3.jar` | 0.161.0+26.3 | 5.5.7 |

Fabric Loader mínimo: **0.19.5**. El JAR de 26.1.2 declara el rango 26.1.x; las pruebas locales se realizaron con 26.1.2, no individualmente con 26.1 y 26.1.1. No hay soporte de Forge, versiones anteriores a 26.1 ni snapshots.

## Instalar en el cliente

1. Instala Fabric Loader para tu versión de Minecraft.
2. Coloca el JAR correspondiente, Fabric API y GeckoLib en `mods/` de esa instancia.
3. Elimina JAR duplicados o de otras versiones de Minecraft.
4. Arranca el perfil Fabric y abre **Opciones → Controles → Morph**.
5. Prueba **J → Personajes → Otter** en un mundo local.

Obtén los mods desde sus proyectos oficiales: [Fabric](https://fabricmc.net/use/), [Fabric API](https://modrinth.com/mod/fabric-api) y [GeckoLib](https://modrinth.com/mod/geckolib). Para Morph, usa una release disponible del repositorio o [compílalo](ES-Building.md); Modrinth y CurseForge no se presentan como canales publicados de Morph.

## Compatibilidad comprobada

Las pruebas de servidor cubren Lithium y FerriteCore, además de LuckPerms por separado. No garantizan otros modpacks. Los mods que cambian tamaño, movimiento, render del jugador o primera persona pueden entrar en conflicto: consulta [problemas](ES-Troubleshooting.md).

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
