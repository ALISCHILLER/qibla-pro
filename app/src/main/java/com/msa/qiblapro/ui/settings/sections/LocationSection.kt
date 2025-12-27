package com.msa.qiblapro.ui.settings.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.settings.SettingsAction
import com.msa.qiblapro.ui.settings.SettingsUiState
import com.msa.qiblapro.ui.settings.components.ProIntSliderRow
import com.msa.qiblapro.ui.settings.components.ProSwitchRow
import com.msa.qiblapro.ui.settings.components.SectionHeader

@Composable
fun LocationSection(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            icon = Icons.Filled.GpsFixed,
            title = stringResource(R.string.gps_section)
        )

        ProSwitchRow(
            title = stringResource(R.string.gps_prompt_title),
            subtitle = stringResource(R.string.gps_prompt_desc),
            checked = state.showGpsPrompt,
            onCheckedChange = { onAction(SettingsAction.SetShowGpsPrompt(it)) }
        )

        ProSwitchRow(
            title = stringResource(R.string.low_power_location),
            subtitle = stringResource(R.string.low_power_location_desc),
            checked = state.useLowPowerLocation,
            onCheckedChange = { onAction(SettingsAction.SetLowPowerLoc(it)) }
        )
        ProSwitchRow(
            title = stringResource(R.string.battery_saver_mode),
            subtitle = stringResource(R.string.battery_saver_desc),
            checked = state.batterySaverMode,
            onCheckedChange = { onAction(SettingsAction.SetBatterySaver(it)) }
        )

        ProIntSliderRow(
            title = stringResource(R.string.bg_update_frequency),
            subtitle = stringResource(R.string.bg_update_frequency_desc),
            value = state.bgUpdateFreqSec,
            range = 2..30,
            valueText = "${state.bgUpdateFreqSec}s",
            onValueChange = { onAction(SettingsAction.SetBgUpdateFreq(it)) }
        )
    }
}
