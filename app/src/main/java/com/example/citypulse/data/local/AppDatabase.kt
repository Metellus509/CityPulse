package com.example.citypulse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.citypulse.local.PlaceDao
import com.example.citypulse.model.Place

// On définit les entités (tables) et la version de la base
@Database(entities = [Place::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // On lie le DAO à la base de données
    abstract fun placeDao(): PlaceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Le Singleton pour éviter d'ouvrir plusieurs instances de la base en même temps
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "citypulse_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}