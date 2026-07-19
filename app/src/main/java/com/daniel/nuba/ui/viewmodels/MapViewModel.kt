package com.daniel.nuba.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.Category
import com.daniel.nuba.model.Venue

class MapViewModel : ViewModel() {
    var selectedFilter by mutableStateOf<Category?>(null)

    fun initialize(initialCategory: Category?) {
        selectedFilter = initialCategory
    }

    fun getVisibleVenues(appState: AppState): List<Venue> {
        return appState.venues.filter { selectedFilter == null || it.category == selectedFilter }
    }

    fun updateFilter(category: Category?) {
        selectedFilter = if (selectedFilter == category) null else category
    }

    fun selectVenue(appState: AppState, venueId: String) {
        appState.selectedVenueId = venueId
    }
}
