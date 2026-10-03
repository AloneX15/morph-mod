# Mixins

Fabric API cubre eventos, red, atributos, adjuntos, HUD y teclas. Estos puntos no tienen
una API equivalente para sustituir el modelo de un jugador y mantener sus colisiones.

| Mixin | Objetivo | Motivo | Riesgo de conflicto |
|---|---|---|---|
| AvatarMixin | `Avatar.getDefaultDimensions` | Hitbox y altura de ojos de la forma; excluye dormir y morir. | Mods de tamaño y otros sistemas de morph. |
| PlayerMixin | `Player.onClimbable` | Trepar paredes solo para formas con esa pasiva. | Mods de movimiento y escalada. |
| LevelExtractorMixin | `LevelExtractor.extractVisibleEntities` (26.2+) / `LevelRenderer.extractVisibleEntities` (26.1.x) | Sustituye únicamente el estado de render de jugadores transformados mediante WrapOperation. | Mods que sustituyen completamente la extracción de entidades. |
| MinecraftMixin | `Minecraft.shouldEntityAppearGlowing` | Vibraciones para el observador transformado en Warden. | Mods que reemplazan contornos. |
| WalkAnimationStateAccessor | Campos `speedOld`, `speed`, `position` de `WalkAnimationState` | Copia animaciones al modelo separado. | Cambios de campos de Minecraft o de animaciones. |
| CharacterPackMixin | `PackRepository.discoverAvailable` | Añade el paquete de recursos del catálogo recibido. | Mods que sustituyen el repositorio de paquetes. |
| CharacterDispatcherMixin | `InventoryScreen.extractEntityInInventoryFollowsMouse` | Sustituye el personaje del inventario, conservando el estado vanilla de la cámara. | Mods que reemplazan el preview de inventario. |
| CharacterHandMixin | `AvatarRenderer.renderRightHand/renderLeftHand` | Dibuja brazos GeckoLib en primera persona; objetos mantienen el pase vanilla. | Mods de manos y animaciones de primera persona. |

Los objetivos son obligatorios y conocidos para las versiones declaradas; una nueva
versión debe compilarse y pasar los tests antes de ampliar el rango. No hay Overwrite
ni Redirect. Los fallos de ejecución en los hooks se contienen y conservan el
comportamiento vanilla. Los tests de cliente verifican dimensiones, modelo y vuelta
a la forma humana; la CI también ejecuta Lithium y FerriteCore en servidor.
