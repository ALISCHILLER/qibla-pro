package com.msa.qiblapro.ui.main

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.msa.qiblapro.ui.nav.AppNavGraph
import com.msa.qiblapro.ui.settings.SettingsViewModel
import com.msa.qiblapro.ui.theme.QiblaTheme
import com.msa.qiblapro.util.LanguageHelper
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsVm: SettingsViewModel = hiltViewModel()
            val state by settingsVm.state.collectAsStateWithLifecycle()

            // Handle language change
            LaunchedEffect(state.languageCode) {
                if (!state.isLoaded) return@LaunchedEffect
                
                val target = LanguageHelper.normalizeLanguageTag(state.languageCode)
                val current = LanguageHelper.getCurrentLanguageTag()

                if (target != current) {
                    LanguageHelper.applyLanguage(target)
                    // No need to call recreate() manually as AppCompatDelegate handles it,
                    // but we ensure target is applied to avoid loops.
                }
            }

            val isRtl = LanguageHelper.isRtlLanguage(state.languageCode)
            
            // Force Locale for current context to ensure strings are updated correctly
            val locale = remember(state.languageCode) {
                val tag = LanguageHelper.normalizeLanguageTag(state.languageCode)
                if (tag == "system") Locale.getDefault() else Locale.forLanguageTag(tag)
            }

            val configuration = LocalConfiguration.current
            configuration.setLocale(locale)
            val context = LocalContext.current
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

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
