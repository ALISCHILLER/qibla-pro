package com.msa.qiblapro.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.msa.qiblapro.R
import com.msa.qiblapro.data.settings.AppSettings
import com.msa.qiblapro.ui.viewmodels.QiblaViewModel

@Composable
fun MainTabsScreen(
    modifier: Modifier = Modifier,
    vm: QiblaViewModel = hiltViewModel()
) {
    var tab by remember { mutableIntStateOf(0) }
    val st by vm.state.collectAsState()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = {
                        Icon(
                            Icons.Filled.Explore,
                            contentDescription = stringResource(R.string.tab_compass)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_compass)) }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = {
                        Icon(
                            Icons.Filled.Map,
                            contentDescription = stringResource(R.string.tab_map)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_map)) }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.tab_settings)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_settings)) }
                )
            }
        }
    ) { pad ->
        when (tab) {
            0 -> ProCompassScreen(
                st = st,
                modifier = Modifier.padding(pad)
            )

            1 -> MapScreen(
                modifier = Modifier.padding(pad),
                vm = vm // 👈 حتماً MapScreen باید این پارامتر را پشتیبانی کند
            )

            else -> {
                // ✅ ساخت AppSettings با تمام مقادیر مورد نیاز
                val appSettings = AppSettings(
                    useTrueNorth = st.useTrueNorth,
                    smoothing = st.smoothing,
                    alignmentToleranceDeg = st.alignTolerance,
                    showGpsPrompt = st.showGpsPrompt,
                    batterySaverMode = st.batterySaverMode,
                    bgUpdateFreqSec = st.bgUpdateFreqSec,
                    useLowPowerLocation = st.useLowPowerLocation,
                    autoCalibration = st.autoCalibration,
                    calibrationThreshold = st.calibrationThreshold,

                    enableVibration = st.enableVibration,
                    enableSound = st.enableSound,
                    mapType = st.mapType,
                    showIranCities = st.showIranCities
                )

                SettingsScreenPro(
                    modifier = Modifier.padding(pad),
                    s = appSettings,
                    onToggleTrueNorth = vm::setTrueNorth,
                    onSmoothingChange = vm::setSmoothing,
                    onToleranceChange = { vm.setTolerance(it.toDouble()) },
                    onToggleGpsPrompt = vm::setGpsPrompt,
                    onMapTypeChange = vm::setMapType,
                    onToggleIranCities = vm::setIranCities,
                    onVibrationChange = vm::setVibration,
                    onSoundChange = vm::setSound
                )
            }
        }
    }
}
