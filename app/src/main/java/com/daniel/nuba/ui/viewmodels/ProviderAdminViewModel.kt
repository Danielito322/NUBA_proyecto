package com.daniel.nuba.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daniel.nuba.data.AppState
import com.daniel.nuba.data.SupabaseConfig
import com.daniel.nuba.model.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "ProviderAdminVM"

class ProviderAdminViewModel : ViewModel() {
    private val client = SupabaseConfig.client
    
    // Provider Screen Tabs
    var providerTab by mutableStateOf("Negocios")

    // Admin Screen Tabs
    var adminTab by mutableStateOf("Aprobaciones")

    // Business Management
    var businesses = mutableStateListOf<Business>()
    var loadingBusinesses by mutableStateOf(false)
    var showBusinessDialog by mutableStateOf(false)
    var editingBusiness by mutableStateOf<Business?>(null)

    // Form fields for Business
    var busTitle by mutableStateOf("")
    var busDescription by mutableStateOf("")
    var busCategory by mutableStateOf(Category.DEPORTES)
    var busAddress by mutableStateOf("")
    var busPhone by mutableStateOf("")
    var busEmail by mutableStateOf("")
    var busImage by mutableStateOf("")

    var uploadingImage by mutableStateOf(false)

    fun uploadBusinessImage(bytes: ByteArray, appState: AppState) {
        val userId = client.auth.currentUserOrNull()?.id
        if (userId == null) {
            appState.toast = "Error: Usuario no autenticado"
            return
        }
        val fileName = "$userId/${UUID.randomUUID()}.jpg"
        uploadingImage = true
        viewModelScope.launch {
            try {
                // He actualizado el nombre del bucket a "business-images"
                val bucket = client.storage["business-images"]
                // Se agrega el userId al path para cumplir con políticas RLS típicas
                bucket.upload(fileName, bytes)
                busImage = bucket.publicUrl(fileName)
                appState.toast = "Imagen subida"
            } catch (e: Exception) {
                Log.e(TAG, "Error subiendo imagen: ${e.message}", e)
                appState.toast = "Error Storage: ${e.message}"
            } finally {
                uploadingImage = false
            }
        }
    }

    fun loadProviderBusinesses() {
        val userId = client.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.e(TAG, "loadProviderBusinesses: userId es null")
            return
        }
        loadingBusinesses = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cargando negocios para user: $userId")
                val results = client.postgrest["businesses"]
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("owner_id", userId)
                        }
                    }
                    .decodeList<Business>()
                Log.d(TAG, "Negocios cargados: ${results.size}")
                businesses.clear()
                businesses.addAll(results)
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando negocios: ${e.message}", e)
            } finally {
                loadingBusinesses = false
            }
        }
    }

    fun openCreateBusiness() {
        editingBusiness = null
        busTitle = ""
        busDescription = ""
        busCategory = Category.DEPORTES
        busAddress = ""
        busPhone = ""
        busEmail = ""
        busImage = ""
        showBusinessDialog = true
    }

    fun openEditBusiness(business: Business) {
        editingBusiness = business
        busTitle = business.title
        busDescription = business.description ?: ""
        busCategory = business.category
        busAddress = business.address ?: ""
        busPhone = business.phone ?: ""
        busEmail = business.email ?: ""
        busImage = business.cover_photo ?: ""
        showBusinessDialog = true
    }

    fun saveBusiness(appState: AppState) {
        val userId = client.auth.currentUserOrNull()?.id
        if (userId == null) {
            appState.toast = "Error: Sesión no válida"
            return
        }
        
        Log.d(TAG, "Iniciando guardado de negocio: $busTitle, Editing: ${editingBusiness?.id}")

        viewModelScope.launch {
            try {
                if (editingBusiness == null) {
                    val business = Business(
                        owner_id = userId,
                        title = busTitle,
                        description = busDescription.ifBlank { null },
                        category = busCategory,
                        address = busAddress.ifBlank { null },
                        phone = busPhone.ifBlank { null },
                        email = busEmail.ifBlank { null },
                        cover_photo = busImage.ifBlank { null }
                    )
                    Log.d(TAG, "Insertando nuevo negocio via Postgrest")
                    client.postgrest["businesses"].insert(business)
                    appState.toast = "Negocio creado con éxito"
                } else {
                    val business = Business(
                        id = editingBusiness!!.id,
                        owner_id = userId,
                        title = busTitle,
                        description = busDescription.ifBlank { null },
                        category = busCategory,
                        address = busAddress.ifBlank { null },
                        phone = busPhone.ifBlank { null },
                        email = busEmail.ifBlank { null },
                        cover_photo = busImage.ifBlank { null }
                    )
                    Log.d(TAG, "Actualizando negocio via Postgrest: ${editingBusiness!!.id}")
                    client.postgrest["businesses"].update(business) {
                        filter { eq("id", editingBusiness!!.id!!) }
                    }
                    appState.toast = "Negocio actualizado"
                }
                showBusinessDialog = false
                loadProviderBusinesses()
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar negocio: ${e.message}", e)
                appState.toast = "Error al guardar: ${e.message}"
            }
        }
    }

    fun deleteBusiness(businessId: String, appState: AppState) {
        viewModelScope.launch {
            try {
                client.postgrest["businesses"].delete {
                    filter { eq("id", businessId) }
                }
                appState.toast = "Negocio eliminado"
                loadProviderBusinesses()
            } catch (e: Exception) {
                appState.toast = "Error al eliminar: ${e.message}"
            }
        }
    }

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
