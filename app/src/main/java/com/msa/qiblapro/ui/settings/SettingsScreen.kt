package com.msa.qiblapro.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.msa.qiblapro.R
import com.msa.qiblapro.data.settings.NeonAccent
import com.msa.qiblapro.data.settings.ThemeMode
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.pro.ProBackground
import com.msa.qiblapro.util.haptics.Haptics

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
        onHapticStrength = vm::setHapticStrength,
        onHapticPattern = vm::setHapticPattern,
        onHapticCooldown = vm::setHapticCooldown,
        onSound = vm::setSound,
        onMapType = vm::setMapType,
        onShowIranCities = vm::setIranCities,
        onThemeMode = vm::setThemeMode,
        onAccent = vm::setAccent
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
    onHapticStrength: (Int) -> Unit,
    onHapticPattern: (Int) -> Unit,
    onHapticCooldown: (Long) -> Unit,
    onSound: (Boolean) -> Unit,
    onMapType: (Int) -> Unit,
    onShowIranCities: (Boolean) -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onAccent: (NeonAccent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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

            // --- Appearance ---
            AppCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.appearance_section_title)
                )

                Text(
                    text = stringResource(R.string.theme_mode_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        val label = when (mode) {
                            ThemeMode.SYSTEM -> stringResource(R.string.theme_mode_system)
                            ThemeMode.LIGHT -> stringResource(R.string.theme_mode_light)
                            ThemeMode.DARK -> stringResource(R.string.theme_mode_dark)
                        }
                        FilterChip(
                            selected = state.themeMode == mode,
                            onClick = { onThemeMode(mode) },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.accent_color_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    NeonAccent.entries.forEach { accent ->
                        val accentColor = when (accent) {
                            NeonAccent.GREEN -> Color(0xFF39FFB6)
                            NeonAccent.BLUE -> Color(0xFF00C2FF)
                            NeonAccent.PURPLE -> Color(0xFFBD00FF)
                            NeonAccent.PINK -> Color(0xFFFF00E5)
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                                .border(
                                    width = if (state.accent == accent) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable {
                                    onAccent(accent)
                                    Haptics.vibrate(context, state.hapticStrength, state.hapticPattern)
                                }
                        )
                    }
                }
            }

            // --- Compass ---
            AppCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    icon = Icons.Default.CompassCalibration,
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

            // --- Feedback / Haptics ---
            AppCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    icon = Icons.Default.Vibration,
                    title = stringResource(R.string.feedback_section_title)
                )

                ProSwitchRow(
                    title = stringResource(R.string.feedback_vibration_title),
                    subtitle = stringResource(R.string.feedback_vibration_desc),
                    checked = state.enableVibration,
                    onCheckedChange = onVibration
                )

                if (state.enableVibration) {
                    ProIntSliderRow(
                        title = stringResource(R.string.haptic_strength_title),
                        subtitle = "",
                        value = state.hapticStrength,
                        range = 1..3,
                        valueText = hapticStrengthLabel(state.hapticStrength),
                        onValueChange = {
                            onHapticStrength(it)
                            Haptics.vibrate(context, it, state.hapticPattern)
                        }
                    )

                    ProIntSliderRow(
                        title = stringResource(R.string.haptic_pattern_title),
                        subtitle = "",
                        value = state.hapticPattern,
                        range = 1..3,
                        valueText = hapticPatternLabel(state.hapticPattern),
                        onValueChange = {
                            onHapticPattern(it)
                            Haptics.vibrate(context, state.hapticStrength, it)
                        }
                    )

                    ProIntSliderRow(
                        title = "Cooldown", // Simplified, could use a string resource
                        subtitle = "Delay between vibrations",
                        value = (state.hapticCooldownMs / 100).toInt(),
                        range = 5..50,
                        valueText = "${state.hapticCooldownMs} ms",
                        onValueChange = { onHapticCooldown(it.toLong() * 100) }
                    )
                }

                ProSwitchRow(
                    title = stringResource(R.string.feedback_sound_title),
                    subtitle = stringResource(R.string.feedback_sound_desc),
                    checked = state.enableSound,
                    onCheckedChange = onSound
                )
            }

            // --- GPS / Location ---
            AppCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    icon = Icons.Default.GpsFixed,
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

            // --- Battery / Background ---
            AppCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    icon = Icons.Default.BatterySaver,
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

            // --- Map section ---
            AppCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    icon = Icons.Default.Map,
                    title = stringResource(R.string.map_section_title)
                )

                ProSwitchRow(
                    title = stringResource(R.string.show_iran_cities_title),
                    subtitle = stringResource(R.string.show_iran_cities_desc),
                    checked = state.showIranCities,
                    onCheckedChange = onShowIranCities
                )

                ProIntSliderRow(
                    title = stringResource(R.string.map_type_title),
                    subtitle = stringResource(R.string.map_type_desc),
                    value = state.mapType,
                    range = 1..4,
                    valueText = mapTypeLabel(state.mapType),
                    onValueChange = onMapType
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

/* ---------- UI pieces ---------- */

@Composable
private fun hapticStrengthLabel(strength: Int): String = when (strength) {
    1 -> stringResource(R.string.haptic_strength_low)
    3 -> stringResource(R.string.haptic_strength_high)
    else -> stringResource(R.string.haptic_strength_medium)
}

@Composable
private fun hapticPatternLabel(pattern: Int): String = when (pattern) {
    2 -> stringResource(R.string.haptic_pattern_double)
    3 -> stringResource(R.string.haptic_pattern_long)
    else -> stringResource(R.string.haptic_pattern_short)
}

@Composable
private fun mapTypeLabel(mapType: Int): String = when (mapType) {
    2 -> stringResource(R.string.map_type_satellite)
    3 -> stringResource(R.string.map_type_terrain)
    4 -> stringResource(R.string.map_type_hybrid)
    else -> stringResource(R.string.map_type_normal)
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
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
                if (subtitle.isNotEmpty()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
