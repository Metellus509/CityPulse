package com.example.citypulse.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "places")
data class Place(
    @PrimaryKey
    @SerializedName("xid") // Nom dans le JSON de l'API
    val id: String,

    @SerializedName("name")
    val name: String,

    // Pour extraire la latitude/longitude du JSON imbriqué d'OpenTripMap
    // On simplifie ici pour la base de données locale
    val lat: Double,
    val lon: Double,

    val isFavorite: Boolean = false
)