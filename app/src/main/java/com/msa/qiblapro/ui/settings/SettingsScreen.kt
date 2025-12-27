package com.msa.qiblapro.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.pro.ProBackground
import com.msa.qiblapro.ui.settings.sections.*

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onOpenAbout: () -> Unit // ✅ اضافه شده برای هندل کردن کلیک روی "درباره ما"
) {

    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_dialog_title)) },
            text = { Text(stringResource(R.string.reset_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(SettingsAction.ResetToDefaults)
                        showResetDialog = false
                    }
                ) { Text(stringResource(R.string.reset)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

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

            AppearanceSection(state = state, onAction = onAction)

            LanguageSection(
                selectedLang = state.languageCode,
                onAction = onAction
            )

            CompassSection(state = state, onAction = onAction)

            HapticSection(
                state = state,
                onVibration = { onAction(SettingsAction.SetVibration(it)) },
                onHapticStrength = { onAction(SettingsAction.SetHapticStrength(it)) },
                onHapticPattern = { onAction(SettingsAction.SetHapticPattern(it)) },
                onHapticCooldown = { onAction(SettingsAction.SetHapticCooldown(it)) },
                onSound = { onAction(SettingsAction.SetSound(it)) }
            )

            LocationSection(state = state, onAction = onAction)
            MapSection(state = state, onAction = onAction)

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.advanced_actions),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(stringResource(R.string.reset_all_settings))
                }
            }

            // ✅ بخش درباره ما
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAbout() } // ✅ فراخوانی مستقیم تابع
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
