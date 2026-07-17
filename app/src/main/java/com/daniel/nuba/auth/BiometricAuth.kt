package com.daniel.nuba.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.daniel.nuba.model.Role

object BiometricAuth {
    private const val PREFS = "nuba_biometric_prefs_v2"
    private const val KEY_LAST_ROLE = "last_biometric_role"

    private fun roleKey(role: Role, suffix: String): String = "${role.name.lowercase()}_$suffix"

    fun defaultEmail(role: Role): String = when (role) {
        Role.CLIENTE -> "daniel@nuba.app"
        Role.PROVEEDOR -> "proveedor@nuba.app"
        Role.ADMIN -> "admin@nuba.app"
    }

    fun defaultPassword(role: Role): String = when (role) {
        Role.CLIENTE -> "12345678"
        Role.PROVEEDOR -> "proveedor123"
        Role.ADMIN -> "admin123"
    }

    fun defaultName(role: Role): String = when (role) {
        Role.CLIENTE -> "Daniel Apaza"
        Role.PROVEEDOR -> "Proveedor NUBA"
        Role.ADMIN -> "Administrador"
    }

    fun isEnabled(context: Context, role: Role): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(roleKey(role, "enabled"), false)

    fun savedEmail(context: Context, role: Role): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(roleKey(role, "email"), defaultEmail(role)) ?: defaultEmail(role)

    fun savedName(context: Context, role: Role): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(roleKey(role, "name"), defaultName(role)) ?: defaultName(role)

    fun lastRole(context: Context): Role? {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LAST_ROLE, null)
        return raw?.let { runCatching { Role.valueOf(it) }.getOrNull() }
    }

    fun hasAnyEnabled(context: Context): Boolean = Role.entries.any { isEnabled(context, it) }

    fun saveRole(context: Context, role: Role, email: String, name: String = defaultName(role)) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(roleKey(role, "enabled"), true)
            .putString(roleKey(role, "email"), email.ifBlank { defaultEmail(role) })
            .putString(roleKey(role, "name"), name)
            .putString(KEY_LAST_ROLE, role.name)
            .apply()
    }

    fun rememberLastRole(context: Context, role: Role) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_ROLE, role.name)
            .apply()
    }

    fun disableRole(context: Context, role: Role) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
            .remove(roleKey(role, "enabled"))
            .remove(roleKey(role, "email"))
            .remove(roleKey(role, "name"))
        if (prefs.getString(KEY_LAST_ROLE, null) == role.name) {
            val next = Role.entries.firstOrNull { it != role && isEnabled(context, it) }
            if (next == null) editor.remove(KEY_LAST_ROLE) else editor.putString(KEY_LAST_ROLE, next.name)
        }
        editor.apply()
    }

    fun credentialIsValid(role: Role, email: String, password: String): Boolean {
        val expectedEmail = defaultEmail(role)
        val expectedPassword = defaultPassword(role)
        return email.trim().equals(expectedEmail, ignoreCase = true) && password == expectedPassword
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
        description: String = "Usa la huella registrada en tu teléfono. También puedes entrar con contraseña.",
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
            .setDescription(description)
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
