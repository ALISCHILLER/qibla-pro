package com.msa.qiblapro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.*
import com.msa.qiblapro.ui.viewmodels.QiblaUiState
import com.msa.qiblapro.ui.widgets.CompassRose

@Composable
fun ProCompassScreen(
    st: QiblaUiState,
    modifier: Modifier = Modifier
) {
    val prox = proximity01(
        rotationErrorDeg = st.rotationToQibla?.toFloat() ?: 0f,
        toleranceDeg = st.alignTolerance.toFloat()
    )
    val isFacing = st.facingQibla

    ProBackground(modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FacingGlowPill(
                text = if (isFacing) stringResource(id = R.string.facing_qibla) else stringResource(id = R.string.rotate_to_qibla, st.rotationToQibla?.toInt() ?: 0),
                isFacing = isFacing
            )

            Spacer(modifier = Modifier.weight(1f))

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .proShadow(),
                contentPadding = PaddingValues(0.dp)
            ) {
                val premiumNeedleMod = NeedlePremiumModifier(proximity = prox, isFacing = isFacing)
                CompassRose(
                    headingDeg = st.headingTrue?.toFloat() ?: 0f,
                    qiblaDeg = st.qiblaDeg?.toFloat() ?: 0f,
                    modifier = Modifier.fillMaxSize(),
                    needleModifier = premiumNeedleMod
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .proShadow(),
                contentPadding = PaddingValues(16.dp)
            ) {
                InfoRow(stringResource(id = R.string.qibla_direction), st.qiblaDeg?.let { "%.1f°".format(it) } ?: "--")
                InfoRow(stringResource(id = R.string.distance_to_kaaba), st.distanceKm?.let { "${it.toInt()} km" } ?: "--")
                InfoRow(stringResource(id = R.string.accuracy), st.accuracy?.let { "${it.toInt()} m" } ?: "--")
            }
        }
    }
}

@Composable
private fun InfoRow(title: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = Color.White.copy(alpha = 0.78f))
        Text(value, color = Color.White.copy(alpha = 0.96f))
    }
}
