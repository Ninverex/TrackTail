package com.example.tracktail.data.repositories

import android.content.Context
import com.example.tracktail.data.models.Walk
import com.example.tracktail.data.models.WalkList
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

class WalkRepository(private val context: Context) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val walksFile = File(context.filesDir, "walks.json")

    fun getAllWalks(): List<Walk> {
        return try {
            if (!walksFile.exists()) {
                return emptyList()
            }
            val jsonString = walksFile.readText()
            val walkList = json.decodeFromString<WalkList>(jsonString)
            walkList.walks.sortedByDescending { it.startTime }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun saveWalk(walk: Walk): Boolean {
        return try {
            val currentWalks = getAllWalks().toMutableList()

            val existingIndex = currentWalks.indexOfFirst { it.id == walk.id }
            if (existingIndex != -1) {
                currentWalks[existingIndex] = walk
            } else {
                currentWalks.add(walk)
            }

            val walkList = WalkList(currentWalks)
            val jsonString = json.encodeToString(walkList)
            walksFile.writeText(jsonString)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteWalk(walkId: String): Boolean {
        return try {
            val currentWalks = getAllWalks().toMutableList()
            currentWalks.removeAll { it.id == walkId }

            val walkList = WalkList(currentWalks)
            val jsonString = json.encodeToString(walkList)
            walksFile.writeText(jsonString)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getWalkById(walkId: String): Walk? {
        return getAllWalks().find { it.id == walkId }
    }
}