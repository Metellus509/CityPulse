package com.example.citypulse.remote

import com.example.citypulse.model.Place
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {
    @GET("en/places/radius")
    suspend fun getNearbyPlaces(
        @Query("radius") radius: Int,
        @Query("lon") lon: Double,
        @Query("lat") lat: Double,
        @Query("apikey") apiKey: String,
        @Query("format") format: String = "json"
    ): List<Place>
}