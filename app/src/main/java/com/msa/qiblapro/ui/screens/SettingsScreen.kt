package com.msa.qiblapro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.pro.ProBackground
import com.msa.qiblapro.ui.viewmodels.SettingsViewModel
import com.msa.qiblapro.ui.viewmodels.SettingsUiState

@Composable
fun SettingsRoute(
    vm: SettingsViewModel = hiltViewModel()
) {
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
        onCalibrationThreshold = vm::setCalibThreshold,
        onVibration = vm::setVibration,
        onSound = vm::setSound,
        onMapType = vm::setMapType,
        onShowIranCities = vm::setIranCities
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
    onVibration: (Boolean) -> Unit,
    onSound: (Boolean) -> Unit,
    onMapType: (Int) -> Unit,
    onShowIranCities: (Boolean) -> Unit,
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
            AppCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    icon = Icons.Filled.CompassCalibration,
                    title = stringResource(R.string.compass_settings)
                )

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
                    valueText = "${(state.smoothing * 100).toInt()}%",
                    onValueChange = onAngleSmoothing
                )

                ProIntSliderRow(
                    title = stringResource(R.string.alignment_sensitivity),
                    subtitle = stringResource(R.string.sensitivity_desc),
                    value = state.alignmentToleranceDeg,
                    range = 2..20,
                    valueText = "${state.alignmentToleranceDeg}°",
                    onValueChange = onAlignmentTolerance
                )

                HorizontalDivider(
                    Modifier.padding(top = 10.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )

                ProSwitchRow(
                    title = stringResource(R.string.auto_calibration),
                    subtitle = stringResource(R.string.calibration_guide),
                    checked = state.autoCalibration,
                    onCheckedChange = onAutoCalibration
                )

                ProIntSliderRow(
                    title = stringResource(R.string.calibration_threshold_title),
                    subtitle = stringResource(R.string.calibration_threshold_desc),
                    value = state.calibrationThreshold,
                    range = 1..10,
                    valueText = state.calibrationThreshold.toString(),
                    onValueChange = onCalibrationThreshold
                )
            }

            // --- GPS / Location ---
            AppCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    icon = Icons.Filled.GpsFixed,
                    title = stringResource(R.string.gps_section)
                )

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

            // --- Feedback / Haptics ---
            AppCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    icon = Icons.Filled.BatterySaver, // می‌تونی آیکون بهتری بذاری مثل Volume/Vibration
                    title = stringResource(R.string.feedback_section_title)
                )

                ProSwitchRow(
                    title = stringResource(R.string.feedback_vibration_title),
                    subtitle = stringResource(R.string.feedback_vibration_desc),
                    checked = state.enableVibration,
                    onCheckedChange = onVibration
                )

                ProSwitchRow(
                    title = stringResource(R.string.feedback_sound_title),
                    subtitle = stringResource(R.string.feedback_sound_desc),
                    checked = state.enableSound,
                    onCheckedChange = onSound
                )
            }

            // --- Battery / Background ---
            AppCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    icon = Icons.Filled.BatterySaver,
                    title = stringResource(R.string.battery_saver)
                )

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
                        valueText = "${state.bgUpdateFreqSec} s",
                        onValueChange = onBgUpdateFreqSec
                    )
                }
            }

            // --- Map section (اختیاری – UI ساده برای الان) ---
            AppCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    icon = Icons.Filled.GpsFixed,
                    title = stringResource(R.string.map_section_title)
                )

                ProSwitchRow(
                    title = stringResource(R.string.show_iran_cities_title),
                    subtitle = stringResource(R.string.show_iran_cities_desc),
                    checked = state.showIranCities,
                    onCheckedChange = onShowIranCities
                )

                // فعلاً ساده: 1 = Normal, 2 = Satellite, 3 = Terrain (مثلاً)
                ProIntSliderRow(
                    title = stringResource(R.string.map_type_title),
                    subtitle = stringResource(R.string.map_type_desc),
                    value = state.mapType,
                    range = 1..3,
                    valueText = state.mapType.toString(),
                    onValueChange = onMapType
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

/* ---------- UI pieces ---------- */

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(10.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun ProSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun ProSliderRow(
    title: String,
    subtitle: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                valueText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = { onValueChange(it.coerceIn(range.start, range.endInclusive)) },
            valueRange = range
        )
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
fun ProIntSliderRow(
    title: String,
    subtitle: String,
    value: Int,
    range: IntRange,
    valueText: String,
    onValueChange: (Int) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                valueText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = {
                val intVal = it.toInt().coerceIn(range.first, range.last)
                onValueChange(intVal)
            },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = (range.last - range.first - 1).coerceAtLeast(0)
        )
    }
    Spacer(Modifier.height(6.dp))
}
