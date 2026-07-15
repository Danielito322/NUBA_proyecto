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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.VerifiedUser
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
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(appState: AppState, onEnter: (AppRoute) -> Unit) {
    val context = LocalContext.current
    val activity = context.findFragmentActivity()
    val initialRole = remember {
        BiometricAuth.lastRole(context)?.takeIf { BiometricAuth.isEnabled(context, it) } ?: Role.CLIENTE
    }

    var selectedRole by remember { mutableStateOf(initialRole) }
    var email by remember { mutableStateOf(BiometricAuth.savedEmail(context, initialRole)) }
    var password by remember { mutableStateOf(BiometricAuth.defaultPassword(initialRole)) }
    var biometricEnabled by remember { mutableStateOf(BiometricAuth.isEnabled(context, initialRole)) }
    var autoPromptDone by remember { mutableStateOf(false) }
    var showDisableDialog by remember { mutableStateOf(false) }
    var disablePassword by remember { mutableStateOf("") }
    val biometricAvailable = remember { BiometricAuth.canAuthenticate(context) }
    val biometricStatus = remember { BiometricAuth.statusMessage(context) }

    fun routeFor(role: Role): AppRoute = when (role) {
        Role.CLIENTE -> AppRoute.Home
        Role.PROVEEDOR -> AppRoute.Provider
        Role.ADMIN -> AppRoute.Admin
    }

    fun enterWithRole(role: Role, fromBiometric: Boolean = false) {
        appState.role = role
        appState.userEmail = if (fromBiometric) BiometricAuth.savedEmail(context, role) else email.ifBlank { BiometricAuth.defaultEmail(role) }
        appState.userName = BiometricAuth.defaultName(role)
        BiometricAuth.rememberLastRole(context, role)
        appState.toast = if (fromBiometric) "Huella validada: ingreso como ${role.title}" else "Ingreso como ${role.title}"
        onEnter(routeFor(role))
    }

    fun validatePasswordLogin() {
        if (BiometricAuth.credentialIsValid(selectedRole, email, password)) {
            enterWithRole(selectedRole)
        } else {
            appState.toast = "Correo o contraseña incorrectos para ${selectedRole.title}"
        }
    }

    fun authenticateWithFingerprint(role: Role, automatic: Boolean = false) {
        if (!BiometricAuth.isEnabled(context, role)) {
            if (!automatic) appState.toast = "Activa primero la huella para ${role.title}"
            return
        }
        if (!biometricAvailable) {
            appState.toast = biometricStatus
            return
        }
        if (activity == null) {
            appState.toast = "No se pudo abrir el lector de huella"
            return
        }
        BiometricAuth.authenticate(
            activity = activity,
            title = "Confirmar identidad",
            subtitle = "${role.title}: ${BiometricAuth.savedEmail(context, role)}",
            description = "Valida tu huella para entrar rápido a NUBA. También puedes cancelar y usar contraseña.",
            onSuccess = { enterWithRole(role, fromBiometric = true) },
            onError = { message -> if (!automatic || message != "Cancelado") appState.toast = message }
        )
    }

    fun enableFingerprintForSelectedRole() {
        if (!biometricAvailable) {
            appState.toast = biometricStatus
            return
        }
        if (!BiometricAuth.credentialIsValid(selectedRole, email, password)) {
            appState.toast = "Primero confirma la contraseña de ${selectedRole.title} para activar huella"
            return
        }
        if (activity == null) {
            appState.toast = "No se pudo abrir el lector de huella"
            return
        }
        BiometricAuth.authenticate(
            activity = activity,
            title = "Activar huella",
            subtitle = "${selectedRole.title}: ${email.ifBlank { BiometricAuth.defaultEmail(selectedRole) }}",
            description = "Por seguridad, NUBA vinculará esta cuenta al bloqueo biométrico ya registrado en tu teléfono.",
            onSuccess = {
                BiometricAuth.saveRole(context, selectedRole, email, BiometricAuth.defaultName(selectedRole))
                biometricEnabled = true
                appState.toast = "Huella activada para ${selectedRole.title}"
            },
            onError = { appState.toast = it }
        )
    }

    fun confirmDisableFingerprint() {
        if (BiometricAuth.credentialIsValid(selectedRole, email, disablePassword)) {
            BiometricAuth.disableRole(context, selectedRole)
            biometricEnabled = false
            showDisableDialog = false
            disablePassword = ""
            appState.toast = "Huella desactivada para ${selectedRole.title}"
        } else {
            appState.toast = "Contraseña incorrecta. No se desactivó la huella."
        }
    }

    LaunchedEffect(selectedRole) {
        email = BiometricAuth.savedEmail(context, selectedRole)
        password = BiometricAuth.defaultPassword(selectedRole)
        biometricEnabled = BiometricAuth.isEnabled(context, selectedRole)
        disablePassword = ""
    }

    LaunchedEffect(Unit) {
        if (!autoPromptDone && biometricEnabled && biometricAvailable) {
            autoPromptDone = true
            delay(520)
            authenticateWithFingerprint(selectedRole, automatic = true)
        }
    }

    if (showDisableDialog) {
        AlertDialog(
            onDismissRequest = { showDisableDialog = false },
            title = { Text("Desactivar huella") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Para evitar que alguien quite la seguridad desde tu celular desbloqueado, confirma la contraseña de ${selectedRole.title}.",
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = disablePassword,
                        onValueChange = { disablePassword = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = { TextButton(onClick = { confirmDisableFingerprint() }) { Text("Confirmar") } },
            dismissButton = { TextButton(onClick = { showDisableDialog = false }) { Text("Cancelar") } }
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
                        listOf(Color(0x66060B16), Color(0xCC08111F), Color(0xF2050A13))
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
                Text("Ingreso seguro", color = NubaText, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(
                    "Usa correo y contraseña o entra rápido con huella. Disponible para Cliente, Proveedor y Administrador.",
                    color = NubaMuted,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Role.entries.forEach { role ->
                        val active = role == selectedRole
                        val roleBiometric = BiometricAuth.isEnabled(context, role)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(96.dp)
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
                            Text(
                                if (roleBiometric) "Huella activa" else "Contraseña",
                                color = if (roleBiometric) NubaCyan else Color.White.copy(.52f),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
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
                PrimaryButton("Acceder con contraseña", icon = Icons.Outlined.Login) { validatePasswordLogin() }

                Spacer(Modifier.height(12.dp))
                BiometricPanel(
                    enabled = biometricEnabled,
                    available = biometricAvailable,
                    status = biometricStatus,
                    selectedRole = selectedRole,
                    onLogin = { authenticateWithFingerprint(selectedRole) },
                    onRegister = { enableFingerprintForSelectedRole() },
                    onDisable = { showDisableDialog = true }
                )

                Spacer(Modifier.height(10.dp))
                SecondaryButton("Crear cuenta") { appState.toast = "Registro simulado: el usuario podrá elegir rol y activar huella después" }
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
private fun BiometricPanel(
    enabled: Boolean,
    available: Boolean,
    status: String,
    selectedRole: Role,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onDisable: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(.10f), NubaViolet.copy(.13f), NubaCyan.copy(.08f))
                )
            )
            .border(1.dp, Color.White.copy(.16f), RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(if (enabled) NubaCyan.copy(.18f) else Color.White.copy(.08f))
                    .border(1.dp, if (enabled) NubaCyan.copy(.55f) else Color.White.copy(.14f), RoundedCornerShape(17.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Fingerprint, null, tint = if (available) NubaCyan else NubaMuted, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Acceso biométrico", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    if (enabled) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Outlined.VerifiedUser, null, tint = NubaCyan, modifier = Modifier.size(16.dp))
                    }
                }
                Text(
                    if (enabled) "Listo para ${selectedRole.title}. Al abrir NUBA se pedirá huella automáticamente." else status,
                    color = NubaMuted,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onLogin,
                enabled = enabled && available,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NubaViolet, disabledContainerColor = Color.White.copy(.10f))
            ) {
                Icon(Icons.Outlined.Fingerprint, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Entrar", fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            Button(
                onClick = if (enabled) onDisable else onRegister,
                enabled = available,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (enabled) Color.White.copy(.13f) else NubaCyan.copy(.88f),
                    disabledContainerColor = Color.White.copy(.10f)
                )
            ) {
                Icon(if (enabled) Icons.Outlined.Lock else Icons.Outlined.Fingerprint, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (enabled) "Desactivar" else "Activar", fontSize = 11.5.sp, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Para activar se pide contraseña y huella. Para desactivar se vuelve a pedir contraseña. Así no se quita la seguridad con un toque accidental.",
            color = Color.White.copy(.58f),
            fontSize = 10.sp,
            lineHeight = 13.sp
        )
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
