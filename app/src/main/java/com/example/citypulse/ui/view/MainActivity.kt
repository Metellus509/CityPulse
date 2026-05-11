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

        // 2. Observation des données du ViewModel
        setupObservers()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configuration initiale de la vue sur Port-au-Prince
        val pap = LatLng(18.5392, -72.335)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(pap, 12f))

        // Activation des outils de la carte
        mMap?.uiSettings?.isZoomControlsEnabled = true
        mMap?.uiSettings?.isMapToolbarEnabled = true

        // --- GESTION DES CLICS ---

        // A. Clic simple sur un marqueur (affiche un Toast rapide)
        mMap?.setOnMarkerClickListener { marker ->
            val place = marker.tag as? Place
            place?.let {
                Toast.makeText(this, "Lieu : ${it.name}", Toast.LENGTH_SHORT).show()
            }
            false // Permet d'afficher aussi la bulle d'info par défaut
        }

        // B. Clic sur la bulle d'info (ouvre les détails)
        mMap?.setOnInfoWindowClickListener { marker ->
            val place = marker.tag as? Place
            place?.let {
                ouvrirDetailsLieu(it)
            }
        }

        // Vérifier si des données sont déjà arrivées avant que la carte ne soit prête
        viewModel.placesByLiveData.value?.let { list ->
            if (list.isNotEmpty()) {
                updateMarkersOnMap(list)
            }
        }
    }

    private fun setupObservers() {
        viewModel.placesByLiveData.observe(this) { listDeLieux ->
            if (!listDeLieux.isNullOrEmpty()) {
                // On informe l'utilisateur du nombre de lieux chargés
                Toast.makeText(this, "${listDeLieux.size} lieux trouvés à Port-au-Prince", Toast.LENGTH_SHORT).show()

                // Dessiner les points sur la carte
                updateMarkersOnMap(listDeLieux)
            }
        }
    }

    private fun updateMarkersOnMap(lieux: List<Place>) {
        mMap?.let { map ->
            map.clear() // On nettoie pour éviter les doublons au rafraîchissement

            for (place in lieux) {
                // On s'assure que les coordonnées sont valides (Parsing JSON corrigé)
                if (place.lat != 0.0 && place.lon != 0.0) {
                    val position = LatLng(place.lat, place.lon)

                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(place.name)
                            .snippet("Appuyez ici pour voir les détails")
                    )

                    // CRUCIAL : On attache l'objet Place au marqueur pour le récupérer au clic
                    marker?.tag = place
                }
            }
        }
    }

    private fun ouvrirDetailsLieu(place: Place) {
        // Pour ton projet de sortie, c'est ici qu'on lancera une nouvelle page
        // ou qu'on affichera une description complète.
        Toast.makeText(this, "Chargement des détails pour ${place.name}...", Toast.LENGTH_LONG).show()
    }
}