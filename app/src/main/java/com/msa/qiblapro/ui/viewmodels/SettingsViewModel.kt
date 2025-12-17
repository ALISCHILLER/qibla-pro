package com.msa.qiblapro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.qiblapro.data.settings.SettingsRepository
import com.msa.qiblapro.ui.screens.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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
            calibrationThreshold = s.calibrationThreshold
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState(
        true, 0.65f, 6, true, false, 5, true, true, 3
    ))

    fun setUseTrueNorth(v: Boolean) = viewModelScope.launch { repo.setUseTrueNorth(v) }
    fun setSmoothing(v: Float) = viewModelScope.launch { repo.setSmoothing(v) }
    fun setAlignmentTol(v: Int) = viewModelScope.launch { repo.setAlignmentTolerance(v) }

    fun setShowGpsPrompt(v: Boolean) = viewModelScope.launch { repo.setShowGpsPrompt(v) }
    fun setBatterySaver(v: Boolean) = viewModelScope.launch { repo.setBatterySaver(v) }
    fun setBgFreqSec(v: Int) = viewModelScope.launch { repo.setBgFreqSec(v) }
    fun setLowPowerLoc(v: Boolean) = viewModelScope.launch { repo.setLowPowerLocation(v) }

    fun setAutoCalib(v: Boolean) = viewModelScope.launch { repo.setAutoCalibration(v) }
    fun setCalibThreshold(v: Int) = viewModelScope.launch { repo.setCalibrationThreshold(v) }
}
