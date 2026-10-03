# Arquitectura y sincronización

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Architecture.md)

## Autoridad del servidor

`MorphRegistry` resuelve definiciones propias y fallback de mobs. `MorphManager` mantiene transformación, atributos, vuelo, pasivas y cooldowns. `CharacterManager` controla selección, emotes y permisos. Los comandos y paquetes usan estas operaciones; el cliente no decide objetivos ni daño.

La forma actual y el personaje se guardan como adjuntos sincronizados. Los desbloqueos de mobs se sincronizan solo al propietario; el acceso al catálogo de personajes se envía mediante metadatos. El emote es transitorio y contiene ID, modo e instante inicial.

## Catálogo

`CharacterCatalog` realiza IO fuera del tick y publica snapshots inmutables. `CharacterBundle` valida recursos y genera ZIP/hash. `CharacterNetworking` negocia recursos en configuración y juego, transmite fragmentos con presupuesto por tick y envía acceso por usuario.

En el cliente, `ClientCharacters` valida archivos, MoLang y hashes, prepara un paquete de sesión y activa el catálogo. Desconexiones y nuevas ofertas invalidan trabajo anterior para evitar aplicar una sesión obsoleta.

## Render

Los mobs utilizan entidades de representación que no se añaden al mundo. Los personajes usan `CharacterEntity`, un proxy GeckoLib separado que refleja pose, equipo, rotación y movimiento del jugador. Los controladores combinan locomoción, manos, combate y emotes.

El mundo sustituye estados de render visibles; el inventario tiene un hook específico. La extracción del avatar local se conserva para cámara y objetos vanilla. Un hook de manos dibuja brazos personalizados. No sustituyas globalmente el estado del avatar local por un estado genérico: rompe las expectativas de Minecraft.

## Red y fiabilidad

El protocolo actual es **2**. El servidor comprueba negociación, estado, IDs, desbloqueos, ranuras y límites de solicitudes. Los comandos siguen disponibles sin canales del cliente. Los adjuntos transmiten cambios de estado; no se envían poses completas cada frame.

Los callbacks críticos usan guards y los renderizadores conservan fallback. La selección cambia atributos por transición y el tick recorre UUID activos. Los ocho mixins se describen en el documento MIXINS del repositorio.

## LuckPerms

La integración está aislada y es opcional. Consulta permisos efectivos/contextos; los estados indefinidos usan defaults. Evita referencias a clases opcionales desde rutas que se cargan sin LuckPerms.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
