package com.daniel.nuba.ui.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daniel.nuba.auth.BiometricAuth
import com.daniel.nuba.auth.FirebaseAuthRepository
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.AppRoute
import com.daniel.nuba.model.AuthUser
import com.daniel.nuba.model.Role
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val authRepository = FirebaseAuthRepository()

    var selectedRole by mutableStateOf(Role.CLIENTE)
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var biometricEnabled by mutableStateOf(false)
    var autoPromptDone by mutableStateOf(false)
    var pendingEnableAfterPassword by mutableStateOf<AuthUser?>(null)
    var authBusy by mutableStateOf(false)
    
    // Register states
    var showRegisterDialog by mutableStateOf(false)
    var registerName by mutableStateOf("")
    var registerEmail by mutableStateOf("")
    var registerPassword by mutableStateOf("")

    // Biometric info
    var biometricAvailable by mutableStateOf(false)
    var biometricStatus by mutableStateOf("")

    fun init(context: Context) {
        val initialRole = BiometricAuth.lastRole(context)?.takeIf { BiometricAuth.isEnabled(context, it) } ?: Role.CLIENTE
        selectedRole = initialRole
        updateRoleState(context, initialRole)
        biometricAvailable = BiometricAuth.canAuthenticate(context)
        biometricStatus = BiometricAuth.statusMessage(context)
    }

    fun onRoleSelected(context: Context, role: Role) {
        selectedRole = role
        updateRoleState(context, role)
    }

    private fun updateRoleState(context: Context, role: Role) {
        email = BiometricAuth.savedEmail(context, role)
        password = BiometricAuth.defaultPassword(role)
        biometricEnabled = BiometricAuth.isEnabled(context, role)
    }

    fun onEnterWithRole(
        context: Context,
        appState: AppState,
        onEnter: (AppRoute) -> Unit,
        role: Role,
        fromBiometric: Boolean = false,
        resolvedEmail: String? = null,
        resolvedName: String? = null
    ) {
        val finalEmail = resolvedEmail ?: if (fromBiometric) BiometricAuth.savedEmail(context, role) else email.ifBlank { BiometricAuth.defaultEmail(role) }
        val finalName = resolvedName ?: if (fromBiometric) BiometricAuth.savedName(context, role) else BiometricAuth.defaultName(role)

        appState.role = role
        appState.userEmail = finalEmail
        appState.userName = finalName.ifBlank { BiometricAuth.defaultName(role) }
        BiometricAuth.rememberLastRole(context, role)
        appState.toast = if (fromBiometric) "Huella validada: ingreso como ${role.title}" else "Ingreso como ${role.title}"
        onEnter(routeFor(role))
    }

    private fun routeFor(role: Role): AppRoute = when (role) {
        Role.CLIENTE -> AppRoute.Home
        Role.PROVEEDOR -> AppRoute.Provider
        Role.ADMIN -> AppRoute.Admin
    }

    fun validatePasswordLogin(context: Context, appState: AppState, onEnter: (AppRoute) -> Unit) {
        if (authBusy) return
        authBusy = true
        viewModelScope.launch {
            val result = authRepository.signIn(email, password, selectedRole)
            authBusy = false
            result.onSuccess { user ->
                if (!BiometricAuth.isEnabled(context, user.role) && biometricAvailable) {
                    pendingEnableAfterPassword = user
                } else {
                    onEnterWithRole(context, appState, onEnter, user.role, resolvedEmail = user.email, resolvedName = user.displayName)
                }
            }.onFailure {
                appState.toast = it.message ?: "Correo o contraseña incorrectos para ${selectedRole.title}"
            }
        }
    }

    fun createFirebaseAccount(appState: AppState, onSuccess: () -> Unit) {
        if (authBusy) return
        authBusy = true
        viewModelScope.launch {
            val result = authRepository.register(registerEmail, registerPassword, selectedRole, registerName)
            authBusy = false
            result.onSuccess { user ->
                showRegisterDialog = false
                email = user.email
                password = registerPassword
                pendingEnableAfterPassword = user
                appState.toast = "Cuenta creada en Firebase para ${user.role.title}"
                onSuccess()
            }.onFailure {
                appState.toast = it.message ?: "No se pudo crear la cuenta"
            }
        }
    }

    fun authenticateWithFingerprint(
        context: Context,
        activity: FragmentActivity?,
        appState: AppState,
        onEnter: (AppRoute) -> Unit,
        automatic: Boolean = false
    ) {
        if (!BiometricAuth.isEnabled(context, selectedRole)) {
            if (!automatic) appState.toast = "Primero activa la huella para ${selectedRole.title}"
            return
        }
        if (!biometricAvailable) {
            appState.toast = biometricStatus
            return
        }
        if (activity == null) {
            appState.toast = "No se pudo abrir el lector biométrico"
            return
        }
        BiometricAuth.authenticate(
            activity = activity,
            title = "Desbloquear NUBA",
            subtitle = "${selectedRole.title}: ${BiometricAuth.savedEmail(context, selectedRole)}",
            description = "Confirma tu identidad con la huella registrada en este teléfono.",
            onSuccess = { onEnterWithRole(context, appState, onEnter, selectedRole, fromBiometric = true) },
            onError = { message -> if (!automatic || message != "Cancelado") appState.toast = message }
        )
    }

    fun promptFingerprintLink(
        context: Context,
        activity: FragmentActivity?,
        appState: AppState,
        onEnter: (AppRoute) -> Unit,
        user: AuthUser,
        afterSuccess: Boolean = false
    ) {
        if (!biometricAvailable) {
            appState.toast = biometricStatus
            return
        }
        if (activity == null) {
            appState.toast = "No se pudo abrir el lector biométrico"
            return
        }
        BiometricAuth.authenticate(
            activity = activity,
            title = "Vincular huella",
            subtitle = "${user.role.title}: ${user.email}",
            description = "NUBA usará la huella registrada en este teléfono para próximos ingresos. La huella no se guarda en Firebase ni en la app.",
            onSuccess = {
                BiometricAuth.saveRole(context, user.role, user.email, user.displayName)
                biometricEnabled = user.role == selectedRole && BiometricAuth.isEnabled(context, user.role)
                pendingEnableAfterPassword = null
                appState.toast = "Huella activada para ${user.role.title}"
                if (afterSuccess) {
                    onEnterWithRole(context, appState, onEnter, user.role, resolvedEmail = user.email, resolvedName = user.displayName)
                }
            },
            onError = { appState.toast = it }
        )
    }

    fun enableFingerprintAndEnter(
        context: Context,
        activity: FragmentActivity?,
        appState: AppState,
        onEnter: (AppRoute) -> Unit,
        afterSuccess: Boolean = false
    ) {
        if (!biometricAvailable) {
            appState.toast = biometricStatus
            return
        }
        if (authBusy) return
        authBusy = true
        viewModelScope.launch {
            val result = authRepository.signIn(email, password, selectedRole)
            authBusy = false
            result
                .onSuccess { user -> promptFingerprintLink(context, activity, appState, onEnter, user, afterSuccess = afterSuccess) }
                .onFailure { appState.toast = it.message ?: "No se pudo validar la cuenta" }
        }
    }

    fun checkAutoPrompt(
        context: Context,
        activity: FragmentActivity?,
        appState: AppState,
        onEnter: (AppRoute) -> Unit
    ) {
        if (!autoPromptDone && biometricEnabled && biometricAvailable) {
            autoPromptDone = true
            viewModelScope.launch {
                delay(460)
                authenticateWithFingerprint(context, activity, appState, onEnter, automatic = true)
            }
        }
    }
}
