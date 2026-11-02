package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.viewModels
import com.example.myapplication.shared.DashboardScreen
import com.example.myapplication.shared.DashboardViewModel

class AutomotiveActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            DashboardScreen(viewModel = viewModel)
        }

        initAutomotiveEcuListeners()
    }

    private fun initAutomotiveEcuListeners() {
        try {
            Class.forName("android.car.Car")
        } catch (_: Exception) {
        }
    }
}
