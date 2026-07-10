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

@Composable
fun LoginScreen(appState: AppState, onEnter: (AppRoute) -> Unit) {
    val context = LocalContext.current
    val activity = context.findFragmentActivity()
    var email by remember { mutableStateOf(BiometricAuth.savedEmail(context)) }
    var password by remember { mutableStateOf("12345678") }
    var selectedRole by remember { mutableStateOf(Role.CLIENTE) }
    var biometricEnabled by remember { mutableStateOf(BiometricAuth.isEnabled(context)) }
    val biometricAvailable = remember { BiometricAuth.canAuthenticate(context) }
    val biometricStatus = remember { BiometricAuth.statusMessage(context) }

    fun enterWithRole(role: Role) {
        appState.role = role
        appState.userEmail = if (role == Role.CLIENTE) email.ifBlank { BiometricAuth.savedEmail(context) } else when (role) {
            Role.CLIENTE -> "daniel@nuba.app"
            Role.PROVEEDOR -> "proveedor@nuba.app"
            Role.ADMIN -> "admin@nuba.app"
        }
        appState.userName = when (role) {
            Role.CLIENTE -> "Daniel Apaza"
            Role.PROVEEDOR -> "Proveedor NUBA"
            Role.ADMIN -> "Administrador"
        }
        appState.toast = "Ingreso como ${role.title}"
        onEnter(
            when (role) {
                Role.CLIENTE -> AppRoute.Home
                Role.PROVEEDOR -> AppRoute.Provider
                Role.ADMIN -> AppRoute.Admin
            }
        )
    }

    fun authenticateForClientLogin() {
        if (!biometricEnabled) {
            appState.toast = "Primero activa la huella para la cuenta Cliente"
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
            title = "Acceder con huella",
            subtitle = "Cuenta Cliente: ${BiometricAuth.savedEmail(context)}",
            onSuccess = {
                appState.role = Role.CLIENTE
                appState.userEmail = BiometricAuth.savedEmail(context)
                appState.userName = BiometricAuth.savedName(context)
                appState.toast = "Huella validada correctamente"
                onEnter(AppRoute.Home)
            },
            onError = { appState.toast = it }
        )
    }

    fun registerClientFingerprint() {
        if (selectedRole != Role.CLIENTE) {
            appState.toast = "La huella solo se habilita para la cuenta Cliente"
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
            title = "Registrar huella en NUBA",
            subtitle = "Cuenta Cliente: ${email.ifBlank { "daniel@nuba.app" }}",
            onSuccess = {
                BiometricAuth.saveClient(context, email.ifBlank { "daniel@nuba.app" }, "Daniel Apaza")
                biometricEnabled = true
                appState.toast = "Huella activada para Cliente"
            },
            onError = { appState.toast = it }
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
                        listOf(Color(0x77070D18), Color(0xD30B1324), Color(0xF007101E))
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
                Text("Iniciar sesión", color = NubaText, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text("Selecciona el tipo de cuenta. La huella es una opción rápida solo para Cliente.", color = NubaMuted, fontSize = 13.sp)
                Spacer(Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Role.entries.forEach { role ->
                        val active = role == selectedRole
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(88.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (active) NubaViolet.copy(.34f) else Color.White.copy(.06f))
                                .border(1.dp, if (active) NubaViolet else Color.White.copy(.12f), RoundedCornerShape(20.dp))
                                .clickable { selectedRole = role }
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(role.icon, null, tint = if (active) Color.White else NubaMuted, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.height(6.dp))
                            Text(role.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1)
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                LoginField("Correo", email, { email = it }, Icons.Outlined.Email, KeyboardType.Email)
                Spacer(Modifier.height(12.dp))
                LoginField("Contraseña", password, { password = it }, Icons.Outlined.Lock, KeyboardType.Password, true)
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = true, onCheckedChange = {}, colors = CheckboxDefaults.colors(checkedColor = NubaViolet))
                    Text("Recordar cuenta", color = Color.White.copy(.8f), fontSize = 12.sp)
                    Spacer(Modifier.weight(1f))
                    Text("Recuperar", color = NubaCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(14.dp))
                PrimaryButton("Acceder", icon = Icons.Outlined.Login) {
                    enterWithRole(selectedRole)
                }

                Spacer(Modifier.height(12.dp))
                BiometricPanel(
                    enabled = biometricEnabled,
                    available = biometricAvailable,
                    status = biometricStatus,
                    selectedRole = selectedRole,
                    onLogin = { authenticateForClientLogin() },
                    onRegister = { registerClientFingerprint() },
                    onDisable = {
                        BiometricAuth.disable(context)
                        biometricEnabled = false
                        appState.toast = "Huella desactivada para Cliente"
                    }
                )

                Spacer(Modifier.height(10.dp))
                SecondaryButton("Crear cuenta") { appState.toast = "Registro simulado habilitado" }
            }
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
            .background(Color.White.copy(.07f))
            .border(1.dp, Color.White.copy(.14f), RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (enabled) NubaViolet.copy(.35f) else Color.White.copy(.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Fingerprint, null, tint = if (available) NubaCyan else NubaMuted, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Acceso con huella", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Text(
                    if (selectedRole == Role.CLIENTE) {
                        if (enabled) "Activado para Cliente" else status
                    } else {
                        "Disponible solo para Cliente"
                    },
                    color = NubaMuted,
                    fontSize = 11.sp
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onLogin,
                enabled = enabled && selectedRole == Role.CLIENTE && available,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NubaViolet, disabledContainerColor = Color.White.copy(.10f))
            ) {
                Icon(Icons.Outlined.Fingerprint, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Entrar", fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            Button(
                onClick = if (enabled) onDisable else onRegister,
                enabled = selectedRole == Role.CLIENTE && available,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (enabled) Color.White.copy(.12f) else NubaCyan.copy(.85f), disabledContainerColor = Color.White.copy(.10f))
            ) {
                Icon(if (enabled) Icons.Outlined.Lock else Icons.Outlined.Fingerprint, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (enabled) "Quitar" else "Activar", fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "La huella no reemplaza correo y contraseña; solo agiliza el ingreso del usuario cliente.",
            color = Color.White.copy(.55f),
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
