# Preguntas frecuentes

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-FAQ.md)

## ¿Funciona en Forge o Minecraft 1.20/1.21?

No. Esta implementación es Fabric para la matriz 26.1.x, 26.2 y 26.3. Los nombres `forge-1.20.1` y `fabric-1.21.1` son perfiles de animación de recursos, no versiones ejecutables del mod.

## ¿Todos los mobs tienen poderes?

No. Diez formas tienen definiciones propias y algunas solo pasivas. Otros mobs compatibles utilizan una definición genérica de atributos sin heredar toda su IA o ataques.

## ¿Los mobs de mi especie dejan de atacarme?

No existe esa función actualmente. Apariencia y pasivas no cambian automáticamente sus relaciones de IA.

## ¿Un personaje grande tiene una colisión grande?

No por su escala visual. Sin `mob` conserva colisión humana. Un vínculo a mob cambia mecánicas y dimensiones según ese mob.

## ¿Puedo caminar mientras bailo?

Sí, si el emote admite `overlay` y su máscara permite conservar locomoción. Un baile `full` se detiene al desplazarte horizontalmente o actuar.

## ¿Puedo usar nombres de animación propios?

Sí, mediante `bindings` y el campo `animation` de emotes. Los clips deben existir. Morph no genera idle, walk ni bailes a partir de la geometría.

## ¿Mis amigos deben copiar todos los personajes?

No. El servidor distribuye los recursos a clientes con Morph, Fabric API y GeckoLib. Los recursos se validan y cachean. No sustituyas el catálogo del servidor por archivos colocados solo en el cliente.

## ¿Se requiere LuckPerms?

No. Sin él, personajes y emotes usan su acceso predeterminado; la administración exige OP 2. Con él puedes controlar grupos, usuarios y contextos.

## ¿Por qué /demorph me pide permisos?

Es un comando administrativo OP 2. Para volver a humano sin OP usa el botón del menú, tu tecla configurada o `/morph character clear`.

## ¿Los desbloqueos se pierden al morir?

Los desbloqueos de mobs y personajes se copian al morir. La forma actual no se copia; los emotes son transitorios.

## ¿Recargar personajes cambia también morphmod.json?

No. Cambia el catálogo de personajes. Reinicia el servidor para aplicar configuración general.

## ¿Dónde reporto un problema?

Consulta [soporte y seguridad](ES-Contributing.md), incluyendo qué versiones, pasos y logs adjuntar. No publiques credenciales ni datos privados.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
