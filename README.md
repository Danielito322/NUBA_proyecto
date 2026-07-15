# NUBA Compose Nativo

Aplicación móvil nativa en Kotlin + Jetpack Compose para una plataforma de reservas.

## Avance actual

- Login por roles: Cliente, Proveedor y Administrador.
- Autenticación con huella dactilar para los tres roles.
- Activación segura de huella con contraseña + biometría.
- Desactivación segura de huella con contraseña.
- Ingreso automático con huella al abrir la app, si la cuenta ya fue activada.
- Cliente: explorar, mapa, detalle, reservar, pago, QR, tienda, carrito, reseñas y perfil.
- Proveedor: panel de negocio, horarios, productos, reservas y validación QR.
- Administrador: aprobaciones, usuarios, reportes y moderación.

## Cuentas demo

- Cliente: `daniel@nuba.app` / `12345678`
- Proveedor: `proveedor@nuba.app` / `proveedor123`
- Administrador: `admin@nuba.app` / `admin123`

## Ejecutar

1. Abrir el proyecto en Android Studio.
2. Esperar Sync Project with Gradle Files.
3. Ejecutar con Run.

## Subir cambios

```powershell
git add .
git commit -m "feat: mejora autenticacion biometrica por roles"
git push origin main
```
