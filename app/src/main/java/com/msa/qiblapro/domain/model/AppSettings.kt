package com.msa.qiblapro.domain.model

data class AppSettings(
    val useTrueNorth: Boolean = true,
    val smoothing: Float = 0.65f,
    val alignmentToleranceDeg: Int = 6,
    val showGpsPrompt: Boolean = true,
    val batterySaverMode: Boolean = false,
    val bgUpdateFreqSec: Int = 5,
    val useLowPowerLocation: Boolean = true,
    val autoCalibration: Boolean = true,
    val calibrationThreshold: Int = 3,
    val enableVibration: Boolean = true,
    val enableSound: Boolean = true,
    val mapType: Int = 1,
    val showIranCities: Boolean = true
)
