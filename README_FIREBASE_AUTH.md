# NUBA - Firebase Auth + biometría

Esta versión ya incluye `app/google-services.json`, el plugin de Google Services y Firebase Authentication.

## Qué se conectó

- Proyecto Firebase: `nuba-49367`
- Paquete Android: `com.daniel.nuba`
- Plugin Gradle: `com.google.gms.google-services`
- Firebase BoM: `34.16.0`
- Firebase Analytics
- Firebase Authentication

## Funcionamiento correcto de seguridad

1. El usuario selecciona su rol: Cliente, Proveedor o Administrador.
2. Ingresa con correo y contraseña.
3. Firebase Auth valida la cuenta.
4. Después de validar la contraseña, NUBA pregunta si desea activar huella.
5. La huella queda vinculada solo en ese teléfono mediante Android BiometricPrompt.
6. La próxima vez la app puede pedir huella automáticamente.

La huella NO se guarda en Firebase. Firebase controla la cuenta y Android controla la autenticación biométrica local.

## Cuentas demo para exposición

Estas cuentas siguen funcionando sin conexión para la demostración rápida:

- Cliente: `daniel@nuba.app` / `12345678`
- Proveedor: `proveedor@nuba.app` / `proveedor123`
- Administrador: `admin@nuba.app` / `admin123`

## Para usar Firebase real

En Firebase Console entra a:

Authentication > Sign-in method > Email/Password > Enable

Luego puedes crear usuarios desde la app usando el botón `Crear cuenta con Firebase`.

## Importante para GitHub

`app/google-services.json` puede subirse si el repositorio es académico, pero en un proyecto real conviene restringir las claves en Google Cloud/Firebase y revisar permisos.
