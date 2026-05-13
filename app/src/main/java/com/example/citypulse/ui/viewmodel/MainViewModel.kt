package com.example.citypulse.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.citypulse.data.local.AppDatabase
import com.example.citypulse.model.Place
import com.example.citypulse.remote.RetrofitInstance
import com.example.citypulse.repository.PlaceRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlaceRepository

    // On déclare la variable sans lui donner de valeur immédiatement
    val placesByLiveData: LiveData<List<Place>>

    init {
        // 1. Récupérer le DAO depuis la database (avec le bon package data.local)
        val database = AppDatabase.getDatabase(application)
        val dao = database.placeDao()

        // 2. Récupérer l'API via ton Singleton
        val apiService = RetrofitInstance.api

        // 3. Initialiser le repository EN PREMIER
        repository = PlaceRepository(apiService, dao)

        // 4. Maintenant que le repository existe, on peut lier le LiveData
        placesByLiveData = repository.allPlaces

        // 5. Charger les données depuis l'API
        loadPlaces()
    }

    private fun loadPlaces() {
        viewModelScope.launch {
            // Coordonnées de Port-au-Prince
            val lat = 18.5392
            val lon = -72.335
            val apiKey = "5ae2e3f221c38a28845f05b6948ae2162f94d9b315e19c7766940257"

            repository.refreshData(lat, lon, apiKey)
        }
    }

    // Ajoute cette fonction à l'intérieur de ta classe MainViewModel
    fun updateFavoriteStatus(placeId: String, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavorite(placeId, isFavorite)
        }
    }
}