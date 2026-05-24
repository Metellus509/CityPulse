package com.example.citypulse.ui.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.citypulse.R
import com.example.citypulse.model.Place
import com.example.citypulse.service.LocationService
import com.example.citypulse.ui.adapter.PlaceAdapter
import com.example.citypulse.ui.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val viewModel: MainViewModel by viewModels()
    private var mMap: GoogleMap? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private lateinit var placeAdapter: PlaceAdapter
    private lateinit var sheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var sheetTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sheetTitle = findViewById(R.id.sheet_title)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupBottomSheet()
        setupRecyclerView()
        setupObservers()
    }

    private fun setupBottomSheet() {
        val bottomSheet = findViewById<ConstraintLayout>(R.id.bottom_sheet)
        sheetBehavior = BottomSheetBehavior.from(bottomSheet)
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPlaces)

        placeAdapter = PlaceAdapter(emptyList()) { place ->
            // ACTION : Cliquer dans la liste centre la carte
            val pos = LatLng(place.lat, place.lon)
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f))

            sheetTitle.text = place.name
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = placeAdapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val place = placeAdapter.getPlaceAt(position)
                viewModel.updateFavoriteStatus(place.id, !place.isFavorite)
                Toast.makeText(this@MainActivity, "Favoris mis à jour", Toast.LENGTH_SHORT).show()
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun openDetails(place: Place) {
        val intent = Intent(this, DetailsActivity::class.java).apply {
            putExtra("PLACE_ID", place.id)
            putExtra("PLACE_NAME", place.name)
            putExtra("IS_FAVORITE", place.isFavorite)
            putExtra("PLACE_LAT", place.lat) // Envoyé pour le partage
            putExtra("PLACE_LON", place.lon) // Envoyé pour le partage
        }
        startActivity(intent)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // --- LOCALISATION ET BOUTONS DE NAVIGATION ---
        mMap?.uiSettings?.apply {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = true
            isCompassEnabled = true
        }

        // Remonte les boutons pour ne pas être barrés par la liste recyclée
        mMap?.setPadding(0, 0, 0, 350)

        // Position initiale sur Port-au-Prince
        val pap = LatLng(18.5392, -72.335)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(pap, 12f))

        // Active le point bleu de localisation utilisateur
        enableUserLocation()

        // ACTION : Clic sur le marqueur ouvre les détails (pour ajouter une note)
        mMap?.setOnMarkerClickListener { marker ->
            val place = marker.tag as? Place
            place?.let { openDetails(it) }
            true
        }
    }

    private fun setupObservers() {
        viewModel.placesByLiveData.observe(this) { list ->
            if (!list.isNullOrEmpty()) {
                placeAdapter.updateData(list)
                updateMarkers(list)
            }
        }
    }

    private fun updateMarkers(list: List<Place>) {
        mMap?.clear()
        for (place in list) {
            val markerIcon = if (place.isFavorite) {
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
            } else {
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            }

            val marker = mMap?.addMarker(MarkerOptions()
                .position(LatLng(place.lat, place.lon))
                .title(place.name)
                .icon(markerIcon))

            marker?.tag = place
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.placesByLiveData.value?.let { updateMarkers(it) }
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        mMap?.isMyLocationEnabled = true
        startService(Intent(this, LocationService::class.java))
    }
}