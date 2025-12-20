package com.msa.qiblapro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.qiblapro.data.settings.SettingsRepository
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
    val enableSound: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {

    val state = repo.settingsFlow.map { s ->
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
            enableSound = s.enableSound
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setUseTrueNorth(v: Boolean) = viewModelScope.launch { repo.setUseTrueNorth(v) }
    fun setSmoothing(v: Float) = viewModelScope.launch { repo.setSmoothing(v) }
    fun setAlignmentTol(v: Int) = viewModelScope.launch { repo.setAlignmentTolerance(v) }
    fun setShowGpsPrompt(v: Boolean) = viewModelScope.launch { repo.setShowGpsPrompt(v) }
    fun setBatterySaver(v: Boolean) = viewModelScope.launch { repo.setBatterySaver(v) }
    fun setBgFreqSec(v: Int) = viewModelScope.launch { repo.setBgFreqSec(v) }
    fun setLowPowerLoc(v: Boolean) = viewModelScope.launch { repo.setLowPowerLocation(v) }
    fun setAutoCalib(v: Boolean) = viewModelScope.launch { repo.setAutoCalibration(v) }
    fun setCalibThreshold(v: Int) = viewModelScope.launch { repo.setCalibrationThreshold(v) }
    fun setMapType(v: Int) = viewModelScope.launch { repo.setMapType(v) }
    fun setIranCities(v: Boolean) = viewModelScope.launch { repo.setShowIranCities(v) }
    fun setVibration(v: Boolean) = viewModelScope.launch { repo.setVibration(v) }
    fun setSound(v: Boolean) = viewModelScope.launch { repo.setSound(v) }
}
