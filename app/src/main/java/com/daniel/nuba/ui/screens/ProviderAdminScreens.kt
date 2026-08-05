package com.daniel.nuba.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daniel.nuba.ui.viewmodels.ProviderAdminViewModel
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.AppRoute
import com.daniel.nuba.model.Category
import com.daniel.nuba.ui.components.*
import com.daniel.nuba.ui.theme.*

@Composable
fun ProviderScreen(appState: AppState, onNavigate: (AppRoute) -> Unit, viewModel: ProviderAdminViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        viewModel.loadProviderBusinesses()
    }
    MobileScaffold(appState, AppRoute.Provider, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Proveedor", "Mi negocio", "Gestiona reservas, local, horarios y reseñas.") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("Negocios","Resumen","Local","Horarios","Reseñas","QR").forEach { item ->
                        FilterButton(item, item == viewModel.providerTab, Modifier.width(90.dp)) { viewModel.providerTab = item }
                    }
                }
            }
            when (viewModel.providerTab) {
                "Negocios" -> item { ProviderBusinesses(appState, viewModel) }
                "Resumen" -> item { ProviderSummary(appState, viewModel) }
                "Local" -> item { ProviderLocal(appState, viewModel) }
                "Horarios" -> item { ProviderAvailability(appState, viewModel) }
                "Reseñas" -> item { ProviderReviews(appState, viewModel) }
                "QR" -> item { ProviderQr(appState, viewModel) }
            }
        }
    }
    if (viewModel.showBusinessDialog) BusinessDialog(viewModel, appState) { viewModel.saveBusiness(appState) }
}

@Composable
private fun ProviderBusinesses(appState: AppState, viewModel: ProviderAdminViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PrimaryButton("Nuevo negocio", icon = Icons.Outlined.Add) { viewModel.openCreateBusiness() }
        
        if (viewModel.loadingBusinesses) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NubaCyan)
            }
        } else if (viewModel.businesses.isEmpty()) {
            EmptyState("Sin negocios", "Aún no has registrado ningún local.", Icons.Outlined.Business)
        } else {
            viewModel.businesses.forEach { business ->
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(business.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text(business.address ?: "Sin dirección", color = NubaMuted, fontSize = 12.sp)
                            Text(business.category.label, color = NubaCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { viewModel.openEditBusiness(business) }) {
                            Icon(Icons.Outlined.Edit, null, tint = Color.White.copy(0.7f))
                        }
                        IconButton(onClick = { viewModel.deleteBusiness(business.id!!, appState) }) {
                            Icon(Icons.Outlined.Delete, null, tint = Color.Red.copy(0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BusinessDialog(viewModel: ProviderAdminViewModel, appState: AppState, onSave: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.readBytes()
            bytes?.let { b -> viewModel.uploadBusinessImage(b, appState) }
        }
    }

    AlertDialog(
        onDismissRequest = { viewModel.showBusinessDialog = false },
        title = { Text(if (viewModel.editingBusiness == null) "Nuevo Negocio" else "Editar Negocio", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                TextInput("Título", viewModel.busTitle) { viewModel.busTitle = it }
                TextInput("Descripción", viewModel.busDescription) { viewModel.busDescription = it }
                SectionTitle("Categoría", "")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Category.entries.forEach { cat ->
                        FilterButton(cat.label, cat == viewModel.busCategory, Modifier.weight(1f)) { viewModel.busCategory = cat }
                    }
                }
                TextInput("Dirección", viewModel.busAddress) { viewModel.busAddress = it }
                TextInput("Teléfono", viewModel.busPhone) { viewModel.busPhone = it }
                TextInput("Email", viewModel.busEmail) { viewModel.busEmail = it }
                
                Spacer(Modifier.height(8.dp))
                SectionTitle("Foto de portada", "")
                if (viewModel.busImage.isNotBlank()) {
                    AsyncImage(
                        model = viewModel.busImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Button(
                    onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.1f)),
                    enabled = !viewModel.uploadingImage
                ) {
                    if (viewModel.uploadingImage) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = NubaCyan, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Outlined.PhotoCamera, null, tint = NubaCyan)
                        Spacer(Modifier.width(8.dp))
                        Text(if (viewModel.busImage.isBlank()) "Elegir foto" else "Cambiar foto", color = Color.White)
                    }
                }
                
                TextInput("O URL personalizada", viewModel.busImage) { viewModel.busImage = it }
            }
        },
        confirmButton = {
            Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = NubaViolet), enabled = !viewModel.uploadingImage) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.showBusinessDialog = false }) {
                Text("Cancelar")
            }
        },
        containerColor = Color(0xFF0A1122),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun ProviderSummary(appState: AppState, viewModel: ProviderAdminViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Ingresos", "S/ 820", Modifier.weight(1f))
            StatCard("Reservas", appState.bookings.size.toString(), Modifier.weight(1f))
        }
        SectionTitle("Solicitudes recientes", "Aprobar o rechazar reservas")
        appState.bookings.take(3).forEach { booking ->
            GlassCard {
                Text(booking.venueName, color = Color.White, fontWeight = FontWeight.Black)
                Text("${booking.dateLabel} · ${booking.time} · ${booking.status}", color = NubaMuted, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.updateBookingStatus(booking, "Rechazada") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) { Text("Rechazar") }
                    Button(onClick = { viewModel.updateBookingStatus(booking, "Aceptada") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = NubaViolet)) { Text("Aprobar") }
                }
            }
        }
    }
}

@Composable
private fun ProviderLocal(appState: AppState, viewModel: ProviderAdminViewModel) {
    val venue = appState.selectedVenue()
    
    LaunchedEffect(venue) {
        viewModel.loadLocalData(venue)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ImageHero(viewModel.localImage, 180)
        Category.entries.forEach { cat -> FilterButton(cat.label, cat == viewModel.localCategory, Modifier.fillMaxWidth()) { viewModel.localCategory = cat } }
        TextInput("Nombre del local", viewModel.localName) { viewModel.localName = it }
        TextInput("Descripción", viewModel.localDescription) { viewModel.localDescription = it }
        TextInput("URL de fotografía", viewModel.localImage) { viewModel.localImage = it }
        PrimaryButton("Guardar cambios", icon = Icons.Outlined.Save) {
            viewModel.saveLocalChanges(appState)
        }
    }
}

@Composable
private fun ProviderAvailability(appState: AppState, viewModel: ProviderAdminViewModel) {
    val venue = appState.selectedVenue()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Disponibilidad semanal", "Activa días y ajusta horarios")
        venue.schedules.forEach { slot ->
            var active by remember(slot.day) { mutableStateOf(slot.active) }
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = active, onCheckedChange = { 
                        active = it
                        viewModel.toggleSchedule(slot, it)
                    }, colors = SwitchDefaults.colors(checkedThumbColor = NubaGreen))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(slot.day, color = Color.White, fontWeight = FontWeight.Black)
                        Text("${slot.open} - ${slot.close}", color = NubaMuted, fontSize = 12.sp)
                    }
                    Icon(Icons.Outlined.Schedule, null, tint = NubaCyan)
                }
            }
        }
    }
}

@Composable
private fun ProviderReviews(appState: AppState, viewModel: ProviderAdminViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Reseñas del local", "Responder o reportar comentarios")
        appState.venueReviews().forEach { review ->
            GlassCard {
                Row { Text("${review.rating}.0", color = NubaAmber, fontWeight = FontWeight.Black); Spacer(Modifier.weight(1f)); Text(review.author, color = NubaMuted, fontSize = 12.sp) }
                Text(review.comment, color = Color.White.copy(.84f), fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.updateReviewStatus(review, "Reportada") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) { Text("Reportar") }
                    Button(onClick = { viewModel.respondToReview(review, "Gracias por tu visita.") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = NubaViolet)) { Text("Responder") }
                }
            }
        }
    }
}

@Composable
private fun ProviderQr(appState: AppState, viewModel: ProviderAdminViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextInput("Código QR o reserva", viewModel.qrCode) { viewModel.qrCode = it }
        PrimaryButton("Validar ingreso", icon = Icons.Outlined.QrCodeScanner) {
            viewModel.validateQr(appState)
        }
        appState.bookings.firstOrNull()?.let { booking ->
            GlassCard {
                Text("Código de prueba", color = NubaMuted)
                Text(booking.code, color = NubaCyan, fontSize = 19.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(10.dp))
                QrCodeVisual(booking.code, Modifier.size(150.dp))
            }
        }
    }
}

@Composable
fun AdminScreen(appState: AppState, onNavigate: (AppRoute) -> Unit, viewModel: ProviderAdminViewModel = viewModel()) {
    MobileScaffold(appState, AppRoute.Admin, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Administrador", "Panel general", "Controla negocios, usuarios, reportes y reseñas.") }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Aprobaciones","Negocios","Usuarios","Reseñas").forEach { FilterButton(it, it == viewModel.adminTab, Modifier.weight(1f)) { viewModel.adminTab = it } } } }
            when(viewModel.adminTab){
                "Aprobaciones" -> item { AdminApprovals(appState, viewModel) }
                "Negocios" -> item { AdminBusinesses(appState, viewModel) }
                "Usuarios" -> item { AdminUsers() }
                "Reseñas" -> item { AdminReviews(appState, viewModel) }
            }
        }
    }
}

@Composable
private fun AdminApprovals(appState: AppState, viewModel: ProviderAdminViewModel) { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { appState.requests.forEach { req -> GlassCard { Text(req.title, color = Color.White, fontWeight = FontWeight.Black); Text("${req.owner} · ${req.category.label} · ${req.status}", color = NubaMuted, fontSize = 12.sp); Spacer(Modifier.height(10.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { viewModel.updateRequestStatus(req, "Rechazado") }, Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) { Text("Rechazar") }; Button(onClick = { viewModel.updateRequestStatus(req, "Aprobado") }, Modifier.weight(1f), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = NubaViolet)) { Text("Aprobar") } } } } } }
@Composable
private fun AdminBusinesses(appState: AppState, viewModel: ProviderAdminViewModel) { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { appState.venues.forEach { venue -> GlassCard { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(venue.name, color = Color.White, fontWeight = FontWeight.Black); Text("${venue.category.label} · ${venue.status}", color = NubaMuted, fontSize = 12.sp) }; Switch(checked = venue.status == "Activo", onCheckedChange = { viewModel.updateVenueStatus(venue, it) }) } } } } }
@Composable
private fun AdminUsers() { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { listOf("Daniel Apaza - Cliente", "Proveedor NUBA - Proveedor", "Administrador - Admin").forEach { user -> GlassCard { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Person, null, tint = NubaCyan); Spacer(Modifier.width(10.dp)); Text(user, color = Color.White, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text("Activo", color = NubaGreen, fontSize = 12.sp) } } } } }
@Composable
private fun AdminReviews(appState: AppState, viewModel: ProviderAdminViewModel) { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { appState.reviews.forEach { review -> GlassCard { Text(review.comment, color = Color.White, fontWeight = FontWeight.Bold); Text("${review.author} · Estado: ${review.status}", color = NubaMuted, fontSize = 12.sp); Spacer(Modifier.height(8.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { viewModel.updateReviewStatus(review, "Oculta") }, Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) { Text("Ocultar") }; Button(onClick = { viewModel.updateReviewStatus(review, "Publicada") }, Modifier.weight(1f), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = NubaViolet)) { Text("Publicar") } } } } } }

@Composable
private fun FilterButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) { Text(text, color = if(selected) Color.White else NubaMuted, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = modifier.clip(RoundedCornerShape(16.dp)).background(if(selected) NubaViolet.copy(.42f) else Color.White.copy(.06f)).border(1.dp, if(selected) NubaViolet else Color.White.copy(.11f), RoundedCornerShape(16.dp)).clickable(onClick=onClick).padding(vertical = 11.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
@Composable
private fun TextInput(label: String, value: String, onValue: (String) -> Unit) { OutlinedTextField(value = value, onValueChange = onValue, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NubaCyan, unfocusedBorderColor = Color.White.copy(.15f), focusedLabelColor = NubaCyan, unfocusedLabelColor = NubaMuted, cursorColor = NubaCyan)) }

