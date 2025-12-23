package com.msa.qiblapro.ui.settings.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.R
import com.msa.qiblapro.data.settings.NeonAccent
import com.msa.qiblapro.data.settings.ThemeMode
import com.msa.qiblapro.ui.settings.SectionHeader
import com.msa.qiblapro.ui.settings.SettingCard
import com.msa.qiblapro.ui.settings.SettingsUiState
import com.msa.qiblapro.util.haptics.Haptics

@Composable
fun AppearanceSection(
    state: SettingsUiState,
    onThemeMode: (ThemeMode) -> Unit,
    onAccent: (NeonAccent) -> Unit
) {
    val context = LocalContext.current

    SectionHeader(stringResource(R.string.appearance_section_title))

    SettingCard {
        Text(
            text = stringResource(R.string.theme_mode_title),
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
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
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .border(
                            width = if (state.accent == accent) 2.dp else 0.dp,
                            color = MaterialTheme.colorScheme.onSurface,
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
}
