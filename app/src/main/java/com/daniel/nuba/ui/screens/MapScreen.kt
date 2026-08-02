package com.daniel.nuba.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daniel.nuba.ui.viewmodels.MapViewModel
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.AppRoute
import com.daniel.nuba.model.Category
import com.daniel.nuba.ui.components.*
import com.daniel.nuba.ui.theme.NubaMuted
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(appState: AppState, onNavigate: (AppRoute) -> Unit, viewModel: MapViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        viewModel.initialize(appState.selectedCategory)
    }

    val puno = LatLng(-15.8402, -70.0219)
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(puno, 13.5f) }
    val visibleVenues = viewModel.getVisibleVenues(appState)

    MobileScaffold(appState, AppRoute.Map, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Puno, Perú", "Mapa de locales", "Google Maps nativo con categorías y locales disponibles.", back = { onNavigate(AppRoute.Home) }) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Category.entries.forEach { category ->
                        CategoryPill(category, viewModel.selectedFilter == category) { viewModel.updateFilter(category) }
                    }
                }
            }
            item {
                GlassCard {
                    GoogleMap(
                        modifier = Modifier.fillMaxWidth().height(315.dp),
                        cameraPositionState = cameraPositionState
                    ) {
                        visibleVenues.forEach { venue ->
                            Marker(
                                state = MarkerState(position = LatLng(venue.latitude, venue.longitude)),
                                title = venue.name,
                                snippet = "${venue.category.label} · ${venue.distance}",
                                onClick = {
                                    viewModel.selectVenue(appState, venue.id)
                                    false
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Toca un marcador o elige un local de la lista.", color = NubaMuted, fontSize = 12.sp)
                }
            }
            item { SectionTitle("Locales en el mapa", "${visibleVenues.size} lugares disponibles") }
            items(visibleVenues) { venue -> VenueCard(venue, appState, onNavigate) }
        }
    }
}
