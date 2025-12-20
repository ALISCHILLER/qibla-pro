package com.msa.qiblapro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

                HorizontalDivider(Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                ProSwitchRow(
                    title = "کالیبراسیون خودکار",
                    subtitle = "نمایش راهنما هنگام خطای سنسور",
                    checked = state.autoCalibration,
                    onCheckedChange = onAutoCalibration
                )

                ProIntSliderRow(
                    title = "آستانه کالیبراسیون",
                    subtitle = "تعداد دفعات خطا قبل از هشدار",
                    value = state.calibrationThreshold,
                    range = 1..10,
                    valueText = state.calibrationThreshold.toString()
                ) { onCalibrationThreshold(it) }
            }

            // --- GPS ---
            GlassCard(modifier = Modifier.fillMaxWidth().proShadow()) {
                SectionHeader(icon = Icons.Filled.GpsFixed, title = "موقعیت")

                ProSwitchRow(
                    title = "هشدار فعال‌سازی GPS",
                    subtitle = "نمایش دیالوگ هنگام خاموش بودن مکان",
                    checked = state.showGpsPrompt,
                    onCheckedChange = onShowGpsPrompt
                )

                ProSwitchRow(
                    title = "موقعیت کم‌مصرف",
                    subtitle = "بهینه‌سازی برای مصرف باتری کمتر",
                    checked = state.useLowPowerLocation,
                    onCheckedChange = onLowPowerLocation
                )
            }

            // --- Battery ---
            GlassCard(modifier = Modifier.fillMaxWidth().proShadow()) {
                SectionHeader(icon = Icons.Filled.BatterySaver, title = stringResource(R.string.battery_saver))

                ProSwitchRow(
                    title = "حالت ذخیره باتری",
                    subtitle = "کاهش نرخ بروزرسانی در پس‌زمینه",
                    checked = state.batterySaverMode,
                    onCheckedChange = onBatterySaver
                )

                if (state.batterySaverMode) {
                    ProIntSliderRow(
                        title = "فاصله بروزرسانی پس‌زمینه",
                        subtitle = "نرخ آپدیت زمانی که برنامه باز نیست",
                        value = state.bgUpdateFreqSec,
                        range = 2..30,
                        valueText = "${state.bgUpdateFreqSec} ثانیه"
                    ) { onBgUpdateFreqSec(it) }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

/* ---------- UI pieces ---------- */

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
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
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(valueText, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(valueText, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt().coerceIn(range.first, range.last)) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = (range.last - range.first - 1).coerceAtLeast(0)
        )
    }
    Spacer(Modifier.height(6.dp))
}
