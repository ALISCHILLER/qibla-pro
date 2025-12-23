package com.msa.qiblapro.ui.settings.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.settings.SettingsAction
import com.msa.qiblapro.ui.settings.components.SectionHeader
import com.msa.qiblapro.util.LanguageHelper

@Composable
fun LanguageSection(
    selectedLang: String,
    onAction: (SettingsAction) -> Unit
) {
    AppCard(Modifier.fillMaxWidth()) {
        SectionHeader(
            icon = Icons.Default.Language,
            title = stringResource(R.string.language_label)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val langs = listOf(
                "en" to R.string.language_en,
                "fa" to R.string.language_fa,
                "ar" to R.string.language_ar
            )

            langs.forEach { (code, labelRes) ->
                FilterChip(
                    selected = selectedLang == code,
                    onClick = { onAction(SettingsAction.SetLanguage(code)) },
                    label = { Text(stringResource(labelRes)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
