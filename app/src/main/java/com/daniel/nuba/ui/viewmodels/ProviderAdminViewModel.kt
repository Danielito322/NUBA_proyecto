package com.daniel.nuba.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.*

class ProviderAdminViewModel : ViewModel() {
    // Provider Screen Tabs
    var providerTab by mutableStateOf("Resumen")

    // Admin Screen Tabs
    var adminTab by mutableStateOf("Aprobaciones")

    // Actions
    fun updateBookingStatus(booking: Booking, status: String) {
        booking.status = status
    }

    fun updateRequestStatus(request: AdminRequest, status: String) {
        request.status = status
    }

    fun updateVenueStatus(venue: Venue, active: Boolean) {
        venue.status = if (active) "Activo" else "Suspendido"
    }

    fun updateReviewStatus(review: Review, status: String) {
        review.status = status
    }

    fun respondToReview(review: Review, response: String) {
        review.response = response
    }

    fun toggleSchedule(slot: ScheduleSlot, active: Boolean) {
        slot.active = active
    }

    // Provider Local Edit State
    var localName by mutableStateOf("")
    var localDescription by mutableStateOf("")
    var localImage by mutableStateOf("")
    var localCategory by mutableStateOf(Category.DEPORTES)

    fun loadLocalData(venue: Venue) {
        localName = venue.name
        localDescription = venue.description
        localImage = venue.imageUrl
        localCategory = venue.category
    }

    fun saveLocalChanges(appState: AppState) {
        val venue = appState.selectedVenue()
        venue.name = localName
        venue.description = localDescription
        venue.imageUrl = localImage
        venue.category = localCategory
        appState.toast = "Local actualizado"
    }

    // Provider Products
    var showAddProduct by mutableStateOf(false)
    var newProductName by mutableStateOf("")
    var newProductPrice by mutableStateOf("0")
    var newProductStock by mutableStateOf("0")
    var newProductImage by mutableStateOf("")
    var newProductDesc by mutableStateOf("")

    fun resetNewProductFields() {
        newProductName = ""
        newProductPrice = "0"
        newProductStock = "0"
        newProductImage = ""
        newProductDesc = ""
    }

    fun addProduct(appState: AppState) {
        appState.addProviderProduct(
            newProductName.ifBlank { "Producto premium" },
            newProductPrice.toIntOrNull() ?: 0,
            newProductStock.toIntOrNull() ?: 0,
            newProductImage,
            newProductDesc
        )
        showAddProduct = false
        resetNewProductFields()
    }

    // Provider QR
    var qrCode by mutableStateOf("")

    fun validateQr(appState: AppState) {
        val found = appState.bookings.firstOrNull { it.code.equals(qrCode, true) }
        if (found != null && found.status != "Usada") {
            found.status = "Usada"
            appState.toast = "Reserva validada correctamente"
        } else {
            appState.toast = "Código no válido o ya usado"
        }
    }
}
