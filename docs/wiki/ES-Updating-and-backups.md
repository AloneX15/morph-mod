# Actualización y copias de seguridad

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Updating-and-backups.md)

## Qué guardar

Con el servidor detenido, copia el mundo completo (incluidos datos de jugadores), `config/morphmod.json` y `config/morphmod/characters/`. Si usas LuckPerms, incluye sus datos/configuración o la copia de seguridad correspondiente a su almacenamiento.

Los desbloqueos y la selección se guardan como adjuntos en datos de jugador, no en un archivo separado de permisos de Morph. La caché cliente es regenerable y no sustituye una copia del catálogo.

## Actualizar Morph o Minecraft

1. Lee el changelog y la matriz de compatibilidad.
2. Conserva los JAR actuales y realiza la copia de seguridad.
3. Prueba la actualización en una copia del servidor y del mundo.
4. Instala el JAR correcto y dependencias de la nueva versión en servidor y clientes.
5. Retira versiones duplicadas.
6. Arranca y revisa el log; prueba selección, vuelta a humano, permisos y emotes desde dos clientes.
7. Actualiza producción cuando la prueba sea correcta.

No está garantizada la carga de mundos convertidos por una versión más nueva de Minecraft en una versión antigua.

## Actualizar personajes

Mantén IDs estables si quieres conservar la relación con desbloqueos y nodos de permiso. Cambiar `id` crea otra identidad, aunque conserve el nombre visible. Actualiza canales y anclajes al renombrar huesos.

Conserva una copia del catálogo anterior, modifica los recursos y ejecuta `/morph character reload`. Una validación fallida mantiene la versión anterior. Revisa también el log cliente: aceptación del servidor no garantiza que toda expresión o render sea válido en clientes.

## Volver atrás

Detén el servidor y restaura el conjunto coherente de mundo, configuración, personajes y JAR que guardaste. No restaures únicamente un JAR antiguo sobre un mundo ya convertido sin haberlo probado.

La configuración usa esquema 1 y preserva archivos de esquema futuro. Protocolo de red actual: 2. El manifiesto de personajes usa esquema 1. Esta wiki no promete compatibilidad entre versiones futuras.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
