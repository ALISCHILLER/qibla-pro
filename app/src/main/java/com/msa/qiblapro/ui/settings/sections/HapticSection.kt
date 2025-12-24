package com.msa.qiblapro.ui.settings.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.settings.model.IntSliderRow
import com.msa.qiblapro.ui.settings.model.SectionHeader
import com.msa.qiblapro.ui.settings.model.SettingCard
import com.msa.qiblapro.ui.settings.SettingsUiState
import com.msa.qiblapro.ui.settings.model.SwitchRow
import com.msa.qiblapro.util.haptics.Haptics

@Composable
fun HapticSection(
    state: SettingsUiState,
    onVibration: (Boolean) -> Unit,
    onHapticStrength: (Int) -> Unit,
    onHapticPattern: (Int) -> Unit,
    onHapticCooldown: (Long) -> Unit,
    onSound: (Boolean) -> Unit
) {
    val context = LocalContext.current

    SectionHeader(stringResource(R.string.feedback_section_title))

    SettingCard {
        SwitchRow(
            title = stringResource(R.string.feedback_vibration_title),
            desc = stringResource(R.string.feedback_vibration_desc),
            checked = state.enableVibration,
            onCheckedChange = onVibration
        )

        if (state.enableVibration) {
            IntSliderRow(
                title = stringResource(R.string.haptic_strength_title),
                desc = "",
                value = state.hapticStrength,
                valueRange = 1..3,
                label = { hapticStrengthLabel(it) },
                onValueChange = {
                    onHapticStrength(it)
                    Haptics.vibrate(context, it, state.hapticPattern)
                }
            )

            IntSliderRow(
                title = stringResource(R.string.haptic_pattern_title),
                desc = "",
                value = state.hapticPattern,
                valueRange = 1..3,
                label = { hapticPatternLabel(it) },
                onValueChange = {
                    onHapticPattern(it)
                    Haptics.vibrate(context, state.hapticStrength, it)
                }
            )

            IntSliderRow(
                title = stringResource(R.string.haptic_cooldown_title),
                desc = stringResource(R.string.haptic_cooldown_desc),
                value = (state.hapticCooldownMs / 100).toInt(),
                valueRange = 5..50,
                label = { "${it * 100} ms" },
                onValueChange = { onHapticCooldown(it.toLong() * 100) }
            )
        }

        SwitchRow(
            title = stringResource(R.string.feedback_sound_title),
            desc = stringResource(R.string.feedback_sound_desc),
            checked = state.enableSound,
            onCheckedChange = onSound
        )
    }
}

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
