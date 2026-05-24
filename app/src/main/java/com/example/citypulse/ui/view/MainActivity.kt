package com.example.citypulse.ui.view

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
        createNotificationChannels()
    }

    private fun setupBottomSheet() {
        val bottomSheet = findViewById<ConstraintLayout>(R.id.bottom_sheet)
        sheetBehavior = BottomSheetBehavior.from(bottomSheet)
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPlaces)

        placeAdapter = PlaceAdapter(emptyList()) { place ->
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
            putExtra("PLACE_LAT", place.lat)
            putExtra("PLACE_LON", place.lon)
        }
        startActivity(intent)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap?.uiSettings?.apply {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = true
            isCompassEnabled = true
        }

        mMap?.setPadding(0, 0, 0, 350)

        val pap = LatLng(18.5392, -72.335)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(pap, 12f))

        enableUserLocation()

        mMap?.setOnMarkerClickListener { marker ->
            val place = marker.tag as? Place
            place?.let { openDetails(it) }
            true
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val trackingChannel = NotificationChannel(
                "citypulse_location",
                "Localisation CityPulse",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(trackingChannel)

            val proximityChannel = NotificationChannel(
                "PROXIMITY_CHANNEL",
                "Alertes de proximité",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications lorsqu'un lieu d'intérêt est à moins de 500m"
            }
            manager.createNotificationChannel(proximityChannel)
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
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        // Si les permissions sont déjà acceptées précédemment
        activateLocationFeatures()
    }

    // 👈 AJOUT MAJEUR : Cette fonction force le point bleu à s'activer dès l'autorisation obtenue
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activateLocationFeatures()
            } else {
                Toast.makeText(this, "Localisation refusée : le point bleu est désactivé.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 👈 AJOUT MAJEUR : Centralisation de l'activation pour éviter la redondance
    private fun activateLocationFeatures() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap?.isMyLocationEnabled = true // Force l'apparition du point bleu
            startService(Intent(this, LocationService::class.java)) // Lance le service de traque
        }
    }
}