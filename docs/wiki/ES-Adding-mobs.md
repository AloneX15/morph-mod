# Añadir mobs y habilidades

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Adding-mobs.md)

## Elegir la intervención

Un mob registrado con atributos de entidad viva puede ser compatible mediante fallback sin código propio. Añade una definición dedicada cuando necesites estadísticas específicas, pasivas o poderes. No es una API pública estable ni una carga de definiciones por datapack.

## Registrar una definición

En el registro común, utiliza el builder del proyecto. Este ejemplo de definición no añade un poder activo:

~~~java
register(MorphDefinition.builder(EntityTypes.PHANTOM)
    .stat(Attributes.MAX_HEALTH, 20.0)
    .passive(Passive.FLIGHT, Passive.BURNS_IN_SUN)
    .build());
~~~

Úsalo dentro de `MorphRegistry.init()` con los imports apropiados. No modifiques la lista de exclusiones para permitir entidades que no sean compatibles con el jugador sin revisar dimensiones y render.

## Implementar un poder

`MorphAbility` requiere `id()`, `cooldownTicks()` y `activate(ServerPlayer)`. Implementación ilustrativa de una curación, para registrar como primary o secondary:

~~~java
public final class RecoverAbility implements MorphAbility {
    public String id() { return "recover"; }
    public int cooldownTicks() { return 100; }
    public boolean activate(ServerPlayer player) {
        if (!player.isAlive() || player.getHealth() >= player.getMaxHealth()) return false;
        player.heal(2.0F);
        return true;
    }
}
~~~

Añade `.primary(new RecoverAbility())` al builder. El ejemplo necesita imports de MorphAbility y ServerPlayer. `true` inicia cooldown; `false` indica que no se activó. No envíes daño, alcance o objetivos confiando en valores del cliente.

## Traducciones y pruebas

Añade `ability.morphmod.recover` a ambos idiomas del mod. Documenta estadísticas, daño, cooldown y pasivas; actualiza changelog. Prueba desbloqueo, selección, atributos, dimensiones, habilidad, cooldown y vuelta a humano.

Para proyectiles y teletransporte prueba colisiones y destinos inválidos. Para render prueba tercera/primera persona y un segundo cliente. Ejecuta la matriz multiversión y compatibilidad si cambias el núcleo.

Las referencias a clases internas de Minecraft pueden variar entre versiones: adapta con Stonecutter, no con copias independientes sin validar.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
