package com.msa.qiblapro.ui.settings

import com.msa.qiblapro.data.settings.NeonAccent
import com.msa.qiblapro.data.settings.ThemeMode

sealed interface SettingsAction {
    data class SetUseTrueNorth(val v: Boolean) : SettingsAction
    data class SetSmoothing(val v: Float) : SettingsAction
    data class SetAlignmentTol(val v: Int) : SettingsAction
    data class SetShowGpsPrompt(val v: Boolean) : SettingsAction
    data class SetBatterySaver(val v: Boolean) : SettingsAction
    data class SetBgUpdateFreq(val v: Int) : SettingsAction
    data class SetLowPowerLoc(val v: Boolean) : SettingsAction
    data class SetAutoCalib(val v: Boolean) : SettingsAction
    data class SetCalibThreshold(val v: Int) : SettingsAction
    data class SetVibration(val v: Boolean) : SettingsAction
    data class SetHapticStrength(val v: Int) : SettingsAction
    data class SetHapticPattern(val v: Int) : SettingsAction
    data class SetHapticCooldown(val v: Long) : SettingsAction
    data class SetSound(val v: Boolean) : SettingsAction
    data class SetMapType(val v: Int) : SettingsAction
    data class SetIranCities(val v: Boolean) : SettingsAction
    data class SetThemeMode(val mode: ThemeMode) : SettingsAction
    data class SetAccent(val accent: NeonAccent) : SettingsAction
    data class SetLanguage(val langCode: String) : SettingsAction
    object OpenAbout : SettingsAction
}
