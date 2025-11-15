package com.example.tracktail.data.models

import kotlinx.serialization.Serializable

@Serializable
data class WalkPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)

@Serializable
data class Walk(
    val id: String,
    val petId: String?,
    val startTime: Long,
    val endTime: Long?,
    val distance: Float, // w metrach
    val duration: Long, // w milisekundach
    val route: List<WalkPoint> = emptyList()
) {
    companion object {
        fun generateId(): String = "walk_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

@Serializable
data class WalkList(
    val walks: List<Walk> = emptyList()
)