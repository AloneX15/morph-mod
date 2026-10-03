# Distribución de recursos y caché

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Resources-and-cache.md)

## Flujo de recursos

El servidor carga `config/morphmod/characters/` y valida cada carpeta. Publica un catálogo inmutable y ofrece ID, hash SHA-256 y tamaño. El cliente identifica recursos ya cacheados, recibe los restantes, valida cada ZIP y prepara un paquete GeckoLib de sesión antes de activar el catálogo.

Esto ocurre al conectar y tras `/morph character reload`. La distribución funciona para clientes del mod; no requiere que copies manualmente cada personaje en cada equipo.

## Límites

| Recurso | Límite |
|---|---|
| Personajes por catálogo | 256 |
| Archivos por personaje | 64 |
| Tamaño descomprimido por personaje | 16 MiB |
| Catálogo transferido | 64 MiB |
| Metadatos de catálogo / permisos | 32 KiB cada uno |
| Fragmento de red | 32 KiB |
| Envío por jugador y tick | Hasta 256 KiB |
| Textura PNG | Hasta 4096 × 4096 |
| Huesos / clips por personaje | 512 cada uno |
| Duración declarada de emote | Mayor que 0, hasta 120 s |

Los límites combinados importan: 256 personajes con IDs largos pueden exceder los metadatos antes de alcanzar el máximo de personajes.

## Caché

El cliente guarda recursos en `.morphmod-cache` dentro de la instancia de juego. Los ZIP se identifican por hash; una modificación cambia ese hash. La depuración se realiza durante la preparación y elimina recursos antiguos cuando la caché supera 256 MiB, conservando los necesarios para el catálogo ofrecido/activo. También retira paquetes de sesión antiguos.

No edites esa carpeta para añadir personajes. Si necesitas recuperarla, cierra el juego, copia el log y elimina únicamente la caché de esa instancia; el servidor volverá a enviar los recursos al conectar.

## Errores y seguridad

Una recarga inválida del servidor conserva el catálogo anterior. El cliente rechaza hashes incorrectos, archivos truncados, rutas inseguras o queries desconocidas. Los errores de render deshabilitan el personaje afectado y permiten volver al jugador vanilla. No se promete que recursos malformados sean reparados automáticamente.

Disco y preparación se ejecutan fuera del hilo de juego. La transferencia de configuración tiene espera acotada. Guarda `logs/latest.log` si una conexión o recarga falla.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
