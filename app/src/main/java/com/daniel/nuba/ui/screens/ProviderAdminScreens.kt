package com.daniel.nuba.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    MobileScaffold(appState, AppRoute.Provider, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Proveedor", "Mi negocio", "Gestiona reservas, local, horarios, productos y reseñas.") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Resumen","Local","Horarios","Productos","Reseñas","QR").forEach { item ->
                        FilterButton(item, item == viewModel.providerTab, Modifier.weight(1f)) { viewModel.providerTab = item }
                    }
                }
            }
            when (viewModel.providerTab) {
                "Resumen" -> item { ProviderSummary(appState, viewModel) }
                "Local" -> item { ProviderLocal(appState, viewModel) }
                "Horarios" -> item { ProviderAvailability(appState, viewModel) }
                "Productos" -> item { ProviderProducts(appState, viewModel) }
                "Reseñas" -> item { ProviderReviews(appState, viewModel) }
                "QR" -> item { ProviderQr(appState, viewModel) }
            }
        }
    }
}

@Composable
private fun ProviderSummary(appState: AppState, viewModel: ProviderAdminViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Ingresos", "S/ 820", Modifier.weight(1f))
            StatCard("Reservas", appState.bookings.size.toString(), Modifier.weight(1f))
            StatCard("Stock", appState.products.size.toString(), Modifier.weight(1f))
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
private fun ProviderProducts(appState: AppState, viewModel: ProviderAdminViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PrimaryButton("Agregar producto", icon = Icons.Outlined.AddPhotoAlternate) { viewModel.showAddProduct = true }
        appState.venueProducts().forEach { product ->
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(product.imageUrl, null, Modifier.size(74.dp).clip(RoundedCornerShape(18.dp)), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(product.name, color = Color.White, fontWeight = FontWeight.Black)
                        Text("S/ ${product.price}.00 · Stock ${product.stock}", color = NubaCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { product.stock++ }, modifier = Modifier.glassIcon()) { Icon(Icons.Outlined.Add, null, tint = Color.White) }
                }
            }
        }
    }
    if (viewModel.showAddProduct) AddProductDialog(viewModel) {
        viewModel.addProduct(appState)
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

@Composable
private fun AddProductDialog(viewModel: ProviderAdminViewModel, onAdd: () -> Unit) {
    AlertDialog(
        onDismissRequest = { viewModel.showAddProduct = false },
        confirmButton = { Button(onClick = onAdd, colors = ButtonDefaults.buttonColors(containerColor = NubaViolet)) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = { viewModel.showAddProduct = false }) { Text("Cancelar") } },
        title = { Text("Nuevo producto", color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextInput("Nombre", viewModel.newProductName) { viewModel.newProductName = it }
                TextInput("Precio", viewModel.newProductPrice) { viewModel.newProductPrice = it }
                TextInput("Stock", viewModel.newProductStock) { viewModel.newProductStock = it }
                TextInput("URL de imagen", viewModel.newProductImage) { viewModel.newProductImage = it }
                TextInput("Descripción", viewModel.newProductDesc) { viewModel.newProductDesc = it }
            }
        },
        containerColor = Color(0xEE142238),
        shape = RoundedCornerShape(28.dp)
    )
}
