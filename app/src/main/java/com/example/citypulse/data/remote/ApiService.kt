package com.example.citypulse.data.remote

import com.example.citypulse.model.Place
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    // Exemple : Récupérer des lieux autour de coordonnées
    @GET("places")
    suspend fun getNearbyPlaces(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): List<Place>
}