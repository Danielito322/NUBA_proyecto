package com.daniel.nuba.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daniel.nuba.data.ReviewRepository
import com.daniel.nuba.data.SupabaseReviewRepository
import com.daniel.nuba.model.Review
import io.github.jan.supabase.auth.auth
import com.daniel.nuba.data.SupabaseConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.daniel.nuba.data.AppState

class ReviewViewModel(private val repository: ReviewRepository = SupabaseReviewRepository()) : ViewModel() {

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _userReview = MutableStateFlow<Review?>(null)
    val userReview: StateFlow<Review?> = _userReview

    var averageRating by mutableStateOf(0.0)
    var reviewCount by mutableStateOf(0)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Form State
    var ratingForm by mutableStateOf(5)
    var commentForm by mutableStateOf("")
    var showSuccessDialog by mutableStateOf(false)

    fun loadReviews(businessId: String, appState: AppState? = null) {
        val userId = SupabaseConfig.client.auth.currentUserOrNull()?.id
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val list = repository.getReviews(businessId)
                _reviews.value = list
                reviewCount = list.size
                averageRating = if (list.isNotEmpty()) list.map { it.rating }.average() else 0.0
                
                // Sync with local AppState if provided
                appState?.let { state ->
                    state.reviews.removeAll { it.businessId == businessId }
                    state.reviews.addAll(list)
                }

                if (userId != null) {
                    val existing = repository.getUserReview(businessId, userId)
                    _userReview.value = existing
                    if (existing != null) {
                        ratingForm = existing.rating
                        commentForm = existing.comment ?: ""
                    } else {
                        ratingForm = 5
                        commentForm = ""
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error al cargar reseñas: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun submitReview(businessId: String, appState: AppState? = null) {
        val user = SupabaseConfig.client.auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val current = _userReview.value
                if (current == null) {
                    val newReview = Review(
                        businessId = businessId,
                        userId = user.id,
                        rating = ratingForm,
                        comment = commentForm.ifBlank { null }
                    )
                    repository.addReview(newReview)
                } else {
                    val updated = current.copy(
                        rating = ratingForm,
                        comment = commentForm.ifBlank { null }
                    )
                    repository.updateReview(updated)
                }
                loadReviews(businessId, appState)
                showSuccessDialog = true
            } catch (e: Exception) {
                errorMessage = "Error al publicar: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteReview(businessId: String, appState: AppState? = null) {
        val currentId = _userReview.value?.id ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                repository.deleteReview(currentId)
                _userReview.value = null
                ratingForm = 5
                commentForm = ""
                loadReviews(businessId, appState)
                showSuccessDialog = true
            } catch (e: Exception) {
                errorMessage = "Error al eliminar: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
