package com.daniel.nuba.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.daniel.nuba.model.AdminRequest
import com.daniel.nuba.model.Booking
import com.daniel.nuba.model.Category
import com.daniel.nuba.model.Review
import com.daniel.nuba.model.Role
import com.daniel.nuba.model.ScheduleSlot
import com.daniel.nuba.model.Venue
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.random.Random

class AppState {
    var role by mutableStateOf(Role.CLIENTE)
    var selectedCategory by mutableStateOf(Category.DEPORTES)
    var selectedVenueId by mutableStateOf("arena-sur")
    var pendingDate by mutableStateOf("")
    var pendingTime by mutableStateOf("")
    var pendingExtras by mutableStateOf(listOf<String>())
    var pendingPayment by mutableStateOf("Yape")
    var lastBookingCode by mutableStateOf("")
    var toast by mutableStateOf<String?>(null)
    var userName by mutableStateOf("Daniel Apaza")
    var userEmail by mutableStateOf("daniel@nuba.app")

    val venues = mutableStateListOf<Venue>()
    val bookings = mutableStateListOf<Booking>()
    val reviews = mutableStateListOf<Review>()
    val requests = mutableStateListOf<AdminRequest>()

    init {
        seed()
    }

    private fun seed() {
        val baseSchedule = mutableListOf(
            ScheduleSlot("Lunes", "08:00", "22:00"),
            ScheduleSlot("Martes", "08:00", "22:00"),
            ScheduleSlot("Miércoles", "08:00", "22:00"),
            ScheduleSlot("Jueves", "08:00", "22:00"),
            ScheduleSlot("Viernes", "08:00", "23:00"),
            ScheduleSlot("Sábado", "07:00", "23:00"),
            ScheduleSlot("Domingo", "07:00", "20:00")
        )
        venues.addAll(
            listOf(
                Venue(
                    id = "arena-sur",
                    name = "Arena Sur Puno",
                    category = Category.DEPORTES,
                    description = "Cancha de fútbol 6 con iluminación profesional, vestidores limpios y validación rápida por QR. Ideal para partidos nocturnos con amigos.",
                    address = "Jr. Los Incas 450, Puno",
                    distance = "1.2 km",
                    price = 78,
                    rating = 4.8,
                    reviewsCount = 124,
                    latitude = -15.8436,
                    longitude = -70.0201,
                    imageUrl = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=1200&q=80",
                    services = listOf("Iluminación", "Vestidores", "Estacionamiento", "Wi-Fi"),
                    schedules = baseSchedule.map { it.copy() }.toMutableList(),
                    phone = "+51 950 123 456",
                    email = "contacto@arenasurpuno.pe",
                    website = "www.arenasurpuno.pe"
                ),
                Venue(
                    id = "nexo-padel",
                    name = "Nexo Pádel",
                    category = Category.DEPORTES,
                    description = "Canchas de pádel con superficie profesional, alquiler de raquetas de alta gama y venta de bebidas energéticas y snacks.",
                    address = "Av. La Torre 890, Puno",
                    distance = "2.0 km",
                    price = 92,
                    rating = 4.7,
                    reviewsCount = 88,
                    latitude = -15.8358,
                    longitude = -70.0298,
                    imageUrl = "https://images.unsplash.com/photo-1622279457486-62dcc4a431d6?auto=format&fit=crop&w=1200&q=80",
                    services = listOf("Raquetas", "Duchas", "Cafetería"),
                    schedules = baseSchedule.map { it.copy() }.toMutableList(),
                    phone = "+51 951 789 456",
                    email = "info@nexopadel.com",
                    website = "www.nexopadel.com"
                ),
                Venue(
                    id = "barber-studio",
                    name = "Studio Barber 360",
                    category = Category.BELLEZA,
                    description = "Barbería moderna con ambiente relajado, música, reserva por hora y atención personalizada por expertos barberos.",
                    address = "Jr. Lima 120, Puno",
                    distance = "900 m",
                    price = 35,
                    rating = 4.9,
                    reviewsCount = 203,
                    latitude = -15.8389,
                    longitude = -70.0242,
                    imageUrl = "https://images.unsplash.com/photo-1621605815971-fbc98d665033?auto=format&fit=crop&w=1200&q=80",
                    services = listOf("Corte", "Barba", "Color"),
                    schedules = baseSchedule.map { it.copy(open = "09:00", close = "21:00") }.toMutableList(),
                    phone = "+51 958 456 123",
                    email = "hola@barber360.pe",
                    website = "www.barberstudio360.com"
                ),
                Venue(
                    id = "glow-beauty",
                    name = "Glow Beauty Lab",
                    category = Category.BELLEZA,
                    description = "Salón de belleza especializado en el cuidado de uñas, maquillaje profesional y peinados para eventos con confirmación inmediata.",
                    address = "Av. El Sol 315, Puno",
                    distance = "1.6 km",
                    price = 45,
                    rating = 4.6,
                    reviewsCount = 76,
                    latitude = -15.8456,
                    longitude = -70.0269,
                    imageUrl = "https://images.unsplash.com/photo-1560066984-138dadb4c035?auto=format&fit=crop&w=1200&q=80",
                    services = listOf("Uñas", "Maquillaje", "Peinado", "Tratamientos"),
                    schedules = baseSchedule.map { it.copy(open = "10:00", close = "20:00") }.toMutableList(),
                    phone = "+51 959 654 321",
                    email = "citas@glowbeauty.pe",
                    website = "www.glowbeautylab.pe"
                ),
                Venue(
                    id = "arcade-zone",
                    name = "Arcade Zone Puno",
                    category = Category.ENTRETENIMIENTO,
                    description = "Cabinas gamer de alto rendimiento, experiencias de realidad virtual inmersivas y paquetes especiales para grupos.",
                    address = "Jr. Deustua 210, Puno",
                    distance = "1.4 km",
                    price = 28,
                    rating = 4.5,
                    reviewsCount = 69,
                    latitude = -15.8406,
                    longitude = -70.0213,
                    imageUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=1200&q=80",
                    services = listOf("VR", "PC Gamer", "Snacks", "Torneos"),
                    schedules = baseSchedule.map { it.copy(open = "11:00", close = "23:00") }.toMutableList(),
                    phone = "+51 957 321 987",
                    email = "play@arcadezone.com",
                    website = "www.arcadezonepuno.pe"
                )
            )
        )
        bookings.add(
            Booking(
                id = "bk-001",
                venueId = "arena-sur",
                venueName = "Arena Sur Puno",
                dateLabel = "Sábado 04 de julio",
                time = "18:00",
                extras = listOf("Balón", "Agua"),
                total = 102,
                payment = "Yape",
                code = "NUBA-PUNO-4821"
            )
        )
        reviews.addAll(
            listOf(
                Review("r1", "arena-sur", "María Q.", 5, "La cancha estaba limpia y el QR agilizó el ingreso."),
                Review("r2", "barber-studio", "Luis C.", 4, "Buena atención y servicio rápido."),
                Review("r3", "arcade-zone", "Ana P.", 5, "Ideal para ir con amigos, la reserva fue rápida.")
            )
        )
        requests.addAll(
            listOf(
                AdminRequest("rq1", "Kuntur Cine Lounge", "Fernando Ramos", Category.ENTRETENIMIENTO),
                AdminRequest("rq2", "Bella Studio Nails", "Adriana Flores", Category.BELLEZA),
                AdminRequest("rq3", "Cancha Norte 7", "Carlos Mamani", Category.DEPORTES)
            )
        )
    }

    fun selectedVenue(): Venue = venues.firstOrNull { it.id == selectedVenueId } ?: venues.first()
    fun venueReviews(venueId: String = selectedVenueId): List<Review> = reviews.filter { it.businessId == venueId && it.status == "Publicada" }

    fun filteredVenues(): List<Venue> = venues.filter { it.category == selectedCategory && it.approved && it.status == "Activo" }

    fun createBooking(total: Int): Booking {
        val venue = selectedVenue()
        val code = "NUBA-PUNO-${Random.nextInt(1000, 9999)}"
        val booking = Booking(
            id = "bk-${System.currentTimeMillis()}",
            venueId = venue.id,
            venueName = venue.name,
            dateLabel = pendingDate.ifBlank { nextSevenDays().first() },
            time = pendingTime.ifBlank { "18:00" },
            extras = pendingExtras,
            total = total,
            payment = pendingPayment,
            code = code
        )
        bookings.add(0, booking)
        lastBookingCode = code
        toast = "Pago confirmado. QR generado."
        return booking
    }

    fun addReview(venueId: String, rating: Int, comment: String) {
        reviews.add(0, Review("r-${System.currentTimeMillis()}", venueId, userName, rating, comment))
        bookings.find { it.venueId == venueId && !it.reviewed }?.reviewed = true
        toast = "Reseña publicada"
    }
}

fun nextSevenDays(): List<String> {
    val locale = Locale("es", "PE")
    return (0..6).map { offset ->
        val d = LocalDate.now().plusDays(offset.toLong())
        val day = d.dayOfWeek.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.titlecase(locale) }
        val month = d.month.getDisplayName(TextStyle.FULL, locale)
        "$day ${d.dayOfMonth} de $month"
    }
}

fun monthDays(monthOffset: Long = 0): List<LocalDate> {
    val base = LocalDate.now().plusMonths(monthOffset).withDayOfMonth(1)
    return (0 until base.lengthOfMonth()).map { base.plusDays(it.toLong()) }
}
