package com.msa.qiblapro.ui.compass.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.R

@Composable
fun CompassActionsRow(
    onCalibration: () -> Unit,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onCalibration,
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.calibration_title))
        }
        Button(
            onClick = onSettings,
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.settings))
        }
    }
}
