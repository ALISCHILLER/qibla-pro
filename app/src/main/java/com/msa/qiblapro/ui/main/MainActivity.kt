package com.msa.qiblapro.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.msa.qiblapro.ui.nav.AppNavGraph
import com.msa.qiblapro.ui.settings.SettingsViewModel
import com.msa.qiblapro.ui.theme.QiblaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsVm: SettingsViewModel = hiltViewModel()
            val state by settingsVm.state.collectAsState()
            
            QiblaTheme(
                themeMode = state.themeMode,
                accent = state.accent
            ) {
                AppNavGraph()
            }
        }
    }
}
