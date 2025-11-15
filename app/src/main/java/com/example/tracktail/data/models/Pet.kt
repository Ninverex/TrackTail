package com.example.tracktail.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Pet(
    val id: String,
    val name: String,
    val species: String,
    val breed: String,
    val birthDate: Long, // timestamp w milisekundach
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun generateId(): String = "pet_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

@Serializable
data class PetList(
    val pets: List<Pet> = emptyList()
)