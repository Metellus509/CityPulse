package com.example.citypulse.data.local

import androidx.room.*
import com.example.citypulse.model.Place
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    // Récupère tous les lieux. Le "Flow" permet à l'interface de se mettre
    // à jour automatiquement dès que la base de données change.
    @Query("SELECT * FROM places")
    fun getAllPlaces(): Flow<List<Place>>

    // Insère ou met à jour un lieu (utile pour la mise en favoris)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: Place)

    // Supprime un lieu des favoris
    @Delete
    suspend fun deletePlace(place: Place)
}