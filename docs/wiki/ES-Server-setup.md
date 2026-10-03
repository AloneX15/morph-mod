# Instalación del servidor

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Server-setup.md)

## Preparación

Instala un servidor Fabric con Java 25 y los JAR de Morph, Fabric API y GeckoLib correspondientes a la versión de Minecraft. Usa la misma versión de Morph en ambos lados. LuckPerms es opcional y va solo en el servidor; el perfil de desarrollo probado utiliza LuckPerms Fabric 5.5.85.

1. Haz una copia de seguridad si ya existe un mundo.
2. Coloca las dependencias en `mods/` del servidor.
3. Arranca y comprueba el log.
4. Conéctate con un cliente que tenga las mismas dependencias.
5. Prueba una forma de mob y `morphmod:otter`.

## Archivos creados

~~~text
config/
  morphmod.json
  morphmod/
    characters/
      otter/
        character.json
        otter.geo.json
        otter.animation.json
        otter.png
~~~

La plantilla se copia cuando la carpeta Otter no existe. Edita el catálogo del servidor, no las cachés de clientes.

## Administrar recursos

Añade cada personaje en una carpeta propia y usa `/morph character reload`. Esta recarga valida y distribuye el catálogo completo. No recarga `morphmod.json`: para cambiar la configuración general, reinicia el servidor.

~~~text
/morph character reload
/morph character unlock mis_personajes:explorador Nombre
/morph unlock minecraft:bat Nombre
~~~

La consola puede recargar y ejecutar operaciones que admiten objetivos. Los comandos personales necesitan un jugador: no uses `character select` en la consola esperando transformar a otro.

## Acceso y diagnóstico

Por defecto las operaciones administrativas requieren OP 2. LuckPerms puede controlar administración del catálogo y acceso a personajes/emotes, pero no sustituye el requisito OP de los comandos administrativos de mobs.

Sin canales del cliente, los comandos de mobs siguen disponibles; ese cliente no dispone de la UI ni los modelos del mod. Para todas las funciones, instala Morph en ambos lados. Consulta [red y caché](ES-Resources-and-cache.md) y [problemas](ES-Troubleshooting.md).

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
