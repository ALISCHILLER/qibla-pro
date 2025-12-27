package com.msa.qiblapro.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
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

            // Apply language only if different, then recreate to reload resources
            LaunchedEffect(state.languageCode) {
                if (!state.isLoaded) return@LaunchedEffect
                val current = LanguageHelper.getCurrentLanguageTag()
                val target = LanguageHelper.normalizeLanguageTag(state.languageCode)

                if (current != target) {
                    LanguageHelper.applyLanguage(target)
                    this@MainActivity.recreate()
                }
            }

            val isRtl = LanguageHelper.isRtlLanguage(state.languageCode)

            CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides
                        (if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr)
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
