# NUBA - App nativa Android con Jetpack Compose

Este proyecto ya no usa WebView. Es una app Android nativa hecha con Kotlin + Jetpack Compose.

## Qué incluye

- Login inicial con selección de rol: Cliente, Proveedor y Administrador.
- Cliente: inicio, categorías, explorar, detalle, mapa, reserva, calendario mensual, pago, QR, tienda, carrito, perfil y reseñas.
- Proveedor: panel de negocio, aprobación/rechazo de reservas, edición de local, horarios semanales, productos con imagen/stock, reseñas y validación QR.
- Administrador: aprobaciones de negocios, activación/suspensión de locales, usuarios y moderación de reseñas.
- Google Maps nativo con marcadores en Puno.
- Estilo visual móvil tipo glassmorphism: fondo fotográfico, transparencias, degradados, botones grandes y navegación inferior.

## Cómo abrir

1. Descomprime el ZIP.
2. Abre Android Studio.
3. Selecciona **Open** y abre la carpeta `NUBA_Compose_Nativo`.
4. Espera la sincronización de Gradle.
5. Ejecuta con **Run** en emulador o celular.

## Google Maps

La clave de demostración está en `local.properties`:

```properties
MAPS_API_KEY=TU_CLAVE
```

El archivo `local.properties` está en `.gitignore`. No lo subas a GitHub público sin restringir la clave en Google Cloud.

## Flujo para exponer

1. Se inicia sesión como Cliente, Proveedor o Administrador.
2. Cliente elige categoría: Deportes, Belleza o Entretenimiento.
3. Cliente revisa local, fotos, servicios, tienda y reseñas.
4. Cliente reserva con calendario mensual y horarios disponibles.
5. Cliente paga con Yape, Plin o tarjeta simulada.
6. Se genera un QR de ingreso.
7. Proveedor valida el QR y controla reservas/productos.
8. Administrador aprueba negocios y modera reseñas.

## Nota

Es un avance funcional de prototipo nativo. Los datos se manejan en memoria local para presentación. No hay backend todavía.

## FIX3 - Login con huella dactilar

Se agregó autenticación biométrica nativa usando `androidx.biometric.BiometricPrompt`.

- La huella funciona como opción adicional para la cuenta **Cliente**.
- Proveedor y Administrador continúan ingresando con las cuentas predeterminadas.
- No reemplaza correo y contraseña.
- Si el celular no tiene huella registrada, la app muestra el mensaje correspondiente.

Archivos modificados:

- `app/build.gradle.kts`: dependencia `androidx.biometric` y `androidx.fragment`.
- `AndroidManifest.xml`: permisos `USE_BIOMETRIC` y `USE_FINGERPRINT`.
- `MainActivity.kt`: ahora extiende `FragmentActivity`, necesario para `BiometricPrompt`.
- `auth/BiometricAuth.kt`: helper para validar, activar y usar huella.
- `ui/screens/LoginScreens.kt`: panel de huella en la pantalla de inicio.

Flujo de prueba:

1. En el teléfono real, registra una huella en Ajustes > Seguridad.
2. Abre NUBA.
3. Elige Cliente.
4. Presiona **Activar** en “Acceso con huella”.
5. Valida con la huella.
6. Cierra sesión o reinicia la app.
7. En Cliente, presiona **Entrar** en el panel de huella.
