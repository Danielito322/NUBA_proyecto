package com.daniel.nuba.auth

import android.content.Context
import com.daniel.nuba.model.AuthUser
import com.daniel.nuba.model.Role

interface SessionRepository {
    fun isBiometricEnabled(role: Role): Boolean
    fun saveBiometricConfig(role: Role, email: String, name: String, refreshToken: String? = null)
    fun disableBiometric(role: Role)
    fun getSavedEmail(role: Role): String
    fun getSavedName(role: Role): String
    fun getRefreshToken(role: Role): String?
    fun getLastRole(): Role?
    fun saveLastRole(role: Role)
    fun hasAnyBiometricEnabled(): Boolean

    // Nuevos métodos para rastrear al último usuario sin mezclar con biometría
    fun saveLastUsedUser(role: Role, email: String, name: String)
    fun getLastUsedEmail(role: Role): String
    fun getLastUsedName(role: Role): String
}

class SharedPreferencesSessionRepository(context: Context) : SessionRepository {
    private val prefs = context.getSharedPreferences("nuba_biometric_prefs_v2", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_ROLE = "last_biometric_role"
    }

    private fun roleKey(role: Role, suffix: String): String = "${role.name.lowercase()}_$suffix"

    override fun isBiometricEnabled(role: Role): Boolean =
        prefs.getBoolean(roleKey(role, "enabled"), false)

    override fun saveBiometricConfig(role: Role, email: String, name: String, refreshToken: String?) {
        prefs.edit().apply {
            putBoolean(roleKey(role, "enabled"), true)
            putString(roleKey(role, "email"), email)
            putString(roleKey(role, "name"), name)
            refreshToken?.let { putString(roleKey(role, "refresh_token"), it) }
            putString(KEY_LAST_ROLE, role.name)
            apply()
        }
    }

    override fun disableBiometric(role: Role) {
        val editor = prefs.edit()
            .remove(roleKey(role, "enabled"))
            .remove(roleKey(role, "email"))
            .remove(roleKey(role, "name"))
            .remove(roleKey(role, "refresh_token"))
        
        if (prefs.getString(KEY_LAST_ROLE, null) == role.name) {
            val next = Role.entries.firstOrNull { it != role && isBiometricEnabled(it) }
            if (next == null) editor.remove(KEY_LAST_ROLE) else editor.putString(KEY_LAST_ROLE, next.name)
        }
        editor.apply()
    }

    override fun getSavedEmail(role: Role): String =
        prefs.getString(roleKey(role, "email"), "") ?: ""

    override fun getSavedName(role: Role): String =
        prefs.getString(roleKey(role, "name"), "") ?: ""

    override fun getRefreshToken(role: Role): String? =
        prefs.getString(roleKey(role, "refresh_token"), null)

    override fun getLastRole(): Role? {
        val raw = prefs.getString(KEY_LAST_ROLE, null)
        return raw?.let { runCatching { Role.valueOf(it) }.getOrNull() }
    }

    override fun saveLastRole(role: Role) {
        prefs.edit().putString(KEY_LAST_ROLE, role.name).apply()
    }

    override fun hasAnyBiometricEnabled(): Boolean = Role.entries.any { isBiometricEnabled(it) }

    override fun saveLastUsedUser(role: Role, email: String, name: String) {
        prefs.edit()
            .putString(roleKey(role, "last_email"), email)
            .putString(roleKey(role, "last_name"), name)
            .apply()
    }

    override fun getLastUsedEmail(role: Role): String =
        prefs.getString(roleKey(role, "last_email"), "") ?: ""

    override fun getLastUsedName(role: Role): String =
        prefs.getString(roleKey(role, "last_name"), "") ?: ""
}
