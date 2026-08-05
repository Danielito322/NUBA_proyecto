package com.daniel.nuba.auth

import android.content.Context
import com.daniel.nuba.model.AuthUser
import com.daniel.nuba.model.Role

interface SessionRepository {
    fun isBiometricEnabled(role: Role): Boolean
    fun saveBiometricConfig(role: Role, email: String, name: String)
    fun disableBiometric(role: Role)
    fun getSavedEmail(role: Role): String
    fun getSavedName(role: Role): String
    fun getLastRole(): Role?
    fun saveLastRole(role: Role)
    fun hasAnyBiometricEnabled(): Boolean
}

class SharedPreferencesSessionRepository(context: Context) : SessionRepository {
    private val prefs = context.getSharedPreferences("nuba_biometric_prefs_v2", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_ROLE = "last_biometric_role"
    }

    private fun roleKey(role: Role, suffix: String): String = "${role.name.lowercase()}_$suffix"

    override fun isBiometricEnabled(role: Role): Boolean =
        prefs.getBoolean(roleKey(role, "enabled"), false)

    override fun saveBiometricConfig(role: Role, email: String, name: String) {
        prefs.edit()
            .putBoolean(roleKey(role, "enabled"), true)
            .putString(roleKey(role, "email"), email)
            .putString(roleKey(role, "name"), name)
            .putString(KEY_LAST_ROLE, role.name)
            .apply()
    }

    override fun disableBiometric(role: Role) {
        val editor = prefs.edit()
            .remove(roleKey(role, "enabled"))
            .remove(roleKey(role, "email"))
            .remove(roleKey(role, "name"))
        
        if (prefs.getString(KEY_LAST_ROLE, null) == role.name) {
            val next = Role.entries.firstOrNull { it != role && isBiometricEnabled(it) }
            if (next == null) editor.remove(KEY_LAST_ROLE) else editor.putString(KEY_LAST_ROLE, next.name)
        }
        editor.apply()
    }

    override fun getSavedEmail(role: Role): String =
        prefs.getString(roleKey(role, "email"), BiometricAuth.defaultEmail(role)) ?: BiometricAuth.defaultEmail(role)

    override fun getSavedName(role: Role): String =
        prefs.getString(roleKey(role, "name"), BiometricAuth.defaultName(role)) ?: BiometricAuth.defaultName(role)

    override fun getLastRole(): Role? {
        val raw = prefs.getString(KEY_LAST_ROLE, null)
        return raw?.let { runCatching { Role.valueOf(it) }.getOrNull() }
    }

    override fun saveLastRole(role: Role) {
        prefs.edit().putString(KEY_LAST_ROLE, role.name).apply()
    }

    override fun hasAnyBiometricEnabled(): Boolean = Role.entries.any { isBiometricEnabled(it) }
}
