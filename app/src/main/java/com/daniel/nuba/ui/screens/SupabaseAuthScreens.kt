package com.daniel.nuba.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniel.nuba.auth.findFragmentActivity
import com.daniel.nuba.auth.BiometricAuth
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.AppRoute
import com.daniel.nuba.model.Role
import com.daniel.nuba.ui.components.GlassCard
import com.daniel.nuba.ui.components.PrimaryButton
import com.daniel.nuba.ui.components.SecondaryButton
import com.daniel.nuba.ui.components.ToastMessage
import com.daniel.nuba.ui.theme.NubaCyan
import com.daniel.nuba.ui.theme.NubaMuted
import com.daniel.nuba.ui.theme.NubaText
import com.daniel.nuba.ui.theme.NubaViolet
import com.daniel.nuba.ui.viewmodels.LoginViewModel
import com.daniel.nuba.ui.viewmodels.LoginUiEvent
import androidx.compose.material.icons.outlined.Fingerprint

@Composable
fun SupabaseLoginScreen(
    appState: AppState,
    onNavigate: (AppRoute) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.registerEmail).matches()
    val isPasswordValid = uiState.registerPassword.length >= 8
    val isFormValid = uiState.registerName.isNotBlank() && 
            uiState.registerUsername.isNotBlank() && 
            isEmailValid && 
            isPasswordValid
    val activity = context.findFragmentActivity()

    LaunchedEffect(Unit) {
        viewModel.init(context)
        viewModel.events.collect { event ->
            when (event) {
                is LoginUiEvent.Navigate -> {
                    appState.role = uiState.selectedRole
                    appState.userEmail = uiState.email
                    // Obtenemos el nombre del usuario real que acaba de entrar
                    appState.userName = BiometricAuth.savedName(context, uiState.selectedRole).ifBlank { "Usuario" }
                    onNavigate(event.route)
                }
                is LoginUiEvent.ShowToast -> appState.toast = event.message
                is LoginUiEvent.TriggerBiometric -> {
                    activity?.let {
                        BiometricAuth.authenticate(
                            activity = it,
                            title = event.title,
                            subtitle = event.subtitle,
                            description = event.description,
                            onSuccess = event.onSuccess,
                            onError = { msg -> appState.toast = msg }
                        )
                    }
                }
                else -> {}
            }
        }
    }

    if (uiState.pendingEnableAfterPassword != null) {
        val user = uiState.pendingEnableAfterPassword!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissPendingBiometric() },
            title = { Text("Acceso Rápido") },
            text = { Text("¿Deseas activar el ingreso con huella dactilar para tu próxima visita?") },
            confirmButton = {
                TextButton(onClick = { viewModel.promptFingerprintLink(user, afterSuccess = true) }) {
                    Text("Activar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPendingBiometric() }) {
                    Text("Ahora no")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF040A13), Color(0xFF0A1122), Color(0xFF030710))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "NUBA",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
            Text(
                text = "Bienvenido de nuevo",
                color = NubaCyan,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(40.dp))

            GlassCard(radius = 24) {
                Text(
                    text = "Iniciar Sesión",
                    color = NubaText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                AuthField(
                    label = "Correo Electrónico",
                    value = uiState.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    icon = Icons.Outlined.Email,
                    keyboardType = KeyboardType.Email,
                    isError = uiState.email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.email).matches()
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthField(
                    label = "Contraseña",
                    value = uiState.password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    icon = Icons.Outlined.Lock,
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrimaryButton(
                        text = if (uiState.authBusy) "Entrando..." else "Entrar",
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.authBusy
                    ) {
                        viewModel.validatePasswordLogin()
                    }

                    if (uiState.biometricEnabled && uiState.biometricAvailable) {
                        IconButton(
                            onClick = { viewModel.authenticateWithFingerprint() },
                            modifier = Modifier
                                .size(56.dp)
                                .background(NubaCyan.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .border(1.dp, NubaCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Fingerprint,
                                contentDescription = "Huella",
                                tint = NubaCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SecondaryButton(
                    text = "Crear cuenta nueva",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    onNavigate(AppRoute.Register)
                }
            }
        }
        ToastMessage(appState.toast, onDismiss = { appState.toast = null })
    }
}

@Composable
fun SupabaseRegisterScreen(
    appState: AppState,
    onNavigate: (AppRoute) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.registerEmail).matches()
    val isPasswordValid = uiState.registerPassword.length >= 8
    val isFormValid = uiState.registerName.isNotBlank() && 
            uiState.registerUsername.isNotBlank() && 
            isEmailValid && 
            isPasswordValid

    LaunchedEffect(Unit) {
        viewModel.init(context)
        viewModel.events.collect { event ->
            when (event) {
                is LoginUiEvent.Navigate -> onNavigate(event.route)
                is LoginUiEvent.ShowToast -> appState.toast = event.message
                else -> {}
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF040A13), Color(0xFF0A1122), Color(0xFF030710))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate(AppRoute.Login) }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Text(
                    text = "Registro",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            GlassCard(radius = 24) {
                Text(
                    text = "Crea tu cuenta",
                    color = NubaText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                AuthField(
                    label = "Nombre Completo",
                    value = uiState.registerName,
                    onValueChange = { viewModel.onRegisterNameChange(it) },
                    icon = Icons.Outlined.Badge,
                    keyboardType = KeyboardType.Text
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthField(
                    label = "Nombre de Usuario",
                    value = uiState.registerUsername,
                    onValueChange = { viewModel.onRegisterUsernameChange(it) },
                    icon = Icons.Outlined.Person,
                    keyboardType = KeyboardType.Text
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthField(
                    label = "Correo Electrónico",
                    value = uiState.registerEmail,
                    onValueChange = { viewModel.onRegisterEmailChange(it) },
                    icon = Icons.Outlined.Email,
                    keyboardType = KeyboardType.Email,
                    isError = uiState.registerEmail.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.registerEmail).matches()
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthField(
                    label = "Contraseña",
                    value = uiState.registerPassword,
                    onValueChange = { viewModel.onRegisterPasswordChange(it) },
                    icon = Icons.Outlined.Lock,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    isError = uiState.registerPassword.isNotEmpty() && uiState.registerPassword.length < 8
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Tipo de cuenta:",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Role.entries.filter { it != Role.ADMIN }.forEach { role ->
                        val active = uiState.selectedRole == role
                        FilterChip(
                            selected = active,
                            onClick = { viewModel.onRoleSelected(role) },
                            label = { Text(role.title) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NubaViolet.copy(alpha = 0.4f),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.05f),
                                labelColor = NubaMuted
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                PrimaryButton(
                    text = if (uiState.authBusy) "Registrando..." else "Registrarse",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.authBusy && isFormValid
                ) {
                    viewModel.registerAccount()
                }
            }
        }
        ToastMessage(appState.toast, onDismiss = { appState.toast = null })
    }
}

@Composable
fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    isError: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                        tint = if (passwordVisible) NubaCyan else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        } else null,
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = isError,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = NubaCyan,
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            focusedLabelColor = NubaCyan,
            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
            focusedLeadingIconColor = NubaCyan,
            unfocusedLeadingIconColor = Color.White.copy(alpha = 0.5f),
            errorBorderColor = Color.Red.copy(alpha = 0.8f),
            errorLabelColor = Color.Red.copy(alpha = 0.8f),
            errorLeadingIconColor = Color.Red.copy(alpha = 0.8f),
            errorTrailingIconColor = Color.Red.copy(alpha = 0.8f)
        )
    )
}
