package com.example.citypulse.ui.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.citypulse.R
import com.example.citypulse.model.Place
import com.example.citypulse.service.LocationService
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
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupObservers()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configuration initiale sur Port-au-Prince
        val pap = LatLng(18.5392, -72.335)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(pap, 12f))

        mMap?.uiSettings?.isZoomControlsEnabled = true
        mMap?.uiSettings?.isMapToolbarEnabled = true

        // 1. Vérification et activation de la localisation
        enableUserLocation()

        // --- GESTION DES CLICS ---
        mMap?.setOnMarkerClickListener { marker ->
            val place = marker.tag as? Place
            place?.let {
                Toast.makeText(this, "Lieu : ${it.name}", Toast.LENGTH_SHORT).show()
            }
            false
        }

        mMap?.setOnInfoWindowClickListener { marker ->
            val place = marker.tag as? Place
            place?.let { ouvrirDetailsLieu(it) }
        }

        viewModel.placesByLiveData.value?.let { list ->
            if (list.isNotEmpty()) updateMarkersOnMap(list)
        }
    }

    // --- GESTION DE LA LOCALISATION ET PERMISSIONS ---

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Demander la permission si elle n'est pas accordée
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Si la permission est ok : activer le point bleu et lancer le service
        mMap?.isMyLocationEnabled = true
        startPersistentLocationService()
    }

    private fun startPersistentLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        // Lancement du Foreground Service (obligatoire pour Android 8+)
        startForegroundService(serviceIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // L'utilisateur a accepté : on active tout
                enableUserLocation()
            } else {
                Toast.makeText(this, "La localisation est nécessaire pour CityPulse", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- LOGIQUE DES MARQUEURS ---

    private fun setupObservers() {
        viewModel.placesByLiveData.observe(this) { listDeLieux ->
            if (!listDeLieux.isNullOrEmpty()) {
                Toast.makeText(this, "${listDeLieux.size} lieux à Port-au-Prince", Toast.LENGTH_SHORT).show()
                updateMarkersOnMap(listDeLieux)
            }
        }
    }

    private fun updateMarkersOnMap(lieux: List<Place>) {
        mMap?.let { map ->
            map.clear()
            for (place in lieux) {
                if (place.lat != 0.0 && place.lon != 0.0) {
                    val position = LatLng(place.lat, place.lon)
                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(place.name)
                            .snippet("Appuyez pour voir les détails")
                    )
                    marker?.tag = place
                }
            }
        }
    }

    private fun ouvrirDetailsLieu(place: Place) {
        Toast.makeText(this, "Détails : ${place.name}", Toast.LENGTH_LONG).show()
    }
}