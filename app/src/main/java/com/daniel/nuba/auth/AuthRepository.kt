package com.daniel.nuba.auth

import android.util.Log
import com.daniel.nuba.model.Role
import com.daniel.nuba.data.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.daniel.nuba.model.AuthUser

private const val TAG = "AuthRepository"

/**
 * Autenticación real preparada para Supabase + biometría local.
 */


interface AuthRepository {
    suspend fun signIn(email: String, password: String, role: Role): Result<AuthUser>
    suspend fun register(email: String, password: String, role: Role, name: String, username: String): Result<AuthUser>
    suspend fun signInWithFacebook(token: String, role: Role): Result<AuthUser>
    suspend fun signInWithGoogle(idToken: String, role: Role): Result<AuthUser>
    suspend fun signInWithRefreshToken(refreshToken: String, role: Role): Result<AuthUser>
    suspend fun signOut(): Result<Unit>
}

@Serializable
data class SupabaseProfile(
    val id: String,
    val full_name: String,
    val username: String,
    val role: String,
    val photo_url: String? = null,
    val bio: String? = null
)

class SupabaseAuthRepository : AuthRepository {
    private val client = SupabaseConfig.client
    private val auth = client.auth

    override suspend fun signIn(email: String, password: String, role: Role): Result<AuthUser> {
        val cleanEmail = email.trim()
        Log.d(TAG, "Iniciando signIn para: $cleanEmail")
        return try {
            auth.signInWith(Email) {
                this.email = cleanEmail
                this.password = password
            }
            Log.d(TAG, "signInWith exitoso, obteniendo sesión")
            val session = auth.currentSessionOrNull()
            Log.d(TAG, "Sesión obtenida: ${session != null}, refresh token: ${session?.refreshToken != null}")
            fetchProfile(cleanEmail, role, session?.refreshToken)
        } catch (e: Exception) {
            Log.e(TAG, "Error en signIn: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, role: Role, name: String, username: String): Result<AuthUser> {
        val cleanEmail = email.trim()
        Log.d(TAG, "Iniciando registro para: $cleanEmail")
        return try {
            // 1. Registro en Supabase Auth con metadatos
            val user = auth.signUpWith(Email) {
                this.email = cleanEmail
                this.password = password
                data = buildJsonObject {
                    put("full_name", name)
                    put("username", username)
                    put("role", role.name)
                }
            }
            
            val userId = user?.id ?: auth.currentUserOrNull()?.id
                ?: throw Exception("Registro iniciado. Por favor verifica tu correo.")

            // 2. Inserción en tabla profiles
            val profile = SupabaseProfile(
                id = userId,
                full_name = name,
                username = username,
                role = role.name
            )
            
            client.postgrest["profiles"].upsert(profile)
            
            Result.success(AuthUser(
                email = cleanEmail,
                displayName = name,
                role = role,
                uid = userId,
                isDemo = false
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Error registro: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun fetchProfile(email: String, role: Role, refreshToken: String? = null): Result<AuthUser> {
        Log.d(TAG, "fetchProfile iniciado para $email")
        return try {
            val user = auth.currentUserOrNull() ?: throw Exception("Usuario no encontrado en sesión")
            Log.d(TAG, "Usuario en sesión: ${user.id}")
            
            val profile = client.postgrest["profiles"]
                .select {
                    filter {
                        eq("id", user.id)
                    }
                }
                .decodeSingle<SupabaseProfile>()
            
            Log.d(TAG, "Perfil decodificado con éxito: ${profile.username}, rol: ${profile.role}")
            
            Result.success(AuthUser(
                email = email,
                displayName = profile.full_name,
                role = Role.valueOf(profile.role),
                uid = profile.id,
                photoUrl = profile.photo_url,
                bio = profile.bio,
                refreshToken = refreshToken
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Error en fetchProfile: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithRefreshToken(refreshToken: String, role: Role): Result<AuthUser> {
        return try {
            auth.refreshSession(refreshToken)
            val user = auth.currentUserOrNull() ?: throw Exception("Sesión inválida")
            val newSession = auth.currentSessionOrNull()
            fetchProfile(user.email ?: "", role, newSession?.refreshToken)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithFacebook(token: String, role: Role): Result<AuthUser> {
        // Implementación pendiente de configuración OAuth en Supabase
        return Result.failure(Exception("Facebook Login no implementado en Supabase aún"))
    }

    override suspend fun signInWithGoogle(idToken: String, role: Role): Result<AuthUser> {
        // Implementación pendiente de configuración OAuth en Supabase
        return Result.failure(Exception("Google Login no implementado en Supabase aún"))
    }
}



/**
 * Se mantiene para pruebas offline y compatibilidad con pantallas anteriores.
 */
class DemoAuthRepository : AuthRepository {
    override suspend fun signIn(email: String, password: String, role: Role): Result<AuthUser> {
        return if (BiometricAuth.credentialIsValid(role, email, password)) {
            Result.success(AuthUser(BiometricAuth.defaultEmail(role), BiometricAuth.defaultName(role), role, null, true))
        } else {
            Result.failure(IllegalArgumentException("Credenciales inválidas"))
        }
    }

    override suspend fun register(email: String, password: String, role: Role, name: String, username: String): Result<AuthUser> {
        return if (email.isNotBlank() && password.length >= 6) {
            Result.success(AuthUser(email, name.ifBlank { BiometricAuth.defaultName(role) }, role, null, true))
        } else {
            Result.failure(IllegalArgumentException("Completa correo y contraseña de al menos 6 caracteres"))
        }
    }

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)

    override suspend fun signInWithFacebook(token: String, role: Role): Result<AuthUser> {
        return Result.success(AuthUser("fb-demo@nuba.com", "FB Demo User", role, null, true))
    }

    override suspend fun signInWithGoogle(idToken: String, role: Role): Result<AuthUser> {
        return Result.success(AuthUser("google-demo@nuba.com", "Google Demo User", role, null, true))
    }

    override suspend fun signInWithRefreshToken(refreshToken: String, role: Role): Result<AuthUser> {
        return Result.failure(Exception("Demo no soporta Refresh Token"))
    }
}
