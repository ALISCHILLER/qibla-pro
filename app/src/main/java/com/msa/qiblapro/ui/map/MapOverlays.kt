package com.msa.qiblapro.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.msa.qiblapro.R

@Composable
internal fun MapOverlays(
    hasPermission: Boolean,
    gpsEnabled: Boolean,
    userLatLng: LatLng?
) {
    when {
        !hasPermission -> MapOverlay(
            title = stringResource(R.string.map_permission_title),
            body = stringResource(R.string.map_permission_body)
        )

        !gpsEnabled -> MapOverlay(
            title = stringResource(R.string.map_gps_off_title),
            body = stringResource(R.string.map_gps_off_body)
        )

        userLatLng == null -> MapOverlay(
            title = stringResource(R.string.map_waiting_title),
            body = stringResource(R.string.map_waiting_body)
        )
    }
}

@Composable
private fun MapOverlay(title: String, body: String) {
    val cs = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface.copy(alpha = 0.78f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        }
    }
}
