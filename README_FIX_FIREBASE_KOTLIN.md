# Fix Firebase + Kotlin 2.0.21

Este paquete corrige el error:

`Module was compiled with an incompatible version of Kotlin. The binary version of its metadata is 2.3.0, expected version is 2.0.0.`

La causa era que `firebase-bom:34.16.0` descargaba Firebase Auth 24.x y Measurement 23.x, compilados con Kotlin más nuevo que el plugin Kotlin 2.0.21 del proyecto.

Cambios aplicados:

- Firebase BoM bajado a `33.7.0` para compatibilidad con Kotlin 2.0.21.
- Google Services plugin ajustado a `4.4.2`.
- Se mantiene `google-services.json` en `app/google-services.json`.
- Se mantiene Firebase Auth + Analytics.

Después de reemplazar, sincroniza Gradle y ejecuta la app.
