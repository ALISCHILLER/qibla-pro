package com.msa.qiblapro.ui.settings

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.msa.qiblapro.util.LanguageHelper

@Composable
fun SettingsRoute(
    vm: SettingsViewModel = hiltViewModel(),
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    val state by vm.state.collectAsState()

    // ✅ به محض تغییر زبان، Locale و UI اپ به‌روزرسانی می‌شود
    LaunchedEffect(Unit) {
        vm.languageChanged.collect { langCode ->
            LanguageHelper.applyLanguage(langCode)
            (context as? Activity)?.recreate() // برای بازسازی کامل UI با زبان جدید
        }
    }

    // ✅ نمایش صفحه تنظیمات
    SettingsScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is SettingsAction.SetLanguage        -> vm.setLanguage(action.langCode)
                is SettingsAction.SetUseTrueNorth    -> vm.setUseTrueNorth(action.v)
                is SettingsAction.SetSmoothing       -> vm.setSmoothing(action.v)
                is SettingsAction.SetAlignmentTol    -> vm.setAlignmentTol(action.v)
                is SettingsAction.SetShowGpsPrompt   -> vm.setShowGpsPrompt(action.v)
                is SettingsAction.SetBatterySaver    -> vm.setBatterySaver(action.v)
                is SettingsAction.SetBgUpdateFreq    -> vm.setBgFreqSec(action.v)
                is SettingsAction.SetLowPowerLoc     -> vm.setLowPowerLoc(action.v)
                is SettingsAction.SetAutoCalib       -> vm.setAutoCalib(action.v)
                is SettingsAction.SetCalibThreshold  -> vm.setCalibThreshold(action.v)
                is SettingsAction.SetVibration       -> vm.setVibration(action.v)
                is SettingsAction.SetHapticStrength  -> vm.setHapticStrength(action.v)
                is SettingsAction.SetHapticPattern   -> vm.setHapticPattern(action.v)
                is SettingsAction.SetHapticCooldown  -> vm.setHapticCooldown(action.v)
                is SettingsAction.SetSound           -> vm.setSound(action.v)
                is SettingsAction.SetMapType         -> vm.setMapType(action.v)
                is SettingsAction.SetIranCities      -> vm.setIranCities(action.v)
                is SettingsAction.SetThemeMode       -> vm.setThemeMode(action.mode)
                is SettingsAction.SetAccent          -> vm.setAccent(action.accent)
                SettingsAction.OpenAbout             -> onNavigateToAbout()
            }
        },
        onOpenAbout = onNavigateToAbout // ✅ برای کامپوننت About
    )
}
