package com.example.citypulse.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.citypulse.model.Place

@Dao
interface PlaceDao {
    @Query("SELECT * FROM places")
    fun getAllPlaces(): LiveData<List<Place>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaces(places: List<Place>)
}