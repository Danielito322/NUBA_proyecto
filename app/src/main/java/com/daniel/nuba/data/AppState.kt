package com.daniel.nuba.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.daniel.nuba.model.AdminRequest
import com.daniel.nuba.model.Booking
import com.daniel.nuba.model.CartItem
import com.daniel.nuba.model.Category
import com.daniel.nuba.model.Product
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
    val products = mutableStateListOf<Product>()
    val cart = mutableStateListOf<CartItem>()
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
                    description = "Cancha de fútbol 6 con iluminación, vestidores, tienda del local y validación QR.",
                    address = "Jr. Los Incas, Puno",
                    distance = "1.2 km",
                    price = 78,
                    rating = 4.8,
                    reviews = 124,
                    latitude = -15.8436,
                    longitude = -70.0201,
                    imageUrl = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=1200&q=80",
                    services = listOf("Iluminación", "Vestidores", "Estacionamiento", "Wi-Fi"),
                    schedules = baseSchedule.map { it.copy() }.toMutableList()
                ),
                Venue(
                    id = "nexo-padel",
                    name = "Nexo Pádel",
                    category = Category.DEPORTES,
                    description = "Canchas de pádel con superficie profesional, alquiler de raquetas y venta de bebidas.",
                    address = "Av. La Torre, Puno",
                    distance = "2.0 km",
                    price = 92,
                    rating = 4.7,
                    reviews = 88,
                    latitude = -15.8358,
                    longitude = -70.0298,
                    imageUrl = "https://images.unsplash.com/photo-1622279457486-62dcc4a431d6?auto=format&fit=crop&w=1200&q=80",
                    services = listOf("Raquetas", "Duchas", "Tienda", "Cafetería"),
                    schedules = baseSchedule.map { it.copy() }.toMutableList()
                ),
                Venue(
                    id = "barber-studio",
                    name = "Studio Barber 360",
                    category = Category.BELLEZA,
                    description = "Barbería moderna con reserva por hora, productos premium y atención personalizada.",
                    address = "Jr. Lima, Puno",
                    distance = "900 m",
                    price = 35,
                    rating = 4.9,
                    reviews = 203,
                    latitude = -15.8389,
                    longitude = -70.0242,
                    imageUrl = "https://images.unsplash.com/photo-1621605815971-fbc98d665033?auto=format&fit=crop&w=1200&q=80",
                    services = listOf("Corte", "Barba", "Color", "Productos"),
                    schedules = baseSchedule.map { it.copy(open = "09:00", close = "21:00") }.toMutableList()
                ),
                Venue(
                    id = "glow-beauty",
                    name = "Glow Beauty Lab",
                    category = Category.BELLEZA,
                    description = "Salón de belleza para uñas, maquillaje y peinado con confirmación inmediata.",
                    address = "Av. El Sol, Puno",
                    distance = "1.6 km",
                    price = 45,
                    rating = 4.6,
                    reviews = 76,
                    latitude = -15.8456,
                    longitude = -70.0269,
                    imageUrl = "https://images.unsplash.com/photo-1560066984-138dadb4c035?auto=format&fit=crop&w=1200&q=80",
                    services = listOf("Uñas", "Maquillaje", "Peinado", "Tratamientos"),
                    schedules = baseSchedule.map { it.copy(open = "10:00", close = "20:00") }.toMutableList()
                ),
                Venue(
                    id = "arcade-zone",
                    name = "Arcade Zone Puno",
                    category = Category.ENTRETENIMIENTO,
                    description = "Cabinas gamer, realidad virtual y paquetes para grupos con reserva anticipada.",
                    address = "Jr. Deustua, Puno",
                    distance = "1.4 km",
                    price = 28,
                    rating = 4.5,
                    reviews = 69,
                    latitude = -15.8406,
                    longitude = -70.0213,
                    imageUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=1200&q=80",
                    services = listOf("VR", "PC Gamer", "Snacks", "Torneos"),
                    schedules = baseSchedule.map { it.copy(open = "11:00", close = "23:00") }.toMutableList()
                )
            )
        )
        products.addAll(
            listOf(
                Product("balon-pro", "arena-sur", "Balón profesional", "Deportes", 59, 12, "https://images.unsplash.com/photo-1614632537197-38a17061c2bd?auto=format&fit=crop&w=900&q=80", "Balón cosido, ideal para fútbol rápido."),
                Product("agua-pack", "arena-sur", "Pack de agua", "Bebidas", 12, 40, "https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=900&q=80", "Seis botellas para el equipo."),
                Product("raqueta", "nexo-padel", "Alquiler de raqueta", "Deportes", 18, 20, "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?auto=format&fit=crop&w=900&q=80", "Raqueta profesional por reserva."),
                Product("pomada", "barber-studio", "Pomada matte", "Belleza", 32, 18, "https://images.unsplash.com/photo-1621607512214-68297480165e?auto=format&fit=crop&w=900&q=80", "Fijación media para peinados naturales."),
                Product("aceite-barba", "barber-studio", "Aceite para barba", "Belleza", 39, 8, "https://images.unsplash.com/photo-1581182800629-7d90925ad072?auto=format&fit=crop&w=900&q=80", "Hidratación y brillo suave."),
                Product("combo-vr", "arcade-zone", "Combo VR + snack", "Entretenimiento", 25, 16, "https://images.unsplash.com/photo-1592478411213-6153e4ebc696?auto=format&fit=crop&w=900&q=80", "Complemento para experiencia gamer.")
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
                Review("r2", "barber-studio", "Luis C.", 4, "Buena atención y productos disponibles."),
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
    fun venueProducts(venueId: String = selectedVenueId): List<Product> = products.filter { it.venueId == venueId }
    fun venueReviews(venueId: String = selectedVenueId): List<Review> = reviews.filter { it.venueId == venueId && it.status == "Publicada" }

    fun filteredVenues(): List<Venue> = venues.filter { it.category == selectedCategory && it.approved && it.status == "Activo" }

    fun addToCart(product: Product) {
        val existing = cart.firstOrNull { it.product.id == product.id }
        if (existing == null) cart.add(CartItem(product, 1)) else existing.quantity++
        toast = "Producto agregado al carrito"
    }

    fun cartTotal(): Int = cart.fold(0) { total, item -> total + (item.product.price * item.quantity) }

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

    fun addProviderProduct(name: String, price: Int, stock: Int, image: String, description: String) {
        products.add(
            Product(
                id = "prod-${System.currentTimeMillis()}", venueId = selectedVenueId, name = name,
                category = selectedVenue().category.label, price = price, stock = stock,
                imageUrl = image.ifBlank { "https://images.unsplash.com/photo-1518459031867-a89b944bffe4?auto=format&fit=crop&w=900&q=80" },
                description = description.ifBlank { "Producto publicado por el proveedor." }
            )
        )
        toast = "Producto publicado"
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
