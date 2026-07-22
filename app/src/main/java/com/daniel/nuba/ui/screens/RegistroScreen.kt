package com.daniel.nuba.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import com.daniel.nuba.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.AppRoute
import com.daniel.nuba.ui.components.GlassCard
import com.daniel.nuba.ui.components.PrimaryButton
import com.daniel.nuba.ui.theme.NubaCyan
import com.daniel.nuba.ui.theme.NubaMuted
import com.daniel.nuba.ui.theme.NubaText
import com.daniel.nuba.ui.theme.NubaViolet
import com.daniel.nuba.ui.viewmodels.LoginViewModel

@Composable
fun RegistroScreen(appState: AppState, onEnter: (AppRoute) -> Unit, viewModel: LoginViewModel = viewModel()) {
    Box(Modifier.fillMaxSize()) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1534531173927-aeb928d54385?auto=format&fit=crop&w=1400&q=80",
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onEnter(AppRoute.Login) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(.1f))
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver", tint = Color.White)
                }
                Spacer(Modifier.width(16.dp))
                Text("Crear Cuenta", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(30.dp))

            GlassCard(radius = 30) {
                Text(
                    "Regístrate en NUBA",
                    color = NubaText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Completa tus datos para empezar a reservar.",
                    color = NubaMuted,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(24.dp))

                RegistroField("Nombre completo", viewModel.registerName, { viewModel.registerName = it }, Icons.Outlined.Person, KeyboardType.Text)
                Spacer(Modifier.height(12.dp))
                RegistroField("Correo electrónico", viewModel.registerEmail, { viewModel.registerEmail = it }, Icons.Outlined.Email, KeyboardType.Email)
                Spacer(Modifier.height(12.dp))
                RegistroField("Contraseña", viewModel.registerPassword, { viewModel.registerPassword = it }, Icons.Outlined.Lock, KeyboardType.Password, true)
                
                Spacer(Modifier.height(20.dp))
                
                Text(
                    "Registrarme como:",
                    color = Color.White.copy(.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    com.daniel.nuba.model.Role.entries.forEach { role ->
                        val active = role == viewModel.selectedRole
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (active) NubaViolet.copy(.4f) else Color.White.copy(.05f))
                                .border(1.dp, if (active) NubaViolet else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectedRole = role }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(role.title, color = if (active) Color.White else NubaMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                PrimaryButton(
                    if (viewModel.authBusy) "Creando cuenta..." else "Registrarse a Nuba",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.authBusy,
                    icon = Icons.Outlined.AppRegistration
                ) {
                    viewModel.createFirebaseAccount(appState) {
                        onEnter(AppRoute.Login)
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(.1f))
                    Text("  o vincula con  ", color = NubaMuted, fontSize = 12.sp)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(.1f))
                }
                
                Spacer(Modifier.height(10.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { appState.toast = "Próximamente: Registro con Google" },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(.2f))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Google", fontSize = 13.sp)
                    }
                    
                    OutlinedButton(
                        onClick = { appState.toast = "Próximamente: Registro con Facebook" },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(.2f))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_facebook),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Facebook", fontSize = 13.sp)
                    }
                }
            }
            
            Spacer(Modifier.height(30.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿Ya tienes cuenta? ", color = NubaMuted, fontSize = 14.sp)
                Text(
                    "Inicia sesión",
                    color = NubaCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onEnter(AppRoute.Login) }
                )
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun RegistroField(
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
