package com.msa.qiblapro.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.qiblapro.data.settings.NeonAccent
import com.msa.qiblapro.data.settings.SettingsRepository
import com.msa.qiblapro.data.settings.ThemeMode
import com.msa.qiblapro.util.LanguageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val useTrueNorth: Boolean = true,
    val smoothing: Float = 0.65f,
    val alignmentToleranceDeg: Int = 6,
    val showGpsPrompt: Boolean = true,
    val batterySaverMode: Boolean = false,
    val bgUpdateFreqSec: Int = 5,
    val useLowPowerLocation: Boolean = true,
    val autoCalibration: Boolean = true,
    val calibrationThreshold: Int = 3,
    val mapType: Int = 1,
    val showIranCities: Boolean = true,
    val enableVibration: Boolean = true,
    val hapticStrength: Int = 2,
    val hapticPattern: Int = 1,
    val hapticCooldownMs: Long = 1500,
    val enableSound: Boolean = false,
    val neonMapStyle: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accent: NeonAccent = NeonAccent.GREEN,
    val hasSeenOnboarding: Boolean = false,
    val languageCode: String = "system",
    val isLoaded: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {



    val state = repo.settingsFlow
        .map { s ->
            SettingsUiState(
                useTrueNorth = s.useTrueNorth,
                smoothing = s.smoothing,
                alignmentToleranceDeg = s.alignmentToleranceDeg,
                showGpsPrompt = s.showGpsPrompt,
                batterySaverMode = s.batterySaverMode,
                bgUpdateFreqSec = s.bgUpdateFreqSec,
                useLowPowerLocation = s.useLowPowerLocation,
                autoCalibration = s.autoCalibration,
                calibrationThreshold = s.calibrationThreshold,
                mapType = s.mapType,
                showIranCities = s.showIranCities,
                enableVibration = s.enableVibration,
                hapticStrength = s.hapticStrength,
                hapticPattern = s.hapticPattern,
                hapticCooldownMs = s.hapticCooldownMs,
                enableSound = s.enableSound,
                neonMapStyle = s.neonMapStyle,
                themeMode = s.themeMode,
                accent = s.accent,
                hasSeenOnboarding = s.hasSeenOnboarding,
                languageCode = s.languageCode,
                isLoaded = true
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    private inline fun update(crossinline block: suspend SettingsRepository.() -> Unit) {
        viewModelScope.launch { repo.block() }
    }

    fun setUseTrueNorth(v: Boolean) = update { setUseTrueNorth(v) }
    fun setSmoothing(v: Float) = update { setSmoothing(v) }
    fun setAlignmentTol(v: Int) = update { setAlignmentTolerance(v) }
    fun setShowGpsPrompt(v: Boolean) = update { setShowGpsPrompt(v) }
    fun setBatterySaver(v: Boolean) = update { setBatterySaver(v) }
    fun setBgFreqSec(v: Int) = update { setBgFreqSec(v) }
    fun setLowPowerLoc(v: Boolean) = update { setLowPowerLocation(v) }
    fun setAutoCalib(v: Boolean) = update { setAutoCalibration(v) }
    fun setCalibThreshold(v: Int) = update { setCalibrationThreshold(v) }
    fun setMapType(v: Int) = update { setMapType(v) }
    fun setIranCities(v: Boolean) = update { setShowIranCities(v) }
    fun setVibration(v: Boolean) = update { setVibration(v) }
    fun setHapticStrength(v: Int) = update { setHapticStrength(v) }
    fun setHapticPattern(v: Int) = update { setHapticPattern(v) }
    fun setHapticCooldown(v: Long) = update { setHapticCooldown(v) }
    fun setSound(v: Boolean) = update { setSound(v) }
    fun setNeonMapStyle(v: Boolean) = update { setNeonMapStyle(v) }
    fun setThemeMode(mode: ThemeMode) = update { setThemeMode(mode) }
    fun setAccent(accent: NeonAccent) = update { setAccent(accent) }
    fun setHasSeenOnboarding(v: Boolean) = update { setHasSeenOnboarding(v) }

    fun setLanguage(langCode: String) {
        viewModelScope.launch {
            repo.setLanguageCode(LanguageHelper.normalizeLanguageTag(langCode))
        }
    }
    fun resetToDefaults() = update { resetToDefaults() }
}
