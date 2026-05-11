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
            val response = apiService.getNearbyPlaces(10000, lon, lat, apiKey)

            // Conversion : on extrait lat/lon du sous-objet "point"
            val placesToSave = response.map { apiPlace ->
                Place(
                    id = apiPlace.xid,
                    name = apiPlace.name,
                    lat = apiPlace.point.lat,
                    lon = apiPlace.point.lon
                )
            }

            placeDao.insertPlaces(placesToSave)
        } catch (e: Exception) {
            android.util.Log.e("Repo", "Erreur: ${e.message}")
        }
    }
}