package com.msa.qiblapro.ui.settings.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.settings.SettingsAction
import com.msa.qiblapro.ui.settings.SettingsUiState
import com.msa.qiblapro.ui.settings.components.ProIntSliderRow
import com.msa.qiblapro.ui.settings.components.ProSliderRow
import com.msa.qiblapro.ui.settings.components.ProSwitchRow
import com.msa.qiblapro.ui.settings.components.SectionHeader

@Composable
fun CompassSection(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            icon = Icons.Filled.CompassCalibration,
            title = stringResource(R.string.compass_settings)
        )

        ProSwitchRow(
            title = stringResource(R.string.use_true_north),
            subtitle = stringResource(R.string.true_north_desc),
            checked = state.useTrueNorth,
            onCheckedChange = { onAction(SettingsAction.SetUseTrueNorth(it)) }
        )

        ProSliderRow(
            title = stringResource(R.string.angle_smoothing),
            subtitle = stringResource(R.string.smoothing_desc),
            value = state.smoothing,
            range = 0f..1f,
            valueText = "${(state.smoothing * 100).toInt()}%",
            onValueChange = { onAction(SettingsAction.SetSmoothing(it)) }
        )

        ProIntSliderRow(
            title = stringResource(R.string.alignment_sensitivity),
            subtitle = stringResource(R.string.sensitivity_desc),
            value = state.alignmentToleranceDeg,
            range = 2..20,
            valueText = "${state.alignmentToleranceDeg}°",
            onValueChange = { onAction(SettingsAction.SetAlignmentTol(it)) }
        )

        HorizontalDivider(
            Modifier.padding(top = 10.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )

        ProSwitchRow(
            title = stringResource(R.string.auto_calibration),
            subtitle = stringResource(R.string.calibration_guide),
            checked = state.autoCalibration,
            onCheckedChange = { onAction(SettingsAction.SetAutoCalib(it)) }
        )

        ProIntSliderRow(
            title = stringResource(R.string.calibration_threshold_title),
            subtitle = stringResource(R.string.calibration_threshold_desc),
            value = state.calibrationThreshold,
            range = 1..10,
            valueText = state.calibrationThreshold.toString(),
            onValueChange = { onAction(SettingsAction.SetCalibThreshold(it)) }
        )
    }
}
