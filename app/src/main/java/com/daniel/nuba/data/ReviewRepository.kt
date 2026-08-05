package com.daniel.nuba.data

import com.daniel.nuba.model.Review
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ReviewRepository {
    suspend fun getReviews(businessId: String): List<Review>
    suspend fun getUserReview(businessId: String, userId: String): Review?
    suspend fun addReview(review: Review): Review
    suspend fun updateReview(review: Review): Review
    suspend fun deleteReview(reviewId: String)
}

class SupabaseReviewRepository : ReviewRepository {
    private val client = SupabaseConfig.client

    override suspend fun getReviews(businessId: String): List<Review> = withContext(Dispatchers.IO) {
        client.postgrest["reviews"]
            .select(columns = Columns.ALL) {
                filter {
                    eq("business_id", businessId)
                }
            }
            .decodeList<Review>()
    }

    override suspend fun getUserReview(businessId: String, userId: String): Review? = withContext(Dispatchers.IO) {
        client.postgrest["reviews"]
            .select(columns = Columns.ALL) {
                filter {
                    eq("business_id", businessId)
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<Review>()
    }

    override suspend fun addReview(review: Review): Review = withContext(Dispatchers.IO) {
        client.postgrest["reviews"].insert(review).decodeSingle<Review>()
    }

    override suspend fun updateReview(review: Review): Review = withContext(Dispatchers.IO) {
        client.postgrest["reviews"].update(review) {
            filter {
                eq("id", review.id!!)
            }
        }.decodeSingle<Review>()
    }

    override suspend fun deleteReview(reviewId: String): Unit = withContext(Dispatchers.IO) {
        client.postgrest["reviews"].delete {
            filter {
                eq("id", reviewId)
            }
        }
    }
}
