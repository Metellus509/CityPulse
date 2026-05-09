package com.example.citypulse.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.citypulse.model.Place


class MainViewModel : ViewModel() {

    // On utilise MutableLiveData pour modifier les données en interne
    private val _places = MutableLiveData<List<Place>>()

    // On expose une LiveData immuable pour que la View (Activity) puisse l'observer
    val places: LiveData<List<Place>> get() = _places

    init {
        // Pour l'instant, on charge des données de test
        loadDemoPlaces()
    }

    private fun loadDemoPlaces() {
        // Simulation de données avant d'avoir Retrofit ou Room de prêt
        val demoList = listOf(
            Place("1", "Palais National", "Port-au-Prince", 18.543, -72.339, "Monument"),
            Place("2", "MUPANAH", "Place des Héros", 18.544, -72.336, "Musée")
        )
        _places.value = demoList
    }
}