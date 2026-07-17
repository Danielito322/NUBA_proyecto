# Login con huella dactilar - NUBA

Esta versión implementa autenticación biométrica nativa con `BiometricPrompt`.

## Qué hace

- La huella funciona para los tres roles: Cliente, Proveedor y Administrador.
- Al abrir la app, si hay una cuenta con huella activada, NUBA solicita la huella automáticamente.
- El usuario también puede entrar con correo y contraseña.
- Activar huella requiere primero credenciales correctas y luego validación biométrica.
- Desactivar huella requiere confirmar la contraseña del rol seleccionado.
- La huella no se guarda en la app. Android solo devuelve si el usuario fue validado por el sistema.

## Cuentas de prueba

- Cliente: `daniel@nuba.app` / `12345678`
- Proveedor: `proveedor@nuba.app` / `proveedor123`
- Administrador: `admin@nuba.app` / `admin123`

## Flujo UX

1. Seleccionar rol.
2. Ingresar con contraseña o activar huella.
3. Si se activa huella, la próxima vez la app solicita la huella al entrar.
4. Para quitar la huella, se solicita contraseña para evitar desactivar la seguridad accidentalmente.

## Requisitos de prueba

- Celular físico o emulador con biometría configurada.
- Huella registrada en Ajustes del sistema.
- En emulador, usar Extended Controls > Fingerprint.
