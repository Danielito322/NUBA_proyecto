# NUBA Compose Nativo - Huella dactilar integrada

Este proyecto mantiene el nombre de carpeta `NUBA_Compose_Nativo_Gradle85_FIX2` para que puedas reemplazar tu carpeta anterior sin cambiar rutas.

## Qué se agregó

- Login con roles: Cliente, Proveedor y Administrador.
- Huella dactilar como opción adicional solo para Cliente.
- Correo y contraseña siguen funcionando como método tradicional.
- Proveedor y Administrador mantienen cuentas predeterminadas.
- Uso real del sensor biométrico del celular mediante `BiometricPrompt`.
- Permisos biométricos en `AndroidManifest.xml`.
- Dependencias `androidx.biometric` y `fragment-ktx`.

## Cómo probar

1. El celular debe tener una huella registrada en Ajustes.
2. Abre la app.
3. Selecciona `Cliente`.
4. Presiona `Activar huella`.
5. Valida con el dedo.
6. Desde el siguiente ingreso usa `Entrar con huella`.

## Archivos principales modificados

- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/daniel/nuba/MainActivity.kt`
- `app/src/main/java/com/daniel/nuba/auth/BiometricAuth.kt`
- `app/src/main/java/com/daniel/nuba/ui/screens/LoginScreens.kt`

## Nota

La huella no se guarda dentro de la app. Android valida la huella registrada en el sistema del teléfono.
La app solo guarda localmente si el acceso biométrico fue activado para Cliente.
