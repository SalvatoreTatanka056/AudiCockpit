package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.myapplication.shared.DashboardScreen
import com.example.myapplication.shared.DashboardViewModel
import com.example.myapplication.shared.GpsSpeedTracker

class MainActivity : ComponentActivity() {

    private lateinit var gpsSpeedTracker: GpsSpeedTracker
    private val viewModel = DashboardViewModel.shared

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                gpsSpeedTracker.startTracking()
            }
            else -> {
                // Permesso negato
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        gpsSpeedTracker = GpsSpeedTracker(
            context = this,
            onSpeedChanged = { speed -> viewModel.updateVehicleSpeed(speed) },
            onBearingChanged = { bearing -> viewModel.updateCompassBearing(bearing) }
        )

        checkAndRequestLocationPermission()

        setContent {
            DashboardScreen(viewModel = viewModel)
        }
    }

    private fun checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gpsSpeedTracker.startTracking()
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gpsSpeedTracker.stopTracking()
    }
}
