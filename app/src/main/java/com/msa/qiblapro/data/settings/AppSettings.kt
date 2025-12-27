package com.msa.qiblapro.data.settings

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class NeonAccent { GREEN, BLUE, PURPLE, PINK }

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
    val hapticStrength: Int = 2,
    val hapticPattern: Int = 1,
    val hapticCooldownMs: Long = 1500,
    val enableSound: Boolean = true,
    val mapType: Int = 1,
    val showIranCities: Boolean = true,
    val neonMapStyle: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accent: NeonAccent = NeonAccent.GREEN,
    val hasSeenOnboarding: Boolean = false,
    val languageCode: String = "system"
)
