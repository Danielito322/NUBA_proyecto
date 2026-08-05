package com.daniel.nuba.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.daniel.nuba.R
import com.daniel.nuba.auth.BiometricAuth
import com.daniel.nuba.auth.findFragmentActivity
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.AppRoute
import com.daniel.nuba.model.Role
import com.daniel.nuba.ui.components.GlassCard
import com.daniel.nuba.ui.components.PrimaryButton
import com.daniel.nuba.ui.components.SecondaryButton
import com.daniel.nuba.ui.theme.NubaCyan
import com.daniel.nuba.ui.theme.NubaMuted
import com.daniel.nuba.ui.theme.NubaText
import com.daniel.nuba.ui.theme.NubaViolet
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daniel.nuba.ui.viewmodels.LoginViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daniel.nuba.ui.viewmodels.LoginUiEvent
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

@Composable
fun LoginScreen(appState: AppState, onEnter: (AppRoute) -> Unit, viewModel: LoginViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = context.findFragmentActivity()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.init(context)
        viewModel.events.collect { event ->
            when (event) {
                is LoginUiEvent.Navigate -> {
                    appState.role = uiState.selectedRole
                    appState.userEmail = uiState.email
                    // Intentamos obtener el nombre guardado, si no está usamos el del login si el repositorio lo devolviera
                    // Pero por ahora confiamos en lo que hay o lo que el VM disparó
                    appState.userName = BiometricAuth.savedName(context, uiState.selectedRole).ifBlank { "Usuario" }
                    onEnter(event.route)
                }
                is LoginUiEvent.ShowToast -> {
                    appState.toast = event.message
                }
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
                is LoginUiEvent.LaunchGoogleLogin -> {
                    // Handled in the button onClick for simplicity with coroutines
                }
                is LoginUiEvent.LaunchFacebookLogin -> {
                    activity?.let { act ->
                        LoginManager.getInstance().registerCallback(viewModel.facebookCallbackManager, object : FacebookCallback<LoginResult> {
                            override fun onSuccess(result: LoginResult) {
                                viewModel.onFacebookLoginResult(result.accessToken.token)
                            }
                            override fun onCancel() {
                                viewModel.onAuthCancel("Facebook")
                            }
                            override fun onError(error: FacebookException) {
                                viewModel.onAuthError("Facebook", error.message ?: "Unknown")
                            }
                        })
                        LoginManager.getInstance().logInWithReadPermissions(act, listOf("email", "public_profile"))
                    }
                }
            }
        }
    }

    // Google Login Coroutine Launch (Handle separate from events flow if needed)
    // For brevity and compliance with "View only draws and sends events", 
    // we should ideally use ActivityResultLaunchers. 
    // But since the current code used CredentialManager directly in VM, 
    // we'll move that specific call to a safe place in View.

    LaunchedEffect(uiState.selectedRole) {
        viewModel.checkAutoPrompt()
    }

    if (uiState.pendingEnableAfterPassword != null) {
        val user = uiState.pendingEnableAfterPassword!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissPendingBiometric() },
            title = { Text("Entrar más rápido la próxima vez") },
            text = {
                Text(
                    "Tu cuenta fue validada. Puedes vincular la huella de este teléfono para abrir NUBA con un toque, o continuar solo con contraseña.",
                    fontSize = 13.sp
                )
            },
            confirmButton = { TextButton(onClick = { viewModel.promptFingerprintLink(user, afterSuccess = true) }) { Text("Activar huella") } },
            dismissButton = { TextButton(onClick = { viewModel.dismissPendingBiometric() }) { Text("Ahora no") } }
        )
    }

    Box(Modifier.fillMaxSize()) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1400&q=80",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x77040A13), Color(0xD10A1122), Color(0xF3030710))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 430.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("NUBA", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text("Reserva tu momento", color = NubaCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(26.dp))

            GlassCard(radius = 30) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Bienvenido", color = NubaText, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Text(
                            "Elige tu tipo de cuenta y accede con contraseña o huella.",
                            color = NubaMuted,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(Brush.linearGradient(listOf(NubaViolet.copy(.65f), NubaCyan.copy(.40f))))
                            .border(1.dp, Color.White.copy(.22f), RoundedCornerShape(19.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Fingerprint, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }
                Spacer(Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Role.entries.forEach { role ->
                        val active = role == uiState.selectedRole
                        val roleBiometric = BiometricAuth.isEnabled(context, role)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(98.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (active) NubaViolet.copy(.34f) else Color.White.copy(.06f))
                                .border(1.dp, if (active) NubaViolet else Color.White.copy(.12f), RoundedCornerShape(20.dp))
                                .clickable { viewModel.onRoleSelected(role) }
                                .padding(9.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(role.icon, null, tint = if (active) Color.White else NubaMuted, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.height(6.dp))
                            Text(role.title, color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (roleBiometric) Icon(Icons.Outlined.Fingerprint, null, tint = NubaCyan, modifier = Modifier.size(11.dp))
                                if (roleBiometric) Spacer(Modifier.width(3.dp))
                                Text(
                                    if (roleBiometric) "Lista" else "Clave",
                                    color = if (roleBiometric) NubaCyan else Color.White.copy(.52f),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                LoginField("Correo", uiState.email, { viewModel.onEmailChange(it) }, Icons.Outlined.Email, KeyboardType.Email)
                Spacer(Modifier.height(12.dp))
                LoginField("Contraseña", uiState.password, { viewModel.onPasswordChange(it) }, Icons.Outlined.Lock, KeyboardType.Password, true)
                Spacer(Modifier.height(10.dp))

                DemoCredentialHint(uiState.selectedRole)

                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    PrimaryButton(if (uiState.authBusy) "Validando..." else "Acceder", modifier = Modifier.weight(1f), enabled = !uiState.authBusy, icon = Icons.AutoMirrored.Outlined.Login) { 
                        viewModel.validatePasswordLogin()
                    }
                    IconButton(
                        onClick = { viewModel.authenticateWithFingerprint() },
                        enabled = uiState.biometricEnabled && uiState.biometricAvailable,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(if (uiState.biometricEnabled) NubaCyan.copy(.20f) else Color.White.copy(.06f))
                            .border(1.dp, if (uiState.biometricEnabled) NubaCyan.copy(.55f) else Color.White.copy(.14f), RoundedCornerShape(19.dp))
                    ) {
                        Icon(Icons.Outlined.Fingerprint, null, tint = if (uiState.biometricEnabled) NubaCyan else Color.White.copy(.38f), modifier = Modifier.size(27.dp))
                    }
                }

                // Social Login
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(.1f))
                    Text("  o entra con  ", color = NubaMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(.1f))
                }
                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { 
                            activity?.let { act ->
                                scope.launch {
                                    try {
                                        val credentialManager = CredentialManager.create(context)
                                        val googleIdOption = GetGoogleIdOption.Builder()
                                            .setFilterByAuthorizedAccounts(false)
                                            .setServerClientId(context.getString(R.string.default_web_client_id))
                                            .build()
                                        val request = GetCredentialRequest.Builder()
                                            .addCredentialOption(googleIdOption)
                                            .build()
                                        val result = credentialManager.getCredential(act, request)
                                        val credential = result.credential
                                        if (credential is GoogleIdTokenCredential) {
                                            viewModel.onGoogleLoginResult(credential.idToken)
                                        }
                                    } catch (e: Exception) {
                                        viewModel.onAuthError("Google", e.message ?: "Unknown")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(.12f))
                    ) {
                        Icon(painterResource(R.drawable.ic_google), null, modifier = Modifier.size(17.dp), tint = Color.Unspecified)
                        Spacer(Modifier.width(8.dp))
                        Text("Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { viewModel.onFacebookLoginClick() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(.12f))
                    ) {
                        Icon(painterResource(R.drawable.ic_facebook), null, modifier = Modifier.size(17.dp), tint = Color.Unspecified)
                        Spacer(Modifier.width(8.dp))
                        Text("Facebook", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(18.dp))
                BiometricCompactHint(
                    enabled = uiState.biometricEnabled,
                    available = uiState.biometricAvailable,
                    status = uiState.biometricStatus,
                    selectedRole = uiState.selectedRole,
                    onRegister = { 
                        // To link, we first validate password. ViewModel handles logic.
                        viewModel.validatePasswordLogin() 
                    }
                )

                Spacer(Modifier.height(10.dp))
                SecondaryButton("Crear cuenta") { onEnter(AppRoute.Register) }
            }
        }
    }
}


@Composable
private fun DemoCredentialHint(role: Role) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(.06f))
            .border(1.dp, Color.White.copy(.10f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Security, null, tint = NubaCyan, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text("Cuenta demo ${role.title}", color = Color.White.copy(.88f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("${BiometricAuth.defaultEmail(role)} · clave: ${BiometricAuth.defaultPassword(role)}", color = NubaMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun BiometricCompactHint(
    enabled: Boolean,
    available: Boolean,
    status: String,
    selectedRole: Role,
    onRegister: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(.055f))
            .border(1.dp, Color.White.copy(.11f), RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (enabled) Icons.Outlined.VerifiedUser else Icons.Outlined.Fingerprint,
            null,
            tint = if (enabled) NubaCyan else NubaMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (enabled) "Huella activa para ${selectedRole.title}" else "Huella opcional",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                if (enabled) "La próxima vez NUBA pedirá huella al abrir." else if (available) "Puedes vincularla luego de confirmar tu contraseña." else status,
                color = NubaMuted,
                fontSize = 10.5.sp,
                lineHeight = 13.sp
            )
        }
        if (!enabled && available) {
            TextButton(onClick = onRegister) { Text("Vincular", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun LoginField(
    label: String,
    value: String,
    onValue: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    password: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = NubaCyan,
            unfocusedBorderColor = Color.White.copy(.28f),
            focusedLabelColor = NubaCyan,
            unfocusedLabelColor = Color.White.copy(.65f),
            focusedLeadingIconColor = NubaCyan,
            unfocusedLeadingIconColor = Color.White.copy(.55f),
            cursorColor = NubaCyan
        )
    )
}
