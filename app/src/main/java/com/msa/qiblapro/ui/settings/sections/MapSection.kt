package com.msa.qiblapro.ui.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.settings.SettingsAction
import com.msa.qiblapro.ui.settings.SettingsUiState
import com.msa.qiblapro.ui.settings.components.ProSwitchRow
import com.msa.qiblapro.ui.settings.components.SectionHeader

@Composable
fun MapSection(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        val cs = MaterialTheme.colorScheme
        SectionHeader(
            icon = Icons.Filled.Map,
            title = stringResource(R.string.map_section_title)
        )

        ProSwitchRow(
            title = stringResource(R.string.neon_map_style_title),
            subtitle = stringResource(R.string.neon_map_style_desc),
            checked = state.neonMapStyle,
            onCheckedChange = { onAction(SettingsAction.SetNeonMapStyle(it)) }
        )

        ProSwitchRow(
            title = stringResource(R.string.show_iran_cities_title),
            subtitle = stringResource(R.string.show_iran_cities_desc),
            checked = state.showIranCities,
            onCheckedChange = { onAction(SettingsAction.SetIranCities(it)) }
        )

        Text(
            text = stringResource(R.string.map_type_title),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.map_type_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val mapTypes = listOf(
                1 to stringResource(R.string.map_type_normal),
                2 to stringResource(R.string.map_type_satellite),
                3 to stringResource(R.string.map_type_terrain),
                4 to stringResource(R.string.map_type_hybrid)
            )
            mapTypes.forEach { (id, label) ->
                FilterChip(
                    selected = state.mapType == id,
                    onClick = { onAction(SettingsAction.SetMapType(id)) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = cs.surfaceVariant.copy(alpha = 0.35f),
                        selectedContainerColor = cs.surfaceVariant.copy(alpha = 0.7f),
                        labelColor = cs.onSurface,
                        selectedLabelColor = cs.onSurface
                    )
                )
            }
        }
    }
}