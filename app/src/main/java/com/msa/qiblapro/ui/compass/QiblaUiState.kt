package com.msa.qiblapro.ui.compass

import com.msa.qiblapro.data.settings.NeonAccent
import com.msa.qiblapro.data.settings.ThemeMode

data class QiblaUiState(
    val hasLocationPermission: Boolean = false,
    val hasLocation: Boolean = false,

    val userLat: Double? = null,
    val userLon: Double? = null,
    val locationAccuracyM: Float? = null,

    val headingDeg: Float = 0f,
    val headingTrue: Float? = null,

    val qiblaBearingDeg: Float = 0f,
    val distanceKm: Double = 0.0,
    val declinationDeg: Float = 0f,

    val rotationErrorDeg: Float = 0f,
    val isFacingQibla: Boolean = false,

    val needsCalibration: Boolean = false,
    val showCalibrationGuide: Boolean = false,
    val showCalibrationSheet: Boolean = false,
    val isSensorAvailable: Boolean = true,

    val gpsEnabled: Boolean = true,
    val showGpsDialog: Boolean = false,
    val showGpsPrompt: Boolean = true,
    val airplaneModeOn: Boolean = false,

    // ✅ Settings Mirror
    val useTrueNorth: Boolean = true,
    val smoothing: Float = 0.65f,
    val alignTolerance: Int = 6,
    val enableVibration: Boolean = true,
    val hapticStrength: Int = 2,
    val hapticPattern: Int = 1,
    val hapticCooldownMs: Long = 1500,
    val enableSound: Boolean = true,
    val mapType: Int = 1,
    val showIranCities: Boolean = true,
    val neonMapStyle: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accent: NeonAccent = NeonAccent.GREEN,
    val languageCode: String = "system",

    // ✅ اضافه‌شده‌های ضروری برای رفع خطا
    val autoCalibration: Boolean = true,
    val calibrationThreshold: Int = 3,
    val batterySaverMode: Boolean = false
) {
    val accuracyLabel: String
        get() = locationAccuracyM?.let { "${it.toInt()} m" } ?: "—"

    // Aliases for compatibility with existing UI code
    val qiblaDeg: Float get() = qiblaBearingDeg
    val lat: Double? get() = userLat
    val lon: Double? get() = userLon
}
