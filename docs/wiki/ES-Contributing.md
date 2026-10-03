# Contribución y seguridad

Morph **0.3.0** · [Índice](ES-Index.md) · [English](EN-Contributing.md)

## Reportar errores

Abre un issue del repositorio si tienes acceso. Incluye entorno, versiones, pasos mínimos, resultado esperado/real y logs sanitizados. Para recursos de terceros, comparte solo lo que tengas permiso de distribuir.

Los documentos de planificación y bitácora del repositorio son históricos; usa esta wiki y el código actual para comportamiento de 0.3.0.

## Contribuir código

1. Propón el cambio antes de una modificación grande.
2. Trabaja en una rama desde main y limita el alcance del PR.
3. Usa paquetes `com.takumistudios.morphmod`, identificadores/comentarios en inglés y traducciones en ambos idiomas.
4. Conserva autoría TakumiStudios y licencia MIT.
5. Prefiere APIs de Fabric; documenta mixins y sus riesgos.
6. Valida entradas externas, aísla integraciones opcionales y evita IO en tick/render.
7. Ejecuta pruebas relevantes y matriz si cambias APIs multiversión.
8. Actualiza changelog y documentación del comportamiento final.

Usa mensajes de commit descriptivos, por ejemplo `docs: document character anchors`. No incluyas cachés, mundos de prueba, tokens o artefactos de build en commits del mod.

## Mantener esta wiki

La fuente completa se versiona en `docs/wiki` del repositorio del mod. Modifica ambas páginas ES/EN de un tema, conserva el slug y comprueba enlaces, ejemplos y navegación con `node scripts/check-wiki.mjs`. Actualiza la portada si cambia versión o compatibilidad.

Para publicar también en GitHub Wiki, ejecuta `node scripts/export-wiki.mjs`. Guarda primero una página Home en GitHub, clona `https://github.com/AloneX15/morph-mod.wiki.git`, copia el contenido de `build/wiki-export/` al clon, haz commit y push a su rama predeterminada. El exportador adapta enlaces e imágenes; no publica por sí solo ni cambia la visibilidad del repositorio.

No documentes como disponible una función que solo exista en una propuesta. Al cambiar comandos, formatos o valores, contrasta implementación y pruebas. Las traducciones deben tener el mismo contenido técnico, sin traducir IDs ni campos JSON.

## Seguridad

Para vulnerabilidades usa el formulario de reporte privado del repositorio si está habilitado. Su disponibilidad depende de la configuración del propietario: esta wiki no afirma que esté activo. Si no está disponible, acuerda un canal privado con el mantenedor; no publiques secretos o instrucciones de explotación en un issue.

La visibilidad de la wiki sigue la del repositorio. Publicarla no cambia la visibilidad del código. El código del mod usa licencia MIT; respeta los derechos independientes de recursos de terceros.

---

[Inicio](Home.md) · [Índice completo](ES-Index.md) · [Ayuda](ES-Troubleshooting.md)

Creado por **TakumiStudios**.
