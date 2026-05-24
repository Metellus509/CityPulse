package com.example.citypulse.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.citypulse.BuildConfig // 👈 Import de la configuration générée
import com.example.citypulse.data.local.AppDatabase
import com.example.citypulse.model.Place
import com.example.citypulse.remote.RetrofitInstance
import com.example.citypulse.repository.PlaceRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlaceRepository
    val placesByLiveData: LiveData<List<Place>>

    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.placeDao()

        val apiService = RetrofitInstance.api

        repository = PlaceRepository(apiService, dao)
        placesByLiveData = repository.allPlaces

        loadPlaces()
    }

    private fun loadPlaces() {
        viewModelScope.launch {
            // Coordonnées de Port-au-Prince
            val lat = 18.5392
            val lon = -72.335

            // 👈 RÉCUPÉRATION SÉCURISÉE DE LA CLÉ DEPUIS LOCAL.PROPERTIES
            val apiKey = BuildConfig.OPENTRIPMAP_KEY

            repository.refreshData(lat, lon, apiKey)
        }
    }

    fun updateFavoriteStatus(placeId: String, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavorite(placeId, isFavorite)
        }
    }

    fun saveNote(placeId: String, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(placeId, note)
        }
    }

    suspend fun getPlaceById(placeId: String): Place? {
        return repository.getPlaceById(placeId)
    }
}