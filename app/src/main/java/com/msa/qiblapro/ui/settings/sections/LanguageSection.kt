package com.msa.qiblapro.ui.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.settings.SettingsAction
import com.msa.qiblapro.ui.settings.components.SectionHeader

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

        Spacer(Modifier.height(8.dp))

        val langs = listOf(
            "en" to R.string.language_en,
            "fa" to R.string.language_fa,
            "ar" to R.string.language_ar
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            langs.forEach { (code, labelRes) ->
                FilterChip(
                    selected = selectedLang == code,
                    onClick = {
                        // ✅ اکشن واحد و درست
                        onAction(SettingsAction.SetLanguage(code))
                    },
                    label = { Text(stringResource(id = labelRes)) },
                    modifier = Modifier.defaultMinSize(minWidth = 90.dp)
                )
            }
        }
    }
}
