package com.daniel.nuba.ui.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.daniel.nuba.auth.BiometricAuth
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.Venue
import com.daniel.nuba.model.Product
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
    fun getProducts(appState: AppState, venueId: String) = appState.venueProducts(venueId)
    fun getReviews(appState: AppState, venueId: String) = appState.venueReviews(venueId)

    // Cart Screen State
    var showPaymentSheet by mutableStateOf(false)

    fun finalizePurchase(appState: AppState) {
        appState.cart.clear()
        appState.toast = "Compra confirmada"
        showPaymentSheet = false
    }

    // Profile Screen State
    var showSecurityDialog by mutableStateOf(false)
    var confirmPassword by mutableStateOf("")
    var biometricEnabled by mutableStateOf(false)

    fun loadProfileState(context: Context, appState: AppState) {
        biometricEnabled = BiometricAuth.isEnabled(context, appState.role)
    }

    fun disableBiometric(context: Context, appState: AppState) {
        if (BiometricAuth.credentialIsValid(appState.role, appState.userEmail, confirmPassword)) {
            BiometricAuth.disableRole(context, appState.role)
            biometricEnabled = false
            appState.toast = "Acceso biométrico olvidado para ${appState.role.title}"
            showSecurityDialog = false
            confirmPassword = ""
        } else {
            appState.toast = "Contraseña incorrecta. No se modificó la seguridad."
        }
    }
}
