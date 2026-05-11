package com.example.citypulse.ui.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.citypulse.R
import com.example.citypulse.model.Place
import com.example.citypulse.ui.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val viewModel: MainViewModel by viewModels()
    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Initialisation du fragment de la carte
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 2. Lancement de l'observation des données
        setupObservers()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configuration visuelle de la carte
        val pap = LatLng(18.5392, -72.335) // Port-au-Prince
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(pap, 12f))
        mMap?.uiSettings?.isZoomControlsEnabled = true

        // SECURITÉ : Si le ViewModel a déjà reçu les données avant que la carte ne soit prête,
        // on force l'affichage maintenant.
        viewModel.placesByLiveData.value?.let { lieux ->
            if (lieux.isNotEmpty()) {
                updateMarkersOnMap(lieux)
            }
        }
    }

    private fun setupObservers() {
        viewModel.placesByLiveData.observe(this) { listDeLieux ->
            if (listDeLieux != null && listDeLieux.isNotEmpty()) {
                // Petit message pour confirmer la réception des données
                Toast.makeText(this, "${listDeLieux.size} lieux chargés", Toast.LENGTH_SHORT).show()

                // On met à jour la carte
                updateMarkersOnMap(listDeLieux)
            }
        }
    }

    private fun updateMarkersOnMap(lieux: List<Place>) {
        // On vérifie que mMap n'est pas nul avant de dessiner
        mMap?.let { map ->
            map.clear() // On évite de dupliquer les marqueurs

            for (place in lieux) {
                // Vérification si les coordonnées ne sont pas à 0.0 (soucis de parsing JSON fréquent)
                if (place.lat != 0.0 && place.lon != 0.0) {
                    val position = LatLng(place.lat, place.lon)
                    map.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(place.name)
                            .snippet("Lieu touristique")
                    )
                }
            }
        }
    }
}