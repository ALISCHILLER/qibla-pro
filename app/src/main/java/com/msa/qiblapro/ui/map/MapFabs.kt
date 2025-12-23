package com.msa.qiblapro.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.R

@Composable
internal fun BoxScope.MapFabs(
    onFit: () -> Unit,
    onCenterUser: () -> Unit
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FloatingActionButton(
            onClick = onCenterUser,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Icon(Icons.Default.NearMe, contentDescription = null)
        }

        FloatingActionButton(
            onClick = onFit,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Text(stringResource(R.string.map_fit))
        }
    }
}
