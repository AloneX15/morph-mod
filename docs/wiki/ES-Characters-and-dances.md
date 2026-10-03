# Personajes y bailes

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Characters-and-dances.md)

## Elegir personaje

Entra en **J → Personajes**. El catálogo lo mantiene el servidor; no basta con poner archivos en el cliente de un servidor remoto. El servidor distribuye los recursos automáticamente. En un mundo local, tu instancia actúa también como servidor.

Los personajes gratuitos están disponibles salvo denegación de permisos. Los demás requieren desbloqueo o permiso positivo. El personaje incluido `morphmod:otter` es gratuito por defecto.

## Dos modos de baile

| Modo | Qué anima | Movimiento y manos |
|---|---|---|
| `full` | El clip completo | Sustituye locomoción y poses de manos; se detiene al desplazarte horizontalmente o actuar |
| `overlay` | Canales de huesos de la máscara | Conserva locomoción; permite caminar |

El servidor también detiene un baile completo al atacar, usar objetos o montar, y detiene emotes al dormir, morir, cambiar de forma o reconectar. Activar una habilidad detiene el emote. Un emote no repetido termina tras sus `seconds`; uno repetido sigue hasta detenerlo o perder acceso.

El modo debe estar permitido en el manifiesto. Si solo admite overlay, especifica `overlay`: el comando sin modo intenta `full`.

## Probar la plantilla

~~~text
/morph character select morphmod:otter
/morph emote list
/morph emote play demo_dance full
/morph emote stop
/morph emote play demo_dance overlay
/morph character clear
~~~

## Mecánicas y equipo

Sin `mob`, el personaje mantiene estadísticas, colisión y poderes humanos. Con `mob`, su aspecto personalizado puede acompañar las mecánicas de ese mob. Los objetos, armadura y élitros requieren anclajes del modelo para dibujarse en los lugares correctos; siguen funcionando aunque un anclaje falte.

Consulta [creación de emotes](ES-Creating-emotes.md) si quieres añadir tus propios bailes.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
