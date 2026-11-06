package com.example.myapplication.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class GearMode { P, R, N, D }
enum class TurnIndicator { OFF, LEFT, RIGHT, HAZARD }

data class VehicleState(
    val speedKmh: Float = 0f,
    val maxSpeedKmh: Float = 200f,
    val rpm: Float = 800f,
    val maxRpm: Float = 8000f,
    val gearMode: GearMode = GearMode.P,
    val currentGear: Int = 1,
    val odometerKm: Double = 12458.3,
    val tripKm: Double = 42.7,
    val fuelLevelPercent: Float = 75f,
    val coolantTempC: Float = 88f, // Engine temp ~90°C
    val batteryVoltage: Float = 14.2f, // 12V car battery running voltage
    val tripDurationSeconds: Long = 1845L, // Elapsed trip seconds
    val averageSpeedKmh: Float = 58.4f,
    val instantFuelL100Km: Float = 6.2f, // L/100km instant
    val avgFuelL100Km: Float = 6.5f, // L/100km average
    val speedLimitKmh: Float = 130f, // 50, 90, 110, 130, 150, 0 (off)
    val isNightMode: Boolean = true, // Dark cockpit vs Light day mode
    val turnIndicator: TurnIndicator = TurnIndicator.OFF,
    val isTurnLightOn: Boolean = false,
    val currentTimeString: String = "14:35",
    val currentDateString: String = "LUN 24 MAR",
    val outdoorTempC: Float = 18f,
    val weatherCondition: String = "Soleggiato",
    val compassHeading: String = "NO",
    val compassBearingDeg: Float = 315f,
    val estimatedRangeKm: Int = 540,
    val tirePressureBar: Float = 2.3f
) {
    val gearLabel: String
        get() = when (gearMode) {
            GearMode.P -> "P"
            GearMode.R -> "R"
            GearMode.N -> "N"
            GearMode.D -> "D$currentGear"
        }

    val isSpeedLimitExceeded: Boolean
        get() = speedLimitKmh > 0f && speedKmh > speedLimitKmh
}

class DashboardViewModel : ViewModel() {

    companion object {
        // Istanza Singleton condivisa tra MainActivity (GPS) e AudiCarScreen (Android Auto)
        val shared = DashboardViewModel()
    }

    private val _uiState = MutableStateFlow(VehicleState())
    val uiState: StateFlow<VehicleState> = _uiState.asStateFlow()

    private var turnLightJob: Job? = null
    private var tripTimerJob: Job? = null
    private var clockJob: Job? = null

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.ITALIAN)
    private val dateFormat = SimpleDateFormat("EEE d MMM", Locale.ITALIAN)

    init {
        startTurnLightBlinker()
        startTripComputerTimer()
        startClockTimer()
    }

    private fun startClockTimer() {
        clockJob = viewModelScope.launch {
            while (true) {
                val now = Date()
                val timeStr = timeFormat.format(now)
                val dateStr = dateFormat.format(now).uppercase()
                _uiState.update {
                    it.copy(
                        currentTimeString = timeStr,
                        currentDateString = dateStr
                    )
                }
                delay(1000)
            }
        }
    }

    fun updateGpsSpeed(gpsSpeedKmh: Float) {
        _uiState.update { state ->
            val newSpeed = gpsSpeedKmh.coerceIn(0f, state.maxSpeedKmh)

            val gear = when {
                newSpeed < 25f -> 1
                newSpeed < 50f -> 2
                newSpeed < 80f -> 3
                newSpeed < 115f -> 4
                newSpeed < 155f -> 5
                else -> 6
            }

            val minSpeedForGear = getMinSpeedForGear(gear)
            val maxSpeedForGear = getMaxSpeedForGear(gear)
            val fraction = ((newSpeed - minSpeedForGear) / (maxSpeedForGear - minSpeedForGear)).coerceIn(0f, 1f)
            val newRpm = if (newSpeed < 1f) 800f else (1100f + fraction * 6000f).coerceAtMost(state.maxRpm)

            val mode = if (newSpeed < 1f && state.gearMode != GearMode.R && state.gearMode != GearMode.N) GearMode.P else GearMode.D

            val deltaKm = (newSpeed / 3600.0) * 0.1
            val newOdo = state.odometerKm + deltaKm
            val newTrip = state.tripKm + deltaKm

            val instantFuel = if (newSpeed < 1f) 0.5f else (4.0f + (newRpm / 1000f) * 0.8f + (newSpeed / 30f)).coerceIn(3.0f, 22.0f)
            val newRange = (state.fuelLevelPercent * 7.2f).toInt()

            state.copy(
                speedKmh = newSpeed,
                rpm = newRpm,
                currentGear = gear,
                gearMode = mode,
                odometerKm = newOdo,
                tripKm = newTrip,
                instantFuelL100Km = instantFuel,
                estimatedRangeKm = newRange
            )
        }
    }

    fun updateCompassBearing(bearingDeg: Float) {
        val heading = when (((bearingDeg % 360f) + 360f) % 360f) {
            in 22.5f..67.5f -> "NE"
            in 67.5f..112.5f -> "E"
            in 112.5f..157.5f -> "SE"
            in 157.5f..202.5f -> "S"
            in 202.5f..247.5f -> "SO"
            in 247.5f..292.5f -> "O"
            in 292.5f..337.5f -> "NO"
            else -> "N"
        }
        _uiState.update { it.copy(compassHeading = heading, compassBearingDeg = bearingDeg) }
    }

    private fun startTripComputerTimer() {
        tripTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    if (state.speedKmh > 2f) {
                        val newDuration = state.tripDurationSeconds + 1
                        val newAvg = ((state.averageSpeedKmh * state.tripDurationSeconds) + state.speedKmh) / newDuration
                        state.copy(
                            tripDurationSeconds = newDuration,
                            averageSpeedKmh = newAvg
                        )
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun cycleSpeedLimit() {
        _uiState.update { state ->
            val nextLimit = when (state.speedLimitKmh) {
                50f -> 90f
                90f -> 110f
                110f -> 130f
                130f -> 150f
                150f -> 0f
                else -> 50f
            }
            state.copy(speedLimitKmh = nextLimit)
        }
    }

    fun updateVehicleSpeed(speedKmh: Float) {
        updateGpsSpeed(speedKmh)
    }

    // Funzione di test touch per simulare velocità crescenti (es. +20 km/h ad ogni tocco, fino a 140 poi torna a 0)
    fun simulateNextSpeedStep() {
        val current = _uiState.value.speedKmh
        val next = if (current >= 140f) 0f else current + 20f
        updateGpsSpeed(next)
    }

    fun updateRpm(rpm: Float) {
        _uiState.update { it.copy(rpm = rpm.coerceIn(0f, it.maxRpm)) }
    }

    fun updateGear(mode: GearMode, gearNum: Int) {
        _uiState.update { it.copy(gearMode = mode, currentGear = gearNum.coerceIn(1, 6)) }
    }

    fun updateFuel(fuelPercent: Float) {
        _uiState.update { it.copy(fuelLevelPercent = fuelPercent.coerceIn(0f, 100f)) }
    }

    fun updateCoolantTemp(tempC: Float) {
        _uiState.update { it.copy(coolantTempC = tempC.coerceIn(40f, 130f)) }
    }

    fun updateBatteryVoltage(voltage: Float) {
        _uiState.update { it.copy(batteryVoltage = voltage.coerceIn(10f, 16f)) }
    }

    fun toggleNightMode() {
        _uiState.update { it.copy(isNightMode = !it.isNightMode) }
    }

    private fun getMaxSpeedForGear(gear: Int): Float {
        return when (gear) {
            1 -> 35f
            2 -> 65f
            3 -> 100f
            4 -> 135f
            5 -> 170f
            6 -> 200f
            else -> 35f
        }
    }

    private fun getMinSpeedForGear(gear: Int): Float {
        return when (gear) {
            1 -> 0f
            2 -> 20f
            3 -> 45f
            4 -> 75f
            5 -> 110f
            6 -> 145f
            else -> 0f
        }
    }

    private fun startTurnLightBlinker() {
        turnLightJob = viewModelScope.launch {
            while (true) {
                delay(400)
                _uiState.update { state ->
                    if (state.turnIndicator != TurnIndicator.OFF) {
                        state.copy(isTurnLightOn = !state.isTurnLightOn)
                    } else {
                        state.copy(isTurnLightOn = false)
                    }
                }
            }
        }
    }

    fun setGearMode(mode: GearMode) {
        _uiState.update { it.copy(gearMode = mode) }
    }

    fun setTurnIndicator(indicator: TurnIndicator) {
        _uiState.update {
            val newInd = if (it.turnIndicator == indicator) TurnIndicator.OFF else indicator
            it.copy(turnIndicator = newInd, isTurnLightOn = newInd != TurnIndicator.OFF)
        }
    }

    fun resetTrip() {
        _uiState.update {
            it.copy(
                tripKm = 0.0,
                tripDurationSeconds = 0L,
                averageSpeedKmh = 0f
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        turnLightJob?.cancel()
        tripTimerJob?.cancel()
        clockJob?.cancel()
    }
}
