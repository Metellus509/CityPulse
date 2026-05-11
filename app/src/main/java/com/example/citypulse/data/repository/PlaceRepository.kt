package com.example.citypulse.repository

import androidx.lifecycle.LiveData
import com.example.citypulse.local.PlaceDao
import com.example.citypulse.model.Place
import com.example.citypulse.remote.APIService

class PlaceRepository(private val apiService: APIService, private val placeDao: PlaceDao) {

    // La source de vérité est la base de données (LiveData)
    val allPlaces: LiveData<List<Place>> = placeDao.getAllPlaces()

    suspend fun refreshData(lat: Double, lon: Double, apiKey: String) {
        try {
            // 1. Appel réseau
            val response = apiService.getNearbyPlaces(10000, lon, lat, apiKey)
            // 2. Mise à jour automatique de la base locale
            placeDao.insertPlaces(response)
        } catch (e: Exception) {
            // Si le réseau échoue, on ne fait rien : l'utilisateur verra
            // les anciennes données déjà présentes dans Room
        }
    }
}