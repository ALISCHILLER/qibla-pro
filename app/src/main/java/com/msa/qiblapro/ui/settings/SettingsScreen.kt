package com.msa.qiblapro.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.pro.ProBackground
import com.msa.qiblapro.ui.settings.sections.AppearanceSection
import com.msa.qiblapro.ui.settings.sections.CompassSection
import com.msa.qiblapro.ui.settings.sections.HapticSection
import com.msa.qiblapro.ui.settings.sections.LanguageSection
import com.msa.qiblapro.ui.settings.sections.LocationSection

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit
) {
    ProBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            AppearanceSection(
                state = state,
                onAction = onAction
            )

            LanguageSection(
                selectedLang = state.languageCode,
                onAction = onAction
            )

            CompassSection(
                state = state,
                onAction = onAction
            )

            // این HapticSection شما callback-based هست، پس اینجا مستقیم پاس می‌دیم
            HapticSection(
                state = state,
                onVibration = { onAction(SettingsAction.SetVibration(it)) },
                onHapticStrength = { onAction(SettingsAction.SetHapticStrength(it)) },
                onHapticPattern = { onAction(SettingsAction.SetHapticPattern(it)) },
                onHapticCooldown = { onAction(SettingsAction.SetHapticCooldown(it)) },
                onSound = { onAction(SettingsAction.SetSound(it)) }
            )

            LocationSection(
                state = state,
                onAction = onAction
            )

            AppCard(modifier = Modifier) {
                // About row
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.about_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 2.dp)
                    )
                }

                // کل کارت کلیک‌پذیر:
                // اگر AppCard شما clickable دارد، اینجا بهش بده.
                // اگر ندارد، ساده‌ترین راه این است:
                // (اگر لازم داشتی کد clickable رو هم می‌دم)
                // فعلاً با Action:
                androidx.compose.runtime.LaunchedEffect(Unit) { /* no-op */ }
            }

            // ساده‌تر: به جای کارت بالا، همین دو خط رو بذار اگر AppCard clickable داری:
            // AppCard(Modifier.fillMaxWidth().clickable { onAction(SettingsAction.OpenAbout) }) { ... }

            Spacer(Modifier.height(24.dp))
        }
    }
}
