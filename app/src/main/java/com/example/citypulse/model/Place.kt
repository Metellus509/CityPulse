package com.example.citypulse.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "places")
data class Place(
    @PrimaryKey
    @SerializedName("xid")
    val id: String,

    @SerializedName("name")
    val name: String,

    // Coordonnées pour la carte
    var lat: Double = 0.0,
    var lon: Double = 0.0,

    // État du favori pour changer la couleur du pointeur
    var isFavorite: Boolean = false
)

// Sert à lire le JSON d'OpenTripMap
data class OpenTripMapResponse(
    val xid: String,
    val name: String,
    val point: Point
)

data class Point(
    val lat: Double,
    val lon: Double
)