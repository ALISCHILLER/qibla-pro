package com.msa.qiblapro.ui.compass

import com.msa.qiblapro.data.settings.NeonAccent
import com.msa.qiblapro.data.settings.ThemeMode
import kotlin.math.abs

data class QiblaUiState(
    val hasLocationPermission: Boolean = false,
    val hasLocation: Boolean = false,

    val userLat: Double? = null,
    val userLon: Double? = null,
    val locationAccuracyM: Float? = null,

    val headingDeg: Float = 0f,
    val headingTrue: Float? = null,
    
    // مقادیر کمکی برای تشخیص پایداری قطب‌نما
    val lastHeadingDeg: Float = 0f,
    val compassStabilityScore: Float = 0f, // 0 to 1

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

    val autoCalibration: Boolean = true,
    val calibrationThreshold: Int = 3,
    val batterySaverMode: Boolean = false
) {
    val accuracyLabel: String
        get() = locationAccuracyM?.let { "${it.toInt()} m" } ?: "—"

    // قبله فقط زمانی نمایش داده شود که قطب‌نما کمی پایدار شده باشد
    val isCompassReady: Boolean get() = isSensorAvailable && compassStabilityScore > 0.4f

    val qiblaDeg: Float get() = qiblaBearingDeg
    val lat: Double? get() = userLat
    val lon: Double? get() = userLon
}
