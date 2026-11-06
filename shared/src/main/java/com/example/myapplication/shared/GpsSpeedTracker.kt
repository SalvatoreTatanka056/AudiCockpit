package com.example.myapplication.shared

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

class GpsSpeedTracker(
    context: Context,
    private val onSpeedChanged: (Float) -> Unit,
    private val onBearingChanged: (Float) -> Unit
) : LocationListener {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var lastLocation: Location? = null
    private var lastTimeMs: Long = 0L

    @SuppressLint("MissingPermission")
    fun startTracking() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    300L, // Aggiorna ogni 300ms
                    0.2f, // Aggiorna ogni 0.2 metri
                    this
                )
                val cached = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                cached?.let { handleLocation(it) }
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000L,
                    1f,
                    this
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopTracking() {
        try {
            locationManager.removeUpdates(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleLocation(location: Location) {
        val currentTimeMs = System.currentTimeMillis()
        var speedKmh = 0f

        // 1. Tentativo con la velocità nativa del GPS (se disponibile)
        if (location.hasSpeed()) {
            speedKmh = location.speed * 3.6f
        } else if (lastLocation != null) {
            // 2. FALLBACK GEOMETRICO: Calcolo basato su distanza / tempo
            // Utile se l'app di mock location o il chip GPS non valorizzano hasSpeed()
            val distanceMeters = lastLocation!!.distanceTo(location)
            val timeElapsedSeconds = (currentTimeMs - lastTimeMs) / 1000f

            if (timeElapsedSeconds > 0.1f) {
                val metersPerSecond = distanceMeters / timeElapsedSeconds
                speedKmh = metersPerSecond * 3.6f
            }
        }

        lastLocation = location
        lastTimeMs = currentTimeMs

        // Filtro antirumore: se la velocità calcolata è inferiore a 1 km/h, consideriamo fermo (0)
        val finalSpeed = if (speedKmh < 1.0f) 0f else speedKmh

        onSpeedChanged(finalSpeed.coerceAtLeast(0f))

        if (location.hasBearing()) {
            onBearingChanged(location.bearing)
        }
    }

    override fun onLocationChanged(location: Location) {
        handleLocation(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
