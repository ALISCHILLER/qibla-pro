package com.msa.qiblapro.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.msa.qiblapro.ui.nav.AppNavGraph
import com.msa.qiblapro.ui.settings.SettingsViewModel
import com.msa.qiblapro.ui.theme.QiblaTheme
import com.msa.qiblapro.util.LanguageHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsVm: SettingsViewModel = hiltViewModel()
            val state by settingsVm.state.collectAsStateWithLifecycle()

            // 1) Apply language only if different, then recreate to reload resources
            LaunchedEffect(state.languageCode) {
                val current = LanguageHelper.getCurrentLanguageTag()
                val target = LanguageHelper.normalizeLanguageTag(state.languageCode)

                if (current != target) {
                    LanguageHelper.applyLanguage(target)
                    // Force Activity recreation so that resources (strings) reload correctly
                    this@MainActivity.recreate()
                }
            }

            // 2) RTL/LTR based on language (fa -> RTL)
            val isRtl = LanguageHelper.isRtlLanguage(state.languageCode)

            CompositionLocalProvider(
                LocalLayoutDirection provides (if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr)
            ) {
                QiblaTheme(
                    themeMode = state.themeMode,
                    accent = state.accent
                ) {
                    AppNavGraph(
                        hasSeenOnboarding = state.hasSeenOnboarding,
                        onOnboardingFinish = { settingsVm.setHasSeenOnboarding(true) }
                    )
                }
            }
        }
    }
}
