package com.daniel.nuba.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.AppRoute
import com.daniel.nuba.model.Category
import com.daniel.nuba.ui.components.*
import com.daniel.nuba.ui.theme.*

@Composable
fun ProviderScreen(appState: AppState, onNavigate: (AppRoute) -> Unit) {
    var tab by remember { mutableStateOf("Resumen") }
    MobileScaffold(appState, AppRoute.Provider, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Proveedor", "Mi negocio", "Gestiona reservas, local, horarios, productos y reseñas.") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Resumen","Local","Horarios","Productos","Reseñas","QR").forEach { item ->
                        FilterButton(item, item == tab, Modifier.weight(1f)) { tab = item }
                    }
                }
            }
            when (tab) {
                "Resumen" -> item { ProviderSummary(appState) }
                "Local" -> item { ProviderLocal(appState) }
                "Horarios" -> item { ProviderAvailability(appState) }
                "Productos" -> item { ProviderProducts(appState) }
                "Reseñas" -> item { ProviderReviews(appState) }
                "QR" -> item { ProviderQr(appState) }
            }
        }
    }
}

@Composable
private fun ProviderSummary(appState: AppState) {
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
                    OutlinedButton(onClick = { booking.status = "Rechazada" }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) { Text("Rechazar") }
                    Button(onClick = { booking.status = "Aceptada" }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = NubaViolet)) { Text("Aprobar") }
                }
            }
        }
    }
}

@Composable
private fun ProviderLocal(appState: AppState) {
    val venue = appState.selectedVenue()
    var name by remember { mutableStateOf(venue.name) }
    var description by remember { mutableStateOf(venue.description) }
    var image by remember { mutableStateOf(venue.imageUrl) }
    var category by remember { mutableStateOf(venue.category) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ImageHero(image, 180)
        Category.entries.forEach { cat -> FilterButton(cat.label, cat == category, Modifier.fillMaxWidth()) { category = cat } }
        TextInput("Nombre del local", name) { name = it }
        TextInput("Descripción", description) { description = it }
        TextInput("URL de fotografía", image) { image = it }
        PrimaryButton("Guardar cambios", icon = Icons.Outlined.Save) {
            venue.name = name; venue.description = description; venue.imageUrl = image; venue.category = category
            appState.toast = "Local actualizado"
        }
    }
}

@Composable
private fun ProviderAvailability(appState: AppState) {
    val venue = appState.selectedVenue()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Disponibilidad semanal", "Activa días y ajusta horarios")
        venue.schedules.forEach { slot ->
            var active by remember(slot.day) { mutableStateOf(slot.active) }
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = active, onCheckedChange = { active = it; slot.active = it }, colors = SwitchDefaults.colors(checkedThumbColor = NubaGreen))
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
private fun ProviderProducts(appState: AppState) {
    var showAdd by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PrimaryButton("Agregar producto", icon = Icons.Outlined.AddPhotoAlternate) { showAdd = true }
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
    if (showAdd) AddProductDialog(onDismiss = { showAdd = false }) { name, price, stock, image, desc ->
        appState.addProviderProduct(name, price, stock, image, desc)
        showAdd = false
    }
}

@Composable
private fun ProviderReviews(appState: AppState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Reseñas del local", "Responder o reportar comentarios")
        appState.venueReviews().forEach { review ->
            GlassCard {
                Row { Text("${review.rating}.0", color = NubaAmber, fontWeight = FontWeight.Black); Spacer(Modifier.weight(1f)); Text(review.author, color = NubaMuted, fontSize = 12.sp) }
                Text(review.comment, color = Color.White.copy(.84f), fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { review.status = "Reportada" }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) { Text("Reportar") }
                    Button(onClick = { review.response = "Gracias por tu visita."; }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = NubaViolet)) { Text("Responder") }
                }
            }
        }
    }
}

@Composable
private fun ProviderQr(appState: AppState) {
    var code by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextInput("Código QR o reserva", code) { code = it }
        PrimaryButton("Validar ingreso", icon = Icons.Outlined.QrCodeScanner) {
            val found = appState.bookings.firstOrNull { it.code.equals(code, true) }
            if (found != null && found.status != "Usada") {
                found.status = "Usada"
                appState.toast = "Reserva validada correctamente"
            } else appState.toast = "Código no válido o ya usado"
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
fun AdminScreen(appState: AppState, onNavigate: (AppRoute) -> Unit) {
    var tab by remember { mutableStateOf("Aprobaciones") }
    MobileScaffold(appState, AppRoute.Admin, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Administrador", "Panel general", "Controla negocios, usuarios, reportes y reseñas.") }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Aprobaciones","Negocios","Usuarios","Reseñas").forEach { FilterButton(it, it == tab, Modifier.weight(1f)) { tab = it } } } }
            when(tab){
                "Aprobaciones" -> item { AdminApprovals(appState) }
                "Negocios" -> item { AdminBusinesses(appState) }
                "Usuarios" -> item { AdminUsers() }
                "Reseñas" -> item { AdminReviews(appState) }
            }
        }
    }
}

@Composable
private fun AdminApprovals(appState: AppState) { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { appState.requests.forEach { req -> GlassCard { Text(req.title, color = Color.White, fontWeight = FontWeight.Black); Text("${req.owner} · ${req.category.label} · ${req.status}", color = NubaMuted, fontSize = 12.sp); Spacer(Modifier.height(10.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { req.status = "Rechazado" }, Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) { Text("Rechazar") }; Button(onClick = { req.status = "Aprobado" }, Modifier.weight(1f), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = NubaViolet)) { Text("Aprobar") } } } } } }
@Composable
private fun AdminBusinesses(appState: AppState) { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { appState.venues.forEach { venue -> GlassCard { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(venue.name, color = Color.White, fontWeight = FontWeight.Black); Text("${venue.category.label} · ${venue.status}", color = NubaMuted, fontSize = 12.sp) }; Switch(checked = venue.status == "Activo", onCheckedChange = { venue.status = if (it) "Activo" else "Suspendido" }) } } } } }
@Composable
private fun AdminUsers() { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { listOf("Daniel Apaza - Cliente", "Proveedor NUBA - Proveedor", "Administrador - Admin").forEach { user -> GlassCard { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Person, null, tint = NubaCyan); Spacer(Modifier.width(10.dp)); Text(user, color = Color.White, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text("Activo", color = NubaGreen, fontSize = 12.sp) } } } } }
@Composable
private fun AdminReviews(appState: AppState) { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { appState.reviews.forEach { review -> GlassCard { Text(review.comment, color = Color.White, fontWeight = FontWeight.Bold); Text("${review.author} · Estado: ${review.status}", color = NubaMuted, fontSize = 12.sp); Spacer(Modifier.height(8.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { review.status = "Oculta" }, Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) { Text("Ocultar") }; Button(onClick = { review.status = "Publicada" }, Modifier.weight(1f), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = NubaViolet)) { Text("Publicar") } } } } } }

@Composable
private fun FilterButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) { Text(text, color = if(selected) Color.White else NubaMuted, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = modifier.clip(RoundedCornerShape(16.dp)).background(if(selected) NubaViolet.copy(.42f) else Color.White.copy(.06f)).border(1.dp, if(selected) NubaViolet else Color.White.copy(.11f), RoundedCornerShape(16.dp)).clickable(onClick=onClick).padding(vertical = 11.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
@Composable
private fun TextInput(label: String, value: String, onValue: (String) -> Unit) { OutlinedTextField(value = value, onValueChange = onValue, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NubaCyan, unfocusedBorderColor = Color.White.copy(.15f), focusedLabelColor = NubaCyan, unfocusedLabelColor = NubaMuted, cursorColor = NubaCyan)) }

@Composable
private fun AddProductDialog(onDismiss: () -> Unit, onAdd: (String, Int, Int, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }; var price by remember { mutableStateOf("0") }; var stock by remember { mutableStateOf("0") }; var image by remember { mutableStateOf("") }; var desc by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { Button(onClick = { onAdd(name.ifBlank { "Producto premium" }, price.toIntOrNull() ?: 0, stock.toIntOrNull() ?: 0, image, desc) }, colors = ButtonDefaults.buttonColors(containerColor = NubaViolet)) { Text("Guardar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }, title = { Text("Nuevo producto", color = Color.White, fontWeight = FontWeight.Black) }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { TextInput("Nombre", name) { name = it }; TextInput("Precio", price) { price = it }; TextInput("Stock", stock) { stock = it }; TextInput("URL de imagen", image) { image = it }; TextInput("Descripción", desc) { desc = it } } }, containerColor = Color(0xEE142238), shape = RoundedCornerShape(28.dp))
}
