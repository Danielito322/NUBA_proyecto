package com.daniel.nuba.ui.viewmodels

import android.util.Log
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daniel.nuba.auth.AuthRepository
import com.daniel.nuba.auth.BiometricAuth
import com.daniel.nuba.auth.SupabaseAuthRepository
import com.daniel.nuba.auth.SessionRepository
import com.daniel.nuba.auth.SharedPreferencesSessionRepository
import com.daniel.nuba.model.AppRoute
import com.daniel.nuba.model.AuthUser
import com.daniel.nuba.model.Role
import com.facebook.CallbackManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val TAG = "LoginViewModel"

data class LoginUiState(
    val selectedRole: Role = Role.CLIENTE,
    val email: String = "",
    val password: String = "",
    val biometricEnabled: Boolean = false,
    val biometricAvailable: Boolean = false,
    val biometricStatus: String = "",
    val authBusy: Boolean = false,
    val pendingEnableAfterPassword: AuthUser? = null,
    val showRegisterDialog: Boolean = false,
    val registerName: String = "",
    val registerEmail: String = "",
    val registerPassword: String = "",
    val registerUsername: String = ""
)

sealed class LoginUiEvent {
    data class Navigate(val route: AppRoute, val role: Role) : LoginUiEvent()
    data class ShowToast(val message: String) : LoginUiEvent()
    data class TriggerBiometric(
        val title: String,
        val subtitle: String,
        val description: String,
        val onSuccess: () -> Unit
    ) : LoginUiEvent()
    data class LaunchGoogleLogin(val serverClientId: String) : LoginUiEvent()
    object LaunchFacebookLogin : LoginUiEvent()
}

class LoginViewModel(
    private val authRepository: AuthRepository = SupabaseAuthRepository(),
    private val sessionRepository: SessionRepository? = null
) : ViewModel() {

    val facebookCallbackManager = CallbackManager.Factory.create()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<LoginUiEvent>()
    val events = _events.receiveAsFlow()

    private var sessionRepo: SessionRepository? = sessionRepository
    private var autoPromptDone = false

    fun init(context: Context) {
        if (sessionRepo == null) {
            sessionRepo = SharedPreferencesSessionRepository(context)
        }
        val repo = sessionRepo!!
        val initialRole = repo.getLastRole() ?: Role.CLIENTE
        val email = repo.getLastUsedEmail(initialRole)
        val isBiometricEnabledForThisUser = repo.isBiometricEnabled(initialRole) && repo.getSavedEmail(initialRole) == email
        
        _uiState.value = _uiState.value.copy(
            selectedRole = initialRole,
            email = email,
            password = "",
            biometricEnabled = isBiometricEnabledForThisUser,
            biometricAvailable = BiometricAuth.canAuthenticate(context),
            biometricStatus = BiometricAuth.statusMessage(context)
        )
    }

    fun onRoleSelected(role: Role) {
        val repo = sessionRepo ?: return
        val email = repo.getLastUsedEmail(role)
        val isBiometricEnabledForThisUser = repo.isBiometricEnabled(role) && repo.getSavedEmail(role) == email
        
        _uiState.value = _uiState.value.copy(
            selectedRole = role,
            email = email,
            password = "",
            biometricEnabled = isBiometricEnabledForThisUser
        )
    }

    fun onEmailChange(email: String) {
        val repo = sessionRepo ?: return
        val isBiometricForThisEmail = repo.isBiometricEnabled(_uiState.value.selectedRole) && 
                                     repo.getSavedEmail(_uiState.value.selectedRole) == email.trim()
        
        _uiState.value = _uiState.value.copy(
            email = email,
            biometricEnabled = isBiometricForThisEmail
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onRegisterNameChange(name: String) {
        _uiState.value = _uiState.value.copy(registerName = name)
    }

    fun onRegisterEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(registerEmail = email)
    }

    fun onRegisterPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(registerPassword = password)
    }

    fun onRegisterUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(registerUsername = username)
    }

    fun onShowRegisterDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showRegisterDialog = show)
    }

    fun registerAccount() {
        Log.d(TAG, "registerAccount() llamado")
        if (_uiState.value.authBusy) {
            Log.d(TAG, "Registro ignorado: authBusy es true")
            return
        }
        
        val state = _uiState.value
        if (state.registerEmail.isBlank() || state.registerPassword.isBlank() || 
            state.registerName.isBlank() || state.registerUsername.isBlank()) {
            viewModelScope.launch { _events.send(LoginUiEvent.ShowToast("Por favor completa todos los campos")) }
            return
        }

        if (state.registerPassword.length < 8) {
            viewModelScope.launch { _events.send(LoginUiEvent.ShowToast("La contraseña debe tener al menos 8 caracteres")) }
            return
        }

        _uiState.value = _uiState.value.copy(authBusy = true)
        Log.d(TAG, "Iniciando corrutina de registro para ${state.registerEmail}")
        
        viewModelScope.launch {
            try {
                val result = authRepository.register(
                    state.registerEmail, 
                    state.registerPassword, 
                    state.selectedRole, 
                    state.registerName,
                    state.registerUsername
                )
                _uiState.value = _uiState.value.copy(authBusy = false)
                
                result.onSuccess { user ->
                    Log.d(TAG, "Registro exitoso: ${user.email}")
                    _uiState.value = _uiState.value.copy(
                        email = state.registerEmail,
                        password = state.registerPassword
                    )
                    _events.send(LoginUiEvent.ShowToast("¡Cuenta creada con éxito! Ya puedes entrar."))
                    // Redirigir a Login
                    _events.send(LoginUiEvent.Navigate(AppRoute.Login, state.selectedRole))
                }.onFailure {
                    val errorMsg = when {
                        it.message?.contains("already registered", true) == true -> "Este correo ya tiene una cuenta"
                        it.message?.contains("network", true) == true -> "Sin conexión a internet"
                        else -> it.message ?: "No se pudo crear la cuenta"
                    }
                    _events.send(LoginUiEvent.ShowToast(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción en registro: ${e.message}", e)
                _uiState.value = _uiState.value.copy(authBusy = false)
                _events.send(LoginUiEvent.ShowToast("Error: ${e.message}"))
            }
        }
    }

    private fun navigateToRole(role: Role, user: AuthUser, fromBiometric: Boolean = false) {
        val repo = sessionRepo ?: return
        repo.saveLastRole(role)
        repo.saveLastUsedUser(role, user.email, user.displayName)
        
        viewModelScope.launch {
            val route = when (role) {
                Role.CLIENTE -> AppRoute.Home
                Role.PROVEEDOR -> AppRoute.Provider
                Role.ADMIN -> AppRoute.Admin
            }
            _events.send(LoginUiEvent.ShowToast(
                if (fromBiometric) "Huella validada. ¡Hola de nuevo!" 
                else "¡Bienvenido de nuevo, ${user.displayName}!"
            ))
            _events.send(LoginUiEvent.Navigate(route, role))
        }
    }

    fun validatePasswordLogin() {
        if (_uiState.value.authBusy) return
        _uiState.value = _uiState.value.copy(authBusy = true)
        
        viewModelScope.launch {
            val state = _uiState.value
            val result = authRepository.signIn(state.email, state.password, null)
            _uiState.value = _uiState.value.copy(authBusy = false, password = "")
            
            result.onSuccess { user ->
                val repo = sessionRepo ?: return@onSuccess
                val isDifferentUser = repo.isBiometricEnabled(user.role) && repo.getSavedEmail(user.role) != user.email
                
                if ((!repo.isBiometricEnabled(user.role) || isDifferentUser) && state.biometricAvailable) {
                    _events.send(LoginUiEvent.ShowToast("¡Bienvenido, ${user.displayName}!"))
                    _uiState.value = _uiState.value.copy(pendingEnableAfterPassword = user)
                } else {
                    navigateToRole(user.role, user)
                }
            }.onFailure {
                val errorMsg = when {
                    it.message?.contains("invalid_credentials", true) == true -> "Correo o contraseña incorrectos"
                    it.message?.contains("user_not_found", true) == true -> "El usuario no existe"
                    it.message?.contains("network", true) == true -> "Sin conexión a internet"
                    else -> "Error de acceso: ${it.message}"
                }
                _events.send(LoginUiEvent.ShowToast(errorMsg))
            }
        }
    }

    fun authenticateWithFingerprint(automatic: Boolean = false) {
        val state = _uiState.value
        val repo = sessionRepo ?: return
        
        if (!repo.isBiometricEnabled(state.selectedRole)) {
            if (!automatic) viewModelScope.launch { _events.send(LoginUiEvent.ShowToast("Primero activa la huella para ${state.selectedRole.title}")) }
            return
        }
        
        val refreshToken = repo.getRefreshToken(state.selectedRole)
        if (refreshToken == null) {
            if (!automatic) viewModelScope.launch { _events.send(LoginUiEvent.ShowToast("Sesión expirada. Ingresa con contraseña.")) }
            return
        }

        if (!state.biometricAvailable) {
            viewModelScope.launch { _events.send(LoginUiEvent.ShowToast(state.biometricStatus)) }
            return
        }

        viewModelScope.launch {
            _events.send(LoginUiEvent.TriggerBiometric(
                title = "Desbloquear NUBA",
                subtitle = "${state.selectedRole.title}: ${repo.getSavedEmail(state.selectedRole)}",
                description = "Confirma tu identidad con la huella registrada en este teléfono.",
                onSuccess = { 
                    viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(authBusy = true)
                        val result = authRepository.signInWithRefreshToken(refreshToken, null)
                        _uiState.value = _uiState.value.copy(authBusy = false)
                        
                        result.onSuccess { user ->
                            // Actualizar token si cambió
                            sessionRepo?.saveBiometricConfig(user.role, user.email, user.displayName, user.refreshToken)
                            navigateToRole(user.role, user, fromBiometric = true)
                        }.onFailure {
                            _events.send(LoginUiEvent.ShowToast("Sesión inválida. Por favor usa tu contraseña."))
                        }
                    }
                }
            ))
        }
    }

    fun promptFingerprintLink(user: AuthUser, afterSuccess: Boolean = false) {
        val state = _uiState.value
        if (!state.biometricAvailable) {
            viewModelScope.launch { _events.send(LoginUiEvent.ShowToast(state.biometricStatus)) }
            return
        }

        viewModelScope.launch {
            _events.send(LoginUiEvent.TriggerBiometric(
                title = "Vincular huella",
                subtitle = "${user.role.title}: ${user.email}",
                description = "NUBA usará la huella registrada en este teléfono para próximos ingresos.",
                onSuccess = {
                    sessionRepo?.saveBiometricConfig(user.role, user.email, user.displayName, user.refreshToken)
                    _uiState.value = _uiState.value.copy(
                        biometricEnabled = true,
                        pendingEnableAfterPassword = null
                    )
                    viewModelScope.launch { _events.send(LoginUiEvent.ShowToast("Huella activada para ${user.role.title}")) }
                    if (afterSuccess) {
                        navigateToRole(user.role, user)
                    }
                }
            ))
        }
    }

    fun dismissPendingBiometric() {
        val user = _uiState.value.pendingEnableAfterPassword ?: return
        _uiState.value = _uiState.value.copy(pendingEnableAfterPassword = null)
        navigateToRole(user.role, user)
    }

    fun checkAutoPrompt() {
        val state = _uiState.value
        if (!autoPromptDone && state.biometricEnabled && state.biometricAvailable) {
            autoPromptDone = true
            viewModelScope.launch {
                kotlinx.coroutines.delay(460)
                authenticateWithFingerprint(automatic = true)
            }
        }
    }

    fun onGoogleLoginClick() {
        viewModelScope.launch {
            _events.send(LoginUiEvent.LaunchGoogleLogin("")) 
        }
    }
    
    fun onGoogleLoginResult(idToken: String) {
        if (_uiState.value.authBusy) return
        _uiState.value = _uiState.value.copy(authBusy = true)
        
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken, _uiState.value.selectedRole)
            _uiState.value = _uiState.value.copy(authBusy = false)
            result.onSuccess { user ->
                navigateToRole(user.role, user)
            }.onFailure {
                _events.send(LoginUiEvent.ShowToast("Error Google: ${it.message}"))
            }
        }
    }

    fun onFacebookLoginClick() {
        viewModelScope.launch {
            _events.send(LoginUiEvent.LaunchFacebookLogin)
        }
    }

    fun onFacebookLoginResult(token: String) {
        if (_uiState.value.authBusy) return
        _uiState.value = _uiState.value.copy(authBusy = true)
        
        viewModelScope.launch {
            val result = authRepository.signInWithFacebook(token, _uiState.value.selectedRole)
            _uiState.value = _uiState.value.copy(authBusy = false)
            result.onSuccess { user ->
                navigateToRole(user.role, user)
            }.onFailure {
                _events.send(LoginUiEvent.ShowToast("Error Facebook: ${it.message}"))
            }
        }
    }
    
    fun onAuthCancel(provider: String) {
        _uiState.value = _uiState.value.copy(authBusy = false)
        viewModelScope.launch { _events.send(LoginUiEvent.ShowToast("Login $provider cancelado")) }
    }

    fun onAuthError(provider: String, error: String) {
        _uiState.value = _uiState.value.copy(authBusy = false)
        viewModelScope.launch { _events.send(LoginUiEvent.ShowToast("$provider Error: $error")) }
    }
}
