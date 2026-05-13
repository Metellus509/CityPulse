package com.example.citypulse.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.citypulse.model.Place

@Dao
interface PlaceDao {
    @Query("SELECT * FROM places")
    fun getAllPlaces(): LiveData<List<Place>>

    // CHANGE ICI : IGNORE permet de ne pas écraser les favoris existants
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaces(places: List<Place>)

    @Query("UPDATE places SET isFavorite = :isFav WHERE id = :placeId")
    suspend fun updateFavoriteStatus(placeId: String, isFav: Boolean)
}