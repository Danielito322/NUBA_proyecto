# NUBA - App móvil nativa con Jetpack Compose

NUBA es una aplicación móvil de reservas para Deportes, Belleza y Entretenimiento. Incluye cliente, proveedor y administrador.

## Avance actual

- Kotlin + Jetpack Compose
- Login por roles
- Firebase Auth conectado
- Huella dactilar con BiometricPrompt
- Mapa de Puno
- Reservas, pago simulado, QR, tienda, carrito y reseñas
- Panel proveedor
- Panel administrador

## Firebase

El proyecto ya incluye `app/google-services.json` y las dependencias Firebase necesarias para autenticación.

Para que el registro/login real funcione, habilita en Firebase Console:

Authentication > Sign-in method > Email/Password

## Biometría

La huella funciona como acceso rápido después de validar la cuenta con contraseña. No reemplaza el login tradicional.

## Ejecutar

1. Abrir carpeta en Android Studio.
2. Sync Project with Gradle Files.
3. Run.

Si aparece error de SDK, crea `local.properties` con:

```properties
sdk.dir=C\:\\Users\\DANIEL\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=TU_CLAVE_GOOGLE_MAPS
```
