package com.msa.qiblapro.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.55f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
        }
    }
}
