package com.msa.qiblapro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.GlassCard
import com.msa.qiblapro.ui.pro.ProBackground
import com.msa.qiblapro.ui.pro.proShadow
import com.msa.qiblapro.ui.viewmodels.SettingsViewModel
import com.msa.qiblapro.ui.viewmodels.SettingsUiState

@Composable
fun SettingsRoute(vm: SettingsViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()

    SettingsScreen(
        state = s,
        onUseTrueNorth = vm::setUseTrueNorth,
        onAngleSmoothing = vm::setSmoothing,
        onAlignmentTolerance = vm::setAlignmentTol,
        onShowGpsPrompt = vm::setShowGpsPrompt,
        onBatterySaver = vm::setBatterySaver,
        onBgUpdateFreqSec = vm::setBgFreqSec,
        onLowPowerLocation = vm::setLowPowerLoc,
        onAutoCalibration = vm::setAutoCalib,
        onCalibrationThreshold = vm::setCalibThreshold
    )
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onUseTrueNorth: (Boolean) -> Unit,
    onAngleSmoothing: (Float) -> Unit,
    onAlignmentTolerance: (Int) -> Unit,
    onShowGpsPrompt: (Boolean) -> Unit,
    onBatterySaver: (Boolean) -> Unit,
    onBgUpdateFreqSec: (Int) -> Unit,
    onLowPowerLocation: (Boolean) -> Unit,
    onAutoCalibration: (Boolean) -> Unit,
    onCalibrationThreshold: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ProBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // --- Compass ---
            GlassCard(modifier = Modifier.fillMaxWidth().proShadow()) {
                SectionHeader(icon = Icons.Filled.CompassCalibration, title = stringResource(R.string.compass_settings))

                ProSwitchRow(
                    title = stringResource(R.string.use_true_north),
                    subtitle = stringResource(R.string.true_north_desc),
                    checked = state.useTrueNorth,
                    onCheckedChange = onUseTrueNorth
                )

                ProSliderRow(
                    title = stringResource(R.string.angle_smoothing),
                    subtitle = stringResource(R.string.smoothing_desc),
                    value = state.smoothing,
                    range = 0f..1f,
                    valueText = "${(state.smoothing * 100).toInt()}%"
                ) { onAngleSmoothing(it) }

                ProIntSliderRow(
                    title = stringResource(R.string.alignment_sensitivity),
                    subtitle = stringResource(R.string.sensitivity_desc),
                    value = state.alignmentToleranceDeg,
                    range = 2..20,
                    valueText = "${state.alignmentToleranceDeg}°"
                ) { onAlignmentTolerance(it) }

                Divider(Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                ProSwitchRow(
                    title = stringResource(R.string.auto_calibration),
                    subtitle = stringResource(R.string.auto_calibration_desc),
                    checked = state.autoCalibration,
                    onCheckedChange = onAutoCalibration
                )

                ProIntSliderRow(
                    title = stringResource(R.string.calibration_threshold),
                    subtitle = stringResource(R.string.calibration_threshold_desc),
                    value = state.calibrationThreshold,
                    range = 1..10,
                    valueText = state.calibrationThreshold.toString()
                ) { onCalibrationThreshold(it) }
            }

            // --- GPS ---
            GlassCard(modifier = Modifier.fillMaxWidth().proShadow()) {
                SectionHeader(icon = Icons.Filled.GpsFixed, title = stringResource(R.string.gps_section))

                ProSwitchRow(
                    title = stringResource(R.string.gps_prompt_title),
                    subtitle = stringResource(R.string.gps_prompt_desc),
                    checked = state.showGpsPrompt,
                    onCheckedChange = onShowGpsPrompt
                )

                ProSwitchRow(
                    title = stringResource(R.string.low_power_location),
                    subtitle = stringResource(R.string.low_power_location_desc),
                    checked = state.useLowPowerLocation,
                    onCheckedChange = onLowPowerLocation
                )
            }

            // --- Battery ---
            GlassCard(modifier = Modifier.fillMaxWidth().proShadow()) {
                SectionHeader(icon = Icons.Filled.BatterySaver, title = stringResource(R.string.battery_saver))

                ProSwitchRow(
                    title = stringResource(R.string.battery_saver_mode),
                    subtitle = stringResource(R.string.battery_saver_desc),
                    checked = state.batterySaverMode,
                    onCheckedChange = onBatterySaver
                )

                if (state.batterySaverMode) {
                    ProIntSliderRow(
                        title = stringResource(R.string.bg_update_frequency),
                        subtitle = stringResource(R.string.bg_update_frequency_desc),
                        value = state.bgUpdateFreqSec,
                        range = 2..30,
                        valueText = stringResource(R.string.seconds_fmt, state.bgUpdateFreqSec)
                    ) { onBgUpdateFreqSec(it) }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
