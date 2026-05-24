package com.example.citypulse.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.citypulse.data.local.AppDatabase // Ajuste cet import selon ta structure
import com.example.citypulse.model.Place
import com.example.citypulse.ui.view.MainActivity
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // Empêche le spam de notifications pour le même ID
    private val notifiedPlaces = mutableSetOf<String>()

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Démarrage obligatoire en Foreground Service
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            startForeground(
                1,
                createTrackingNotification(),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(1, createTrackingNotification())
        }

        setupLocationUpdates()
    }

    private fun createTrackingNotification(): Notification {
        return NotificationCompat.Builder(this, "citypulse_location")
            .setContentTitle("CityPulse est actif")
            .setContentText("Suivi de votre position à Port-au-Prince...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun setupLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000) // 10s
            .setMinUpdateIntervalMillis(5000) // 5s
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val userLocation = locationResult.lastLocation ?: return
                checkProximityToPlaces(userLocation)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        } catch (unlikely: SecurityException) {
            // Permissions perdues
        }
    }

    private fun checkProximityToPlaces(userLocation: Location) {
        serviceScope.launch {
            // Accès direct à Room depuis le Service via l'instance Database
            val db = AppDatabase.getDatabase(applicationContext)
            // Utilise la nouvelle fonction synchrone/directe du DAO (déclarée à l'étape 3)
            val allPlaces = db.placeDao().getAllPlacesList()

            for (place in allPlaces) {
                val distanceResults = FloatArray(1)
                Location.distanceBetween(
                    userLocation.latitude, userLocation.longitude,
                    place.lat, place.lon,
                    distanceResults
                )
                val distanceInMeters = distanceResults[0]

                if (distanceInMeters <= 500) {
                    if (!notifiedPlaces.contains(place.id)) {
                        sendProximityAlertNotification(place, distanceInMeters.toInt())
                        notifiedPlaces.add(place.id)
                    }
                } else {
                    // Supprime de la liste d'exclusion si l'utilisateur s'éloigne
                    notifiedPlaces.remove(place.id)
                }
            }
        }
    }

    private fun sendProximityAlertNotification(place: Place, distance: Int) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, place.id.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, "PROXIMITY_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Nouveau point d'intérêt proche !")
            .setContentText("${place.name} est à $distance mètres de vous.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(place.id.hashCode(), notification)
        } catch (e: SecurityException) {
            // L'utilisateur a révoqué les notifications manuellement
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}