package com.example.tracktail.data.repositories

import android.content.Context
import com.example.tracktail.data.models.Pet
import com.example.tracktail.data.models.PetList
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

class PetRepository(private val context: Context) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val petsFile = File(context.filesDir, "pets.json")

    // Odczyt wszystkich zwierząt
    fun getAllPets(): List<Pet> {
        return try {
            if (!petsFile.exists()) {
                return emptyList()
            }
            val jsonString = petsFile.readText()
            val petList = json.decodeFromString<PetList>(jsonString)
            petList.pets
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Zapis nowego zwierzaka
    fun savePet(pet: Pet): Boolean {
        return try {
            val currentPets = getAllPets().toMutableList()

            // Sprawdź czy zwierzak już istnieje (aktualizacja)
            val existingIndex = currentPets.indexOfFirst { it.id == pet.id }
            if (existingIndex != -1) {
                currentPets[existingIndex] = pet
            } else {
                currentPets.add(pet)
            }

            val petList = PetList(currentPets)
            val jsonString = json.encodeToString(petList)
            petsFile.writeText(jsonString)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Usunięcie zwierzaka
    fun deletePet(petId: String): Boolean {
        return try {
            val currentPets = getAllPets().toMutableList()
            currentPets.removeAll { it.id == petId }

            val petList = PetList(currentPets)
            val jsonString = json.encodeToString(petList)
            petsFile.writeText(jsonString)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Pobranie konkretnego zwierzaka
    fun getPetById(petId: String): Pet? {
        return getAllPets().find { it.id == petId }
    }

    // Aktualizacja zwierzaka
    fun updatePet(pet: Pet): Boolean {
        return savePet(pet)
    }
}