package com.daniel.nuba.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.daniel.nuba.auth.BiometricAuth
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.AppRoute
import com.daniel.nuba.model.Category
import com.daniel.nuba.model.Product
import com.daniel.nuba.model.Venue
import com.daniel.nuba.ui.components.*
import com.daniel.nuba.ui.theme.*

@Composable
fun HomeScreen(appState: AppState, onNavigate: (AppRoute) -> Unit) {
    MobileScaffold(appState, AppRoute.Home, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            item {
                ScreenHeader("Hola, ${appState.userName.split(' ').first()}", "Reserva tu momento", "Deportes, belleza y entretenimiento en Puno.", Icons.Outlined.Person) { onNavigate(AppRoute.Profile) }
            }
            item {
                ImageHero("https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&w=1400&q=80", 210) {
                    Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
                        Text("Confirmación rápida", color = NubaCyan, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text("Elige, paga y entra con QR", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
                        Text("Reservas con calendario mensual, tienda del local y reseñas reales.", color = Color.White.copy(.78f), fontSize = 13.sp)
                    }
                }
            }
            item { SectionTitle("Categorías", "Escoge primero qué deseas reservar") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Category.entries.forEach { category ->
                        CategoryCard(category) {
                            appState.selectedCategory = category
                            onNavigate(AppRoute.Explore)
                        }
                    }
                }
            }
            item { SectionTitle("Recomendados", "Locales activos cerca de ti") }
            items(appState.venues.take(4)) { venue -> VenueCard(venue, appState, onNavigate) }
        }
    }
}

@Composable
fun ExploreScreen(appState: AppState, onNavigate: (AppRoute) -> Unit) {
    MobileScaffold(appState, AppRoute.Explore, onNavigate) {
        var query by remember { mutableStateOf("") }
        val filtered = appState.venues.filter { it.category == appState.selectedCategory && it.name.contains(query, true) }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Explorar", appState.selectedCategory.label, "Filtra locales, revisa fotos, tienda y disponibilidad.") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Category.entries.forEach { category -> CategoryPill(category, category == appState.selectedCategory) { appState.selectedCategory = category } }
                }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    placeholder = { Text("Buscar local o servicio") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = NubaCyan, unfocusedBorderColor = Color.White.copy(.14f),
                        focusedLeadingIconColor = NubaCyan, unfocusedLeadingIconColor = NubaMuted,
                        focusedPlaceholderColor = NubaMuted, unfocusedPlaceholderColor = NubaMuted,
                        cursorColor = NubaCyan
                    )
                )
            }
            if (filtered.isEmpty()) item { EmptyState("Sin resultados", "Prueba con otra categoría o cambia la búsqueda.", Icons.Outlined.SearchOff) }
            items(filtered) { venue -> VenueCard(venue, appState, onNavigate) }
        }
    }
}

@Composable
fun DetailScreen(appState: AppState, onNavigate: (AppRoute) -> Unit) {
    val venue = appState.selectedVenue()
    val products = appState.venueProducts(venue.id)
    val reviews = appState.venueReviews(venue.id)
    MobileScaffold(appState, AppRoute.Detail, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Detalle del local", venue.name, venue.address, back = { onNavigate(AppRoute.Explore) }) }
            item {
                ImageHero(venue.imageUrl, 235) {
                    Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
                        Text(venue.category.label, color = NubaCyan, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Text(venue.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 27.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            MiniBadge(Icons.Outlined.Star, "${venue.rating} (${venue.reviews})")
                            MiniBadge(Icons.Outlined.Place, venue.distance)
                            MiniBadge(Icons.Outlined.Schedule, "Disponible")
                        }
                    }
                }
            }
            item { InfoBlock("Sobre el local", venue.description) }
            item {
                SectionTitle("Incluye", "Servicios disponibles")
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    venue.services.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { service -> ServiceChip(service, Modifier.weight(1f)) }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            item {
                SectionTitle("Tienda del local", if (products.isEmpty()) "Este local no publicó productos" else "Productos del mismo local")
                Spacer(Modifier.height(8.dp))
                if (products.isEmpty()) EmptyState("Sin tienda", "Puedes reservar sin productos adicionales.", Icons.Outlined.Storefront)
                else ProductHorizontal(products.take(3), appState, onNavigate)
            }
            item {
                SectionTitle("Reseñas", "Opiniones antes de reservar")
                reviews.take(2).forEach { review -> ReviewMiniCard(review.comment, review.author, review.rating) }
                SecondaryButton("Ver y calificar", icon = Icons.Outlined.Reviews) { onNavigate(AppRoute.Reviews) }
            }
            item {
                GlassCard(radius = 24) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Desde 60 min", color = NubaMuted, fontSize = 11.sp)
                            Text("S/ ${venue.price}.00", color = Color.White, fontWeight = FontWeight.Black, fontSize = 19.sp)
                        }
                        Button(
                            onClick = { onNavigate(AppRoute.Reserve) },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NubaViolet),
                            modifier = Modifier.height(52.dp)
                        ) { Text("Reservar", fontWeight = FontWeight.Black); Spacer(Modifier.width(5.dp)); Icon(Icons.Outlined.ArrowForward, null) }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ShopScreen(appState: AppState, onNavigate: (AppRoute) -> Unit) {
    val venue = appState.selectedVenue()
    val products = appState.venueProducts(venue.id)
    MobileScaffold(appState, AppRoute.Shop, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Tienda del local", venue.name, "Productos propios del negocio elegido.", Icons.Outlined.ShoppingCart) { onNavigate(AppRoute.Cart) } }
            if (products.isEmpty()) item { EmptyState("Sin productos", "Este negocio no tiene tienda por ahora.", Icons.Outlined.Storefront) }
            items(products) { ProductRow(product = it, appState = appState) }
        }
    }
}

@Composable
fun CartScreen(appState: AppState, onNavigate: (AppRoute) -> Unit) {
    var showPayment by remember { mutableStateOf(false) }
    MobileScaffold(appState, AppRoute.Cart, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Tu compra", "Carrito", "Revisa productos y cantidades.", back = { onNavigate(AppRoute.Shop) }) }
            if (appState.cart.isEmpty()) item { EmptyState("Carrito vacío", "Agrega productos desde la tienda del local.", Icons.Outlined.ShoppingCart) }
            items(appState.cart) { item ->
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(item.product.imageUrl, null, modifier = Modifier.size(72.dp).clip(RoundedCornerShape(18.dp)), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.product.name, color = Color.White, fontWeight = FontWeight.Black)
                            Text("S/ ${item.product.price}.00", color = NubaCyan, fontWeight = FontWeight.Bold)
                        }
                        Text("x${item.quantity}", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
            item {
                GlassCard {
                    Row { Text("Total", color = NubaMuted); Spacer(Modifier.weight(1f)); Text("S/ ${appState.cartTotal()}.00", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black) }
                }
                Spacer(Modifier.height(12.dp))
                PrimaryButton("Finalizar compra", enabled = appState.cart.isNotEmpty(), icon = Icons.Outlined.CreditCard) { showPayment = true }
            }
        }
    }
    if (showPayment) PaymentSheet(title = "Confirmar compra", amount = appState.cartTotal(), onDismiss = { showPayment = false }) {
        appState.cart.clear()
        appState.toast = "Compra confirmada"
        showPayment = false
    }
}

@Composable
fun ProfileScreen(appState: AppState, onNavigate: (AppRoute) -> Unit) {
    val context = LocalContext.current
    var showSecurityDialog by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var biometricEnabled by remember { mutableStateOf(BiometricAuth.isEnabled(context, appState.role)) }

    if (showSecurityDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityDialog = false; confirmPassword = "" },
            title = { Text("Seguridad biométrica") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "La huella está vinculada a la cuenta ${appState.role.title}. Para olvidar este acceso rápido, confirma la contraseña de la cuenta.",
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (BiometricAuth.credentialIsValid(appState.role, appState.userEmail, confirmPassword)) {
                        BiometricAuth.disableRole(context, appState.role)
                        biometricEnabled = false
                        appState.toast = "Acceso biométrico olvidado para ${appState.role.title}"
                        showSecurityDialog = false
                        confirmPassword = ""
                    } else {
                        appState.toast = "Contraseña incorrecta. No se modificó la seguridad."
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { showSecurityDialog = false; confirmPassword = "" }) { Text("Cancelar") } }
        )
    }

    MobileScaffold(appState, AppRoute.Profile, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Tu cuenta", "Perfil", "Preferencias, historial y soporte.", Icons.Outlined.Logout) { onNavigate(AppRoute.Login) } }
            item {
                GlassCard {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.size(92.dp).clip(CircleShape).background(Brush.linearGradient(listOf(NubaViolet, NubaCyan))), contentAlignment = Alignment.Center) {
                            Text(appState.userName.first().toString(), fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(appState.userName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text(appState.userEmail, color = NubaMuted, fontSize = 13.sp)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Reservas", appState.bookings.size.toString(), Modifier.weight(1f))
                    StatCard("Favoritos", "03", Modifier.weight(1f))
                    StatCard("Reseñas", appState.reviews.count { it.author == appState.userName }.toString(), Modifier.weight(1f))
                }
            }
            item { BiometricSecurityCard(biometricEnabled, appState.role.title) { showSecurityDialog = true } }
            item { MenuLink("Mis reservas", "QR, historial y próximos planes", Icons.Outlined.CalendarMonth) { onNavigate(AppRoute.Bookings) } }
            item { MenuLink("Carrito", "Productos seleccionados", Icons.Outlined.ShoppingCart) { onNavigate(AppRoute.Cart) } }
            item { MenuLink("Cerrar sesión", "Volver a elegir tipo de cuenta", Icons.Outlined.Logout) { onNavigate(AppRoute.Login) } }
        }
    }
}

@Composable
private fun BiometricSecurityCard(enabled: Boolean, roleTitle: String, onManage: () -> Unit) {
    GlassCard(radius = 22) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (enabled) NubaCyan.copy(.16f) else Color.White.copy(.07f))
                    .border(1.dp, if (enabled) NubaCyan.copy(.45f) else Color.White.copy(.13f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Fingerprint, null, tint = if (enabled) NubaCyan else NubaMuted, modifier = Modifier.size(25.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Seguridad de acceso", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Text(
                    if (enabled) "Huella activa para $roleTitle" else "Puedes vincular huella desde el próximo inicio de sesión",
                    color = NubaMuted,
                    fontSize = 12.sp
                )
            }
            if (enabled) {
                TextButton(onClick = onManage) { Text("Gestionar", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun CategoryCard(category: Category, onClick: () -> Unit) {
    GlassCard(modifier = Modifier.width(142.dp).height(150.dp).clickable(onClick = onClick)) {
        Icon(category.icon, null, tint = when (category) { Category.DEPORTES -> NubaCyan; Category.BELLEZA -> NubaPink; Category.ENTRETENIMIENTO -> NubaAmber }, modifier = Modifier.size(34.dp))
        Spacer(Modifier.weight(1f))
        Text(category.label, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text("Explorar", color = NubaMuted, fontSize = 12.sp)
    }
}

@Composable
fun VenueCard(venue: Venue, appState: AppState, onNavigate: (AppRoute) -> Unit) {
    GlassCard(modifier = Modifier.clickable { appState.selectedVenueId = venue.id; onNavigate(AppRoute.Detail) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(venue.imageUrl, null, modifier = Modifier.size(86.dp).clip(RoundedCornerShape(22.dp)), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(venue.category.label, color = NubaCyan, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Text(venue.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(venue.address, color = NubaMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniBadge(Icons.Outlined.Star, venue.rating.toString())
                    MiniBadge(Icons.Outlined.Place, venue.distance)
                }
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = Color.White)
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String = "") {
    Column(Modifier.padding(top = 4.dp)) {
        Text(title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
        if (subtitle.isNotBlank()) Text(subtitle, color = NubaMuted, fontSize = 12.sp)
    }
}

@Composable
fun MiniBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(Modifier.clip(RoundedCornerShape(99.dp)).background(Color.White.copy(.08f)).padding(horizontal = 9.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = NubaCyan, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoBlock(title: String, body: String) { GlassCard { Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(8.dp)); Text(body, color = Color.White.copy(.82f), fontSize = 14.sp, lineHeight = 21.sp) } }
@Composable
fun ServiceChip(text: String, modifier: Modifier = Modifier) { Row(modifier.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(.07f)).border(1.dp, Color.White.copy(.11f), RoundedCornerShape(16.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Check, null, tint = NubaCyan, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(8.dp)); Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) } }

@Composable
fun ProductHorizontal(products: List<Product>, appState: AppState, onNavigate: (AppRoute) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        products.forEach { ProductRow(it, appState) }
        SecondaryButton("Abrir tienda del local", icon = Icons.Outlined.Storefront) { onNavigate(AppRoute.Shop) }
    }
}

@Composable
fun ProductRow(product: Product, appState: AppState) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(product.imageUrl, null, modifier = Modifier.size(86.dp).clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(product.description, color = NubaMuted, fontSize = 12.sp, maxLines = 2)
                Text("S/ ${product.price}.00 · Stock ${product.stock}", color = NubaCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { appState.addToCart(product) }, modifier = Modifier.glassIcon()) { Icon(Icons.Outlined.AddShoppingCart, null, tint = Color.White) }
        }
    }
}

@Composable
fun ReviewMiniCard(text: String, author: String, rating: Int) { GlassCard { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Star, null, tint = NubaAmber); Spacer(Modifier.width(6.dp)); Text("$rating.0", color = Color.White, fontWeight = FontWeight.Black); Spacer(Modifier.weight(1f)); Text(author, color = NubaMuted, fontSize = 12.sp) }; Spacer(Modifier.height(8.dp)); Text(text, color = Color.White.copy(.82f), fontSize = 13.sp) } }
@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) { GlassCard(modifier = modifier, radius = 20) { Text(value, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black); Text(label, color = NubaMuted, fontSize = 11.sp) } }
@Composable
fun MenuLink(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) { GlassCard(modifier = Modifier.clickable(onClick = onClick), radius = 22) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = NubaCyan, modifier = Modifier.size(26.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontWeight = FontWeight.Black); Text(subtitle, color = NubaMuted, fontSize = 12.sp) }; Icon(Icons.Outlined.ChevronRight, null, tint = Color.White) } } }
