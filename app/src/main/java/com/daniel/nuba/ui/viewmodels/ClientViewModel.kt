package com.daniel.nuba.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daniel.nuba.data.AppState
import com.daniel.nuba.data.SupabaseConfig
import com.daniel.nuba.model.Business
import com.daniel.nuba.model.Venue
import com.daniel.nuba.model.Review
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class ClientViewModel : ViewModel() {
    // Home Screen
    fun firstName(userName: String) = userName.split(' ').first()
    fun recommendedVenues(appState: AppState) = appState.venues.take(4)

    fun loadBusinesses(appState: AppState) {
        viewModelScope.launch {
            try {
                val results = SupabaseConfig.client.postgrest["businesses"]
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("is_active", true)
                        }
                    }
                    .decodeList<Business>()

                val mappedVenues = results.map { bus ->
                    Venue(
                        id = bus.id ?: UUID.randomUUID().toString(),
                        name = bus.title,
                        category = bus.category,
                        description = bus.description ?: "",
                        address = bus.address ?: "",
                        distance = "${Random.nextInt(1, 5)}.${Random.nextInt(0, 9)} km",
                        price = bus.price.toInt(),
                        rating = 0.0,
                        reviewsCount = 0,
                        latitude = bus.latitude ?: -15.8402,
                        longitude = bus.longitude ?: -70.0212,
                        imageUrl = bus.cover_photo ?: "https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&w=1400&q=80",
                        status = if (bus.is_active) "Activo" else "Inactivo",
                        approved = true,
                        phone = bus.phone ?: "",
                        email = bus.email ?: "",
                        website = bus.website ?: ""
                    )
                }
                if (mappedVenues.isNotEmpty()) {
                    appState.venues.clear()
                    appState.venues.addAll(mappedVenues)
                }
            } catch (e: Exception) {
                // appState.toast = "Error: ${e.message}"
            }
        }
    }

    // Explore Screen State
    var exploreQuery by mutableStateOf("")

    fun filteredVenues(appState: AppState): List<Venue> {
        return appState.venues.filter {
            it.category == appState.selectedCategory &&
            it.name.contains(exploreQuery, true)
        }
    }

    // Detail Screen
    fun getVenue(appState: AppState) = appState.selectedVenue()
    fun getReviews(appState: AppState, venueId: String) = appState.venueReviews(venueId)

    fun venueRating(appState: AppState, venueId: String): Double {
        val venueReviews = appState.reviews.filter { it.businessId == venueId && it.status == "Publicada" }
        return if (venueReviews.isEmpty()) 0.0 else venueReviews.map { it.rating }.average()
    }

    fun venueReviewCount(appState: AppState, venueId: String): Int {
        return appState.reviews.count { it.businessId == venueId && it.status == "Publicada" }
    }
}
