package com.example.myapplication.shared

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat

class GpsSpeedTracker(
    context: Context,
    private val onSpeedChanged: (Float) -> Unit,
    private val onBearingChanged: (Float) -> Unit
) : LocationListener {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    fun startTracking() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    500L, // Aggiorna ogni 500ms
                    0.5f, // Aggiorna ogni 0.5 metri di spostamento
                    this
                )
                // Recupera l'ultima posizione nota per partire subito
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                lastLocation?.let { handleLocation(it) }
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
        // La velocità del Location viene restituita in metri al secondo (m/s).
        // La convertiamo in chilometri orari (km/h) moltiplicando per 3.6.
        val speedKmh = if (location.hasSpeed()) {
            location.speed * 3.6f
        } else {
            0f
        }
        onSpeedChanged(speedKmh.coerceAtLeast(0f))

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
