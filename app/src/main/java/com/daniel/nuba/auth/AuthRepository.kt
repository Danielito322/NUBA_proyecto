package com.daniel.nuba.auth

import com.daniel.nuba.model.Role
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import com.daniel.nuba.model.AuthUser

/**
 * Autenticación real preparada para Firebase Auth + biometría local.
 *
 * Firebase valida correo/contraseña y crea la sesión de usuario.
 * Android BiometricPrompt no guarda huellas en Firebase: solo desbloquea en este
 * teléfono una cuenta ya verificada con correo y contraseña.
 */


interface AuthRepository {
    suspend fun signIn(email: String, password: String, role: Role): Result<AuthUser>
    suspend fun register(email: String, password: String, role: Role, name: String): Result<AuthUser>
    suspend fun signInWithFacebook(token: String, role: Role): Result<AuthUser>
    suspend fun signInWithGoogle(idToken: String, role: Role): Result<AuthUser>
    suspend fun signOut(): Result<Unit>
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {
    override suspend fun signIn(email: String, password: String, role: Role): Result<AuthUser> {
        val cleanEmail = email.trim()
        // Cuentas demo para exposición sin depender de internet o de usuarios creados en Firebase.
        if (BiometricAuth.credentialIsValid(role, cleanEmail, password)) {
            return Result.success(
                AuthUser(
                    email = BiometricAuth.defaultEmail(role),
                    displayName = BiometricAuth.defaultName(role),
                    role = role,
                    firebaseUid = null,
                    isDemo = true
                )
            )
        }

        // Evita que pase datos vacios para no realizar consultas innecesarias a Firebase
        if (cleanEmail.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Completa correo y contraseña"))
        }

        return suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(cleanEmail, password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    continuation.resume(
                        Result.success(
                            AuthUser(
                                email = user?.email ?: cleanEmail,
                                displayName = user?.displayName?.takeIf { it.isNotBlank() }
                                    ?: BiometricAuth.defaultName(role),
                                role = role,
                                firebaseUid = user?.uid,
                                isDemo = false
                            )
                        )
                    )
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(IllegalArgumentException(firebaseMessage(exception))))
                }
        }
    }

    override suspend fun signInWithGoogle(idToken: String, role: Role): Result<AuthUser> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return suspendCancellableCoroutine { continuation ->
            auth.signInWithCredential(credential)
                .addOnSuccessListener { result ->
                    val user = result.user
                    continuation.resume(
                        Result.success(
                            AuthUser(
                                email = user?.email ?: "googleuser@nuba.com",
                                displayName = user?.displayName ?: BiometricAuth.defaultName(role),
                                role = role,
                                firebaseUid = user?.uid,
                                isDemo = false
                            )
                        )
                    )
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(IllegalArgumentException(firebaseMessage(exception))))
                }
        }
    }

    override suspend fun register(email: String, password: String, role: Role, name: String): Result<AuthUser> {
        val cleanEmail = email.trim()
        val cleanName = name.trim().ifBlank { BiometricAuth.defaultName(role) }

        if (cleanEmail.isBlank()) return Result.failure(IllegalArgumentException("Ingresa un correo válido"))
        if (password.length < 6) return Result.failure(IllegalArgumentException("La contraseña debe tener al menos 6 caracteres"))

        return suspendCancellableCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(cleanEmail, password)
                .addOnSuccessListener { result ->
                    val firebaseUser = result.user
                    val profile = UserProfileChangeRequest.Builder()
                        .setDisplayName(cleanName)
                        .build()
                    firebaseUser?.updateProfile(profile)
                        ?.addOnCompleteListener {
                            continuation.resume(
                                Result.success(
                                    AuthUser(
                                        email = firebaseUser.email ?: cleanEmail,
                                        displayName = cleanName,
                                        role = role,
                                        firebaseUid = firebaseUser.uid,
                                        isDemo = false
                                    )
                                )
                            )
                        }
                        ?: continuation.resume(
                            Result.success(AuthUser(cleanEmail, cleanName, role, null, false))
                        )
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(IllegalArgumentException(firebaseMessage(exception))))
                }
        }
    }

    override suspend fun signOut(): Result<Unit> {
        auth.signOut()
        return Result.success(Unit)
    }

    override suspend fun signInWithFacebook(token: String, role: Role): Result<AuthUser> {
        val credential = FacebookAuthProvider.getCredential(token)
        return suspendCancellableCoroutine { continuation ->
            auth.signInWithCredential(credential)
                .addOnSuccessListener { result ->
                    val user = result.user
                    continuation.resume(
                        Result.success(
                            AuthUser(
                                email = user?.email ?: "fbuser@nuba.com",
                                displayName = user?.displayName ?: BiometricAuth.defaultName(role),
                                role = role,
                                firebaseUid = user?.uid,
                                isDemo = false
                            )
                        )
                    )
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(IllegalArgumentException(firebaseMessage(exception))))
                }
        }
    }

    private fun firebaseMessage(error: Exception): String {
        val raw = error.message.orEmpty()
        return when {
            raw.contains("The email address is badly formatted", ignoreCase = true) -> "El correo no tiene un formato válido"
            raw.contains("no user record", ignoreCase = true) -> "No existe una cuenta con ese correo"
            raw.contains("password is invalid", ignoreCase = true) -> "La contraseña no es correcta"
            raw.contains("already in use", ignoreCase = true) -> "Ese correo ya está registrado"
            raw.contains("network", ignoreCase = true) -> "Revisa tu conexión a internet"
            raw.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) -> "Habilita Email/Password en Firebase Authentication"
            else -> raw.ifBlank { "No se pudo validar la cuenta en Firebase" }
        }
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

    override suspend fun register(email: String, password: String, role: Role, name: String): Result<AuthUser> {
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
}
