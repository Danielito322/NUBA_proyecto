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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.daniel.nuba.model.AuthUser
import com.daniel.nuba.auth.BiometricAuth
import com.daniel.nuba.auth.FirebaseAuthRepository
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(appState: AppState, onEnter: (AppRoute) -> Unit) {
    val context = LocalContext.current
    val activity = context.findFragmentActivity()
    val scope = rememberCoroutineScope()
    val authRepository = remember { FirebaseAuthRepository() }
    val initialRole = remember {
        BiometricAuth.lastRole(context)?.takeIf { BiometricAuth.isEnabled(context, it) } ?: Role.CLIENTE
    }

    var selectedRole by remember { mutableStateOf(initialRole) }
    var email by remember { mutableStateOf(BiometricAuth.savedEmail(context, initialRole)) }
    var password by remember { mutableStateOf(BiometricAuth.defaultPassword(initialRole)) }
    var biometricEnabled by remember { mutableStateOf(BiometricAuth.isEnabled(context, initialRole)) }
    var autoPromptDone by remember { mutableStateOf(false) }
    var pendingEnableAfterPassword by remember { mutableStateOf<AuthUser?>(null) }
    var authBusy by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var registerName by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    val biometricAvailable = remember { BiometricAuth.canAuthenticate(context) }
    val biometricStatus = remember { BiometricAuth.statusMessage(context) }

    fun routeFor(role: Role): AppRoute = when (role) {
        Role.CLIENTE -> AppRoute.Home
        Role.PROVEEDOR -> AppRoute.Provider
        Role.ADMIN -> AppRoute.Admin
    }

    fun enterWithRole(
        role: Role,
        fromBiometric: Boolean = false,
        resolvedEmail: String = if (fromBiometric) BiometricAuth.savedEmail(context, role) else email.ifBlank { BiometricAuth.defaultEmail(role) },
        resolvedName: String = if (fromBiometric) BiometricAuth.savedName(context, role) else BiometricAuth.defaultName(role)
    ) {
        appState.role = role
        appState.userEmail = resolvedEmail
        appState.userName = resolvedName.ifBlank { BiometricAuth.defaultName(role) }
        BiometricAuth.rememberLastRole(context, role)
        appState.toast = if (fromBiometric) "Huella validada: ingreso como ${role.title}" else "Ingreso como ${role.title}"
        onEnter(routeFor(role))
    }

    fun authenticateWithFingerprint(role: Role, automatic: Boolean = false) {
        if (!BiometricAuth.isEnabled(context, role)) {
            if (!automatic) appState.toast = "Primero activa la huella para ${role.title}"
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
            subtitle = "${role.title}: ${BiometricAuth.savedEmail(context, role)}",
            description = "Confirma tu identidad con la huella registrada en este teléfono.",
            onSuccess = { enterWithRole(role, fromBiometric = true) },
            onError = { message -> if (!automatic || message != "Cancelado") appState.toast = message }
        )
    }

    fun promptFingerprintLink(user: AuthUser, afterSuccess: Boolean = false) {
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
                if (afterSuccess) enterWithRole(user.role, resolvedEmail = user.email, resolvedName = user.displayName)
            },
            onError = { appState.toast = it }
        )
    }

    fun enableFingerprintAndEnter(role: Role, afterSuccess: Boolean = false) {
        if (!biometricAvailable) {
            appState.toast = biometricStatus
            return
        }
        if (authBusy) return
        authBusy = true
        scope.launch {
            val result = authRepository.signIn(email, password, role)
            authBusy = false
            result
                .onSuccess { user -> promptFingerprintLink(user, afterSuccess = afterSuccess) }
                .onFailure { appState.toast = it.message ?: "No se pudo validar la cuenta" }
        }
    }

    fun validatePasswordLogin() {
        if (authBusy) return
        authBusy = true
        scope.launch {
            val result = authRepository.signIn(email, password, selectedRole)
            authBusy = false
            result.onSuccess { user ->
                if (!BiometricAuth.isEnabled(context, user.role) && biometricAvailable) {
                    pendingEnableAfterPassword = user
                } else {
                    enterWithRole(user.role, resolvedEmail = user.email, resolvedName = user.displayName)
                }
            }.onFailure {
                appState.toast = it.message ?: "Correo o contraseña incorrectos para ${selectedRole.title}"
            }
        }
    }

    fun createFirebaseAccount() {
        if (authBusy) return
        authBusy = true
        scope.launch {
            val result = authRepository.register(registerEmail, registerPassword, selectedRole, registerName)
            authBusy = false
            result.onSuccess { user ->
                showRegisterDialog = false
                email = user.email
                password = registerPassword
                pendingEnableAfterPassword = user
                appState.toast = "Cuenta creada en Firebase para ${user.role.title}"
            }.onFailure {
                appState.toast = it.message ?: "No se pudo crear la cuenta"
            }
        }
    }

    LaunchedEffect(selectedRole) {
        email = BiometricAuth.savedEmail(context, selectedRole)
        password = BiometricAuth.defaultPassword(selectedRole)
        biometricEnabled = BiometricAuth.isEnabled(context, selectedRole)
    }

    LaunchedEffect(Unit) {
        if (!autoPromptDone && biometricEnabled && biometricAvailable) {
            autoPromptDone = true
            delay(460)
            authenticateWithFingerprint(selectedRole, automatic = true)
        }
    }

    pendingEnableAfterPassword?.let { user ->
        AlertDialog(
            onDismissRequest = {
                pendingEnableAfterPassword = null
                enterWithRole(user.role, resolvedEmail = user.email, resolvedName = user.displayName)
            },
            title = { Text("Entrar más rápido la próxima vez") },
            text = {
                Text(
                    "Tu cuenta fue validada. Puedes vincular la huella de este teléfono para abrir NUBA con un toque, o continuar solo con contraseña.",
                    fontSize = 13.sp
                )
            },
            confirmButton = { TextButton(onClick = { promptFingerprintLink(user, afterSuccess = true) }) { Text("Activar huella") } },
            dismissButton = { TextButton(onClick = {
                pendingEnableAfterPassword = null
                enterWithRole(user.role, resolvedEmail = user.email, resolvedName = user.displayName)
            }) { Text("Ahora no") } }
        )
    }

    if (showRegisterDialog) {
        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = { Text("Crear cuenta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Se creará una cuenta en Firebase Auth para el rol seleccionado: ${selectedRole.title}.", fontSize = 12.sp)
                    LoginField("Nombre", registerName, { registerName = it }, Icons.Outlined.Person, KeyboardType.Text)
                    LoginField("Correo", registerEmail, { registerEmail = it }, Icons.Outlined.Email, KeyboardType.Email)
                    LoginField("Contraseña", registerPassword, { registerPassword = it }, Icons.Outlined.Lock, KeyboardType.Password, true)
                }
            },
            confirmButton = { TextButton(onClick = { createFirebaseAccount() }, enabled = !authBusy) { Text(if (authBusy) "Creando..." else "Crear") } },
            dismissButton = { TextButton(onClick = { showRegisterDialog = false }) { Text("Cancelar") } }
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
                        val active = role == selectedRole
                        val roleBiometric = BiometricAuth.isEnabled(context, role)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(98.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (active) NubaViolet.copy(.34f) else Color.White.copy(.06f))
                                .border(1.dp, if (active) NubaViolet else Color.White.copy(.12f), RoundedCornerShape(20.dp))
                                .clickable { selectedRole = role }
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
                LoginField("Correo", email, { email = it }, Icons.Outlined.Email, KeyboardType.Email)
                Spacer(Modifier.height(12.dp))
                LoginField("Contraseña", password, { password = it }, Icons.Outlined.Lock, KeyboardType.Password, true)
                Spacer(Modifier.height(10.dp))

                DemoCredentialHint(selectedRole)

                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    PrimaryButton(if (authBusy) "Validando..." else "Acceder", modifier = Modifier.weight(1f), enabled = !authBusy, icon = Icons.Outlined.Login) { validatePasswordLogin() }
                    IconButton(
                        onClick = { authenticateWithFingerprint(selectedRole) },
                        enabled = biometricEnabled && biometricAvailable,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(if (biometricEnabled) NubaCyan.copy(.20f) else Color.White.copy(.06f))
                            .border(1.dp, if (biometricEnabled) NubaCyan.copy(.55f) else Color.White.copy(.14f), RoundedCornerShape(19.dp))
                    ) {
                        Icon(Icons.Outlined.Fingerprint, null, tint = if (biometricEnabled) NubaCyan else Color.White.copy(.38f), modifier = Modifier.size(27.dp))
                    }
                }

                Spacer(Modifier.height(10.dp))
                BiometricCompactHint(
                    enabled = biometricEnabled,
                    available = biometricAvailable,
                    status = biometricStatus,
                    selectedRole = selectedRole,
                    onRegister = { enableFingerprintAndEnter(selectedRole, afterSuccess = false) }
                )

                Spacer(Modifier.height(10.dp))
                SecondaryButton("Crear cuenta con Firebase") { showRegisterDialog = true }
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
