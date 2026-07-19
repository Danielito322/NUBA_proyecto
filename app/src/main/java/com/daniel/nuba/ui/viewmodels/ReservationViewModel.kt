package com.daniel.nuba.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.AppRoute
import com.daniel.nuba.model.Booking
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class ReservationViewModel : ViewModel() {
    // Reserve Screen
    var monthOffset by mutableStateOf(0L)
    var selectedDate by mutableStateOf(LocalDate.now())
    var selectedTime by mutableStateOf("18:00")
    var extras by mutableStateOf(setOf<String>())

    fun reservationExtraPrice(name: String): Int = when (name) {
        "Balón" -> 12
        "Agua" -> 10
        "Servicio extra" -> 20
        "Decoración" -> 15
        else -> 0
    }

    fun calculateTotal(basePrice: Int): Int {
        return basePrice + extras.fold(0) { acc, item -> acc + reservationExtraPrice(item) }
    }

    fun continueToPayment(appState: AppState, locale: Locale, onNavigate: (AppRoute) -> Unit) {
        appState.pendingDate = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.titlecase(locale) } + " ${selectedDate.dayOfMonth} de " + selectedDate.month.getDisplayName(TextStyle.FULL, locale)
        appState.pendingTime = selectedTime
        appState.pendingExtras = extras.toList()
        onNavigate(AppRoute.Payment)
    }

    // Payment Screen
    var paymentMethod by mutableStateOf("Yape")

    fun confirmPayment(appState: AppState, total: Int, onNavigate: (AppRoute) -> Unit) {
        appState.pendingPayment = paymentMethod
        appState.createBooking(total)
        onNavigate(AppRoute.Confirmation)
    }

    // Bookings & Reviews
    var reviewBookingId by mutableStateOf<String?>(null)
    var rating by mutableStateOf(5)
    var comment by mutableStateOf("")
    var showReviewDialog by mutableStateOf(false)

    fun cancelBooking(booking: Booking) {
        booking.status = "Cancelada"
    }

    fun openReview(venueId: String) {
        reviewBookingId = venueId
        rating = 5
        comment = ""
        showReviewDialog = true
    }

    fun submitReview(appState: AppState) {
        val venueId = reviewBookingId ?: return
        appState.addReview(venueId, rating, comment.ifBlank { "Excelente servicio y reserva rápida." })
        showReviewDialog = false
        reviewBookingId = null
    }
}
