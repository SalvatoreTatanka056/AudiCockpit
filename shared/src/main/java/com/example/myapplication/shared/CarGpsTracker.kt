package com.example.myapplication.shared

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.car.app.CarContext

class CarGpsTracker(
    private val carContext: CarContext,
    private val onSpeedChanged: (Float) -> Unit,
    private val onBearingChanged: (Float) -> Unit
) : LocationListener {

    private val locationManager = carContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var lastLocation: Location? = null
    private var lastTimeMs: Long = 0L

    @SuppressLint("MissingPermission")
    fun startTracking() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    300L,
                    0.2f,
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

        if (location.hasSpeed()) {
            speedKmh = location.speed * 3.6f
        } else if (lastLocation != null) {
            val distanceMeters = lastLocation!!.distanceTo(location)
            val timeElapsedSeconds = (currentTimeMs - lastTimeMs) / 1000f

            if (timeElapsedSeconds > 0.1f) {
                val metersPerSecond = distanceMeters / timeElapsedSeconds
                speedKmh = metersPerSecond * 3.6f
            }
        }

        lastLocation = location
        lastTimeMs = currentTimeMs

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
