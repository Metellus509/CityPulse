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
                // On crée l'objet Place avec le champ isFavorite
                Place(
                    id = apiPlace.xid,
                    name = apiPlace.name,
                    lat = apiPlace.point.lat,
                    lon = apiPlace.point.lon,
                    isFavorite = false // Par défaut à la création
                )
            }

            // Stratégie de sauvegarde : On insère les nouveaux lieux
            placeDao.insertPlaces(placesToSave)

        } catch (e: Exception) {
            android.util.Log.e("Repo", "Erreur lors du refresh: ${e.message}")
        }
    }

    suspend fun updateFavorite(placeId: String, isFavorite: Boolean) {
        placeDao.updateFavoriteStatus(placeId, isFavorite)
    }

    // Dans PlaceRepository.kt

    suspend fun updateNote(placeId: String, note: String) {
        placeDao.updateNote(placeId, note)
    }

    suspend fun getPlaceById(placeId: String): Place? {
        return placeDao.getPlaceById(placeId)
    }
    /**
     * Nouvelle fonction pour mettre à jour l'état favori d'un lieu
     * Cela déclenchera automatiquement le LiveData et changera la couleur sur la carte
     */
    suspend fun toggleFavorite(placeId: String, isFavorite: Boolean) {
        // Tu peux ajouter une méthode spécifique dans ton DAO ou faire un update ici
        // placeDao.updateFavorite(placeId, isFavorite)
    }
}