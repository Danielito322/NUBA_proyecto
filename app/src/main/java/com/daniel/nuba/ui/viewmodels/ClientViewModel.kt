package com.daniel.nuba.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.Venue
import com.daniel.nuba.model.Review

class ClientViewModel : ViewModel() {
    // Home Screen
    fun firstName(userName: String) = userName.split(' ').first()
    fun recommendedVenues(appState: AppState) = appState.venues.take(4)

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
}
