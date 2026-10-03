# Crear emotes y bailes

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Creating-emotes.md)

## Crear el clip

En Blockbench, crea un clip como `emote.saludo` y anima huesos existentes. Exporta el archivo de animaciones. El nombre del clip puede ser distinto del ID del emote que escriben los jugadores.

## Registrar el emote

Añade esta entrada a `emotes` de tu manifiesto:

~~~json
{
  "emotes": {
    "saludo": {
      "animation": "emote.saludo",
      "loop": false,
      "seconds": 3,
      "modes": ["full", "overlay"],
      "bones": ["right_arm", "right_hand"],
      "free": true
    }
  }
}
~~~

El clip `emote.saludo` y ambos huesos deben existir. Para un baile completo sin overlay puedes omitir `bones` y usar solo `"modes": ["full"]`.

## Máscaras overlay

El cliente crea una variante filtrada que contiene los canales de los huesos enumerados. Añade también los hijos que tengan canales propios y quieras conservar. Un padre mueve sus hijos por la jerarquía aunque estos no tengan un canal directo.

Para caminar durante un baile, evita animar piernas, raíz o padres que desplacen todo el cuerpo, salvo que ese efecto sea intencional. Una máscara de torso no evita automáticamente que un giro de ese torso influya en brazos y cabeza.

## Repetición y duración

`loop: true` repite hasta detenerlo o perder acceso. `loop: false` termina cuando el servidor alcanza `seconds`; ajusta ese valor a la duración real del clip, pues no se deriva automáticamente del archivo. La duración declarada debe ser mayor que cero y hasta 120 segundos.

## Probar y restringir

~~~text
/morph character reload
/morph character select mis_personajes:explorador
/morph emote play saludo full
/morph emote stop
/morph emote play saludo overlay
~~~

Prueba caminar y usar objetos en ambos modos, y observa el resultado desde otro cliente. El servidor sincroniza ID, modo y tiempo inicial; los observadores calculan la animación localmente.

Para restringir el emote, usa `free: false` y un permiso positivo como `morphmod.emote.use.mis_personajes.explorador.saludo`. El usuario también necesita permiso o acceso al personaje.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
