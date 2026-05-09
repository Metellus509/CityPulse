package com.example.citypulse.data.repository

import com.example.citypulse.data.local.PlaceDao
import com.example.citypulse.model.Place
import kotlinx.coroutines.flow.Flow

class PlaceRepository(private val placeDao: PlaceDao) {

    // Récupère tous les lieux depuis la base de données Room
    val allPlaces: Flow<List<Place>> = placeDao.getAllPlaces()

    // Fonction pour insérer un lieu (utile pour les favoris)
    suspend fun insert(place: Place) {
        placeDao.insertPlace(place)
    }

    // Plus tard, nous ajouterons ici la fonction pour appeler Retrofit
    // suspend fun fetchPlacesFromNetwork() { ... }
}