# Presentación

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Overview.md)

Morph permite adoptar la forma y mecánicas de mobs, o utilizar personajes propios animados con GeckoLib. Esta wiki describe **Morph 0.3.0** tal como está implementado.

## Dos formas de transformarte

| Tipo | Aspecto | Estadísticas y colisión | Poderes |
|---|---|---|---|
| Mob | Modelo y animaciones del mob | Según su definición | Activas y pasivas si están definidas |
| Personaje personalizado | Geometría, textura y animaciones importadas | Humanas por defecto | Humanas, salvo vínculo explícito a un mob |

Los personajes pueden incluir bailes completos o superpuestos. El servidor selecciona la forma y decide los permisos; los clientes dibujan los modelos y reciben los recursos del catálogo.

## Empieza por aquí

- Jugadores: [instalación](ES-Installation.md) y [primeros pasos](ES-Getting-started.md).
- Administradores: [servidores](ES-Server-setup.md), [configuración](ES-Configuration.md) y [permisos](ES-Permissions.md).
- Creadores: [importación](ES-Importing-characters.md), [animaciones](ES-Animations.md) y [Otter](ES-Otter-tutorial.md).
- Desarrolladores: [compilación](ES-Building.md) y [arquitectura](ES-Architecture.md).

![Otter en el juego](images/otter.png)

## Funciones disponibles y previstas

Hay diez definiciones de mobs con mecánicas propias, soporte genérico para otros mobs compatibles, catálogo de personajes del servidor, emotes y LuckPerms opcional. No están implementados los morphs por datapack, la integración con Mod Menu/Cloth Config ni la neutralidad automática de mobs de tu misma especie. Los bailes requieren clips en tus recursos: el mod no crea animaciones por sí solo.

La wiki acompaña al código local de 0.3.0; no implica que exista una release pública de esa versión.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
