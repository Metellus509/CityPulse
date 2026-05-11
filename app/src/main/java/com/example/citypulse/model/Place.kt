package com.example.citypulse.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "places")
data class Place(
    @PrimaryKey
    @SerializedName("xid") val id: String,
    @SerializedName("name") val name: String,

    // Ces champs seront remplis manuellement dans le Repository
    var lat: Double = 0.0,
    var lon: Double = 0.0
)

// Cette classe sert à lire le JSON d'OpenTripMap qui est structuré différemment
data class OpenTripMapResponse(
    val xid: String,
    val name: String,
    val point: Point // L'API met les coordonnées ici !
)

data class Point(
    val lat: Double,
    val lon: Double
)