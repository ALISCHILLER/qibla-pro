package com.msa.qiblapro.ui.compass.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.msa.qiblapro.util.LanguageHelper

@Composable
fun CompassHeader(languageLabel: String, languageCode: String) {
    val currentFlag = LanguageHelper.getFlagEmoji(languageCode)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$languageLabel: ",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelMedium,
            fontSize = 14.sp
        )
        Text(text = currentFlag, fontSize = 18.sp)
    }
}
