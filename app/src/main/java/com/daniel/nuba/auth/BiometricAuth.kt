package com.daniel.nuba.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAuth {
    private const val PREFS = "nuba_biometric_prefs"
    private const val KEY_ENABLED = "biometric_enabled_client"
    private const val KEY_EMAIL = "client_email"
    private const val KEY_NAME = "client_name"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)

    fun savedEmail(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_EMAIL, "daniel@nuba.app") ?: "daniel@nuba.app"

    fun savedName(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_NAME, "Daniel Apaza") ?: "Daniel Apaza"

    fun saveClient(context: Context, email: String, name: String = "Daniel Apaza") {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, true)
            .putString(KEY_EMAIL, email.ifBlank { "daniel@nuba.app" })
            .putString(KEY_NAME, name)
            .apply()
    }

    fun disable(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun statusMessage(context: Context): String {
        return when (BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Huella disponible"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Este dispositivo no tiene sensor biométrico"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "El sensor biométrico no está disponible"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Primero registra una huella en Ajustes del teléfono"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "El sistema requiere actualización de seguridad"
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "Biometría no soportada en este dispositivo"
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "Estado biométrico desconocido"
            else -> "Biometría no disponible"
        }
    }

    fun canAuthenticate(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Huella no reconocida. Intenta de nuevo.")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription("Usa la huella registrada en tu teléfono. El correo y contraseña siguen disponibles.")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Usar contraseña")
            .build()

        prompt.authenticate(promptInfo)
    }
}

fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) return current
        val base = current.baseContext
        if (base === current) break
        current = base
    }
    return current as? FragmentActivity
}
