package com.msa.qiblapro.ui.uiState

data class QiblaUiState(
    val hasLocationPermission: Boolean = false,
    val hasLocation: Boolean = false,

    val lat: Double? = null,
    val lon: Double? = null,
    val accuracyM: Float? = null,

    val headingMagDeg: Float = 0f,
    val headingTrue: Float? = null,

    val qiblaDeg: Float = 0f,
    val distanceKm: Double = 0.0,

    val rotationToQibla: Float? = null,
    val facingQibla: Boolean = false,

    val needsCalibration: Boolean = false,

    val gpsEnabled: Boolean = true,
    val showGpsDialog: Boolean = false,

    // Settings
    val useTrueNorth: Boolean = true,
    val smoothing: Float = 0.65f,
    val alignTolerance: Int = 6,
    val enableVibration: Boolean = true,
    val enableSound: Boolean = true,
    val mapType: Int = 1,
    val showIranCities: Boolean = true,
    val showGpsPrompt: Boolean = true,
    val batterySaverMode: Boolean = false,
    val bgUpdateFreqSec: Int = 5,
    val useLowPowerLocation: Boolean = true,
    val autoCalibration: Boolean = true,
    val calibrationThreshold: Int = 3,
)