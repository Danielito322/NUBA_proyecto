package com.daniel.nuba.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Logout
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daniel.nuba.ui.viewmodels.ClientViewModel
import com.daniel.nuba.ui.viewmodels.ReviewViewModel
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.AppRoute
import com.daniel.nuba.model.Category
import com.daniel.nuba.model.Venue
import com.daniel.nuba.ui.components.*
import com.daniel.nuba.ui.theme.*

@Composable
fun HomeScreen(appState: AppState, onNavigate: (AppRoute) -> Unit, viewModel: ClientViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        viewModel.loadBusinesses(appState)
    }
    MobileScaffold(appState, AppRoute.Home, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            item {
                ScreenHeader("Hola, ${viewModel.firstName(appState.userName)}", "Reserva tu momento", "Deportes, belleza y entretenimiento en Puno.", Icons.Outlined.Person) { onNavigate(AppRoute.Profile) }
            }
            item {
                ImageHero("https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&w=1400&q=80", 210) {
                    Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
                        Text("Confirmación rápida", color = NubaCyan, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text("Elige, paga y entra con QR", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
                        Text("Reservas con calendario mensual, servicios premium y reseñas reales.", color = Color.White.copy(.78f), fontSize = 13.sp)
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

            items(viewModel.recommendedVenues(appState)) { venue -> VenueCard(venue, appState, onNavigate, viewModel) }
        }
    }
}

@Composable
fun ExploreScreen(appState: AppState, onNavigate: (AppRoute) -> Unit, viewModel: ClientViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        viewModel.loadBusinesses(appState)
    }
    MobileScaffold(appState, AppRoute.Explore, onNavigate) {
        val filtered = viewModel.filteredVenues(appState)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Explorar", appState.selectedCategory.label, "Filtra locales, revisa fotos y disponibilidad.") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Category.entries.forEach { category -> CategoryPill(category, category == appState.selectedCategory) { appState.selectedCategory = category } }
                }
            }
            item {
                OutlinedTextField(
                    value = viewModel.exploreQuery,
                    onValueChange = { viewModel.exploreQuery = it },
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
            items(filtered) { venue -> VenueCard(venue, appState, onNavigate, viewModel) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    appState: AppState,
    onNavigate: (AppRoute) -> Unit,
    viewModel: ClientViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel()
) {
    val venue = viewModel.getVenue(appState)
    val reviews by reviewViewModel.reviews.collectAsState()
    val userReview by reviewViewModel.userReview.collectAsState()

    LaunchedEffect(venue.id) {
        reviewViewModel.loadReviews(venue.id, appState)
    }

    MobileScaffold(appState, AppRoute.Detail, onNavigate) {
        if (reviewViewModel.showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { reviewViewModel.showSuccessDialog = false },
                confirmButton = {
                    Button(
                        onClick = { reviewViewModel.showSuccessDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = NubaViolet)
                    ) {
                        Text("Entendido")
                    }
                },
                title = { Text("¡Gracias!", color = Color.White, fontWeight = FontWeight.Black) },
                text = { Text("Tu reseña ha sido publicada con éxito.", color = Color.White.copy(0.8f)) },
                containerColor = Color(0xFF1A2133),
                shape = RoundedCornerShape(24.dp)
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Detalle del local", venue.name, venue.address, back = { onNavigate(AppRoute.Explore) }) }
            item {
                ImageHero(venue.imageUrl, 280) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.8f))))) {
                        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                            Surface(
                                color = NubaCyan,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    venue.category.label,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(venue.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 10.dp)) {
                                val currentRating = viewModel.venueRating(appState, venue.id)
                                val currentReviewCount = viewModel.venueReviewCount(appState, venue.id)
                                Box(Modifier.clickable { onNavigate(AppRoute.Reviews) }) {
                                    MiniBadge(Icons.Outlined.Star, "${String.format(Locale.getDefault(), "%.1f", currentRating)} ($currentReviewCount reseñas)")
                                }
                                MiniBadge(Icons.Outlined.Place, venue.distance)
                            }
                        }
                    }
                }
            }
            item { 
                SectionTitle("Descripción")
                GlassCard { 
                    Text(
                        venue.description, 
                        color = Color.White.copy(.85f), 
                        fontSize = 15.sp, 
                        lineHeight = 24.sp 
                    ) 
                } 
            }
            item {
                SectionTitle("Información de contacto")
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ContactInfoItem(Icons.Outlined.Place, "Dirección", venue.address)
                        if (venue.phone.isNotBlank()) ContactInfoItem(Icons.Outlined.Phone, "Teléfono", venue.phone)
                        if (venue.email.isNotBlank()) ContactInfoItem(Icons.Outlined.Email, "Correo electrónico", venue.email)
                        if (venue.website.isNotBlank()) ContactInfoItem(Icons.Outlined.Language, "Sitio Web", venue.website)
                    }
                }
            }
            item {
                SectionTitle("Servicios incluidos")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    venue.services.forEach { service ->
                        ServiceChip(service)
                    }
                }
            }

            item {
                SectionTitle(if (userReview == null) "Escribir una reseña" else "Tu reseña")
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Calificación:", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            (1..5).forEach { star ->
                                Icon(
                                    if (star <= reviewViewModel.ratingForm) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                                    null,
                                    tint = if (star <= reviewViewModel.ratingForm) NubaAmber else NubaMuted,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { reviewViewModel.ratingForm = star }
                                )
                            }
                        }
                        
                        OutlinedTextField(
                            value = reviewViewModel.commentForm,
                            onValueChange = { reviewViewModel.commentForm = it },
                            placeholder = { Text("Cuéntanos tu experiencia (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NubaCyan,
                                unfocusedBorderColor = Color.White.copy(0.1f)
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { reviewViewModel.submitReview(venue.id, appState) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NubaViolet),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !reviewViewModel.isLoading
                            ) {
                                Text(if (userReview == null) "Publicar" else "Actualizar")
                            }
                            
                            if (userReview != null) {
                                IconButton(
                                    onClick = { reviewViewModel.deleteReview(venue.id, appState) },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Red.copy(0.1f))
                                ) {
                                    Icon(Icons.Outlined.Delete, null, tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }

            item {
                PrimaryButton("Ver todas las reseñas", icon = Icons.Outlined.Reviews) {
                    onNavigate(AppRoute.Reviews)
                }
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
                        ) { Text("Reservar", fontWeight = FontWeight.Black); Spacer(Modifier.width(5.dp)); Icon(Icons.AutoMirrored.Outlined.ArrowForward, null) }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProfileScreen(appState: AppState, onNavigate: (AppRoute) -> Unit, viewModel: ClientViewModel = viewModel()) {
    MobileScaffold(appState, AppRoute.Profile, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Tu cuenta", "Perfil", "Preferencias, historial y soporte.") }
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
            item { MenuLink("Cerrar sesión", "Volver a elegir tipo de cuenta", Icons.AutoMirrored.Outlined.Logout) { onNavigate(AppRoute.Login) } }
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
fun VenueCard(venue: Venue, appState: AppState, onNavigate: (AppRoute) -> Unit, viewModel: ClientViewModel) {
    val rating = viewModel.venueRating(appState, venue.id)
    GlassCard(
        modifier = Modifier.clickable { 
            appState.selectedVenueId = venue.id
            onNavigate(AppRoute.Detail) 
        },
        radius = 24
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                AsyncImage(
                    model = venue.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Floating Badge for Rating
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Star, null, tint = if (rating > 0) NubaAmber else NubaMuted, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", rating),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Category Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NubaCyan.copy(0.9f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = venue.category.label,
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = venue.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "S/ ${venue.price}",
                        color = NubaCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Spacer(Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Place, null, tint = NubaMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = venue.address,
                        color = NubaMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = venue.description,
                    color = Color.White.copy(0.7f),
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
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
fun ReviewMiniCard(text: String, author: String, rating: Int) { GlassCard { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Star, null, tint = NubaAmber); Spacer(Modifier.width(6.dp)); Text("$rating.0", color = Color.White, fontWeight = FontWeight.Black); Spacer(Modifier.weight(1f)); Text(author, color = NubaMuted, fontSize = 12.sp) }; Spacer(Modifier.height(8.dp)); Text(text, color = Color.White.copy(.82f), fontSize = 13.sp) } }
@Composable
fun ContactInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = NubaCyan, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, color = NubaMuted, fontSize = 11.sp)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) { GlassCard(modifier = modifier, radius = 20) { Text(value, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black); Text(label, color = NubaMuted, fontSize = 11.sp) } }
@Composable
fun MenuLink(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) { GlassCard(modifier = Modifier.clickable(onClick = onClick), radius = 22) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = NubaCyan, modifier = Modifier.size(26.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontWeight = FontWeight.Black); Text(subtitle, color = NubaMuted, fontSize = 12.sp) }; Icon(Icons.Outlined.ChevronRight, null, tint = Color.White) } } }
