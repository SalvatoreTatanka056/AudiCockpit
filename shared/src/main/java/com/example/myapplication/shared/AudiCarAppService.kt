package com.example.myapplication.shared

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class AudiCarAppService : CarAppService() {
    
    override fun createHostValidator(): HostValidator {
        // Permettiamo a qualsiasi host (auto) di connettersi, 
        // necessario per le app installate fuori dal Play Store.
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return object : Session() {
            override fun onCreateScreen(intent: Intent): Screen {
                return AudiCarScreen(carContext)
            }
        }
    }
}
