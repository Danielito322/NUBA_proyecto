package com.daniel.nuba.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Reviews
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
enum class Role(val title: String, val subtitle: String, @Transient val icon: ImageVector = Icons.Outlined.Person) {
    @SerialName("CLIENTE") CLIENTE("Cliente", "Reservar, pagar y calificar", Icons.Outlined.Person),
    @SerialName("PROVEEDOR") PROVEEDOR("Proveedor", "Publicar locales y horarios", Icons.Outlined.BusinessCenter),
    @SerialName("ADMIN") ADMIN("Administrador", "Aprobar negocios, usuarios y reportes", Icons.Outlined.AdminPanelSettings)
}

@Serializable
enum class Category(val label: String, @Transient val icon: ImageVector = Icons.Outlined.SportsSoccer) {
    @SerialName("SPORTS") DEPORTES("Deportes", Icons.Outlined.SportsSoccer),
    @SerialName("BEAUTY") BELLEZA("Belleza", Icons.Outlined.Brush),
    @SerialName("ENTERTAINMENT") ENTRETENIMIENTO("Entretenimiento", Icons.Outlined.EventSeat)
}

@Serializable
data class AuthUser(
    val email: String,
    val displayName: String,
    val role: Role,
    val uid: String? = null,
    val isDemo: Boolean = false,
    val photoUrl: String? = null,
    val bio: String? = null,
    val refreshToken: String? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Business(
    @EncodeDefault(EncodeDefault.Mode.NEVER) val id: String? = null,
    val owner_id: String,
    var title: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER) var description: String? = null,
    var category: Category,
    @EncodeDefault(EncodeDefault.Mode.NEVER) var address: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) var latitude: Double? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) var longitude: Double? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) var phone: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) var email: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) var website: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) var cover_photo: String? = null,
    var is_active: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val created_at: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val updated_at: String? = null
)

@Serializable
data class Venue(
    val id: String,
    var name: String,
    var category: Category,
    var description: String,
    var address: String,
    var distance: String,
    var price: Int,
    var rating: Double,
    var reviews: Int,
    val latitude: Double,
    val longitude: Double,
    var imageUrl: String,
    var status: String = "Activo",
    var approved: Boolean = true,
    var owner: String = "Daniel Apaza",
    var services: List<String> = listOf("Reserva digital", "Confirmación QR", "Pago móvil"),
    var schedules: MutableList<ScheduleSlot> = mutableListOf()
)

@Serializable
data class Booking(
    val id: String,
    val venueId: String,
    val venueName: String,
    var dateLabel: String,
    var time: String,
    var extras: List<String>,
    var total: Int,
    var status: String = "Confirmada",
    var payment: String = "Yape",
    val code: String,
    var reviewed: Boolean = false
)

@Serializable
data class Review(
    val id: String,
    val venueId: String,
    val author: String,
    val rating: Int,
    val comment: String,
    var status: String = "Publicada",
    var response: String = ""
)

@Serializable
data class ScheduleSlot(
    val day: String,
    val open: String,
    val close: String,
    var active: Boolean = true
)

@Serializable
data class AdminRequest(
    val id: String,
    val title: String,
    val owner: String,
    val category: Category,
    var status: String = "Pendiente"
)

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: AppRoute
)

sealed class AppRoute(val title: String, val icon: ImageVector) {
    data object Login : AppRoute("Login", Icons.Outlined.Person)
    data object Register : AppRoute("Registro", Icons.Outlined.PersonAdd)
    data object Home : AppRoute("Inicio", Icons.Outlined.Home)
    data object Explore : AppRoute("Explorar", Icons.Outlined.Explore)
    data object Detail : AppRoute("Detalle", Icons.Outlined.BusinessCenter)
    data object Reserve : AppRoute("Reservar", Icons.Outlined.CalendarMonth)
    data object Payment : AppRoute("Pago", Icons.Outlined.ConfirmationNumber)
    data object Confirmation : AppRoute("QR", Icons.Outlined.QrCodeScanner)
    data object Map : AppRoute("Mapa", Icons.Outlined.Map)
    data object Bookings : AppRoute("Reservas", Icons.Outlined.CalendarMonth)
    data object Reviews : AppRoute("Reseñas", Icons.Outlined.Reviews)
    data object Profile : AppRoute("Perfil", Icons.Outlined.Person)
    data object Provider : AppRoute("Negocio", Icons.Outlined.Dashboard)
    data object Admin : AppRoute("Admin", Icons.Outlined.AdminPanelSettings)
}
