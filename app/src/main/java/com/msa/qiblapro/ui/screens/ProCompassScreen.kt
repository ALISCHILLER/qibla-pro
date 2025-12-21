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
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.pro.FacingGlowPill
import com.msa.qiblapro.ui.pro.NeedlePremiumModifier
import com.msa.qiblapro.ui.pro.ProBackground
import com.msa.qiblapro.ui.pro.proximity01
import com.msa.qiblapro.ui.uiState.QiblaUiState
import com.msa.qiblapro.ui.widgets.CompassRose
import com.msa.qiblapro.ui.widgets.LuxNeedle

@Composable
fun ProCompassScreen(
    st: QiblaUiState,
    modifier: Modifier = Modifier
) {
    val rotation = st.rotationToQibla ?: 0f
    val tolerance = st.alignTolerance.toFloat().coerceAtLeast(1f)

    val prox = proximity01(
        rotationErrorDeg = rotation,
        toleranceDeg = tolerance
    )
    val isFacing = st.facingQibla

    ProBackground(modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FacingGlowPill(
                text = if (isFacing)
                    stringResource(id = R.string.facing_qibla)
                else
                    stringResource(id = R.string.rotate_to_qibla, rotation.toInt()),
                isFacing = isFacing,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CompassRose(
                        headingDeg = st.headingTrue ?: st.headingMagDeg,
                        qiblaDeg = st.qiblaDeg,
                        isFacingQibla = isFacing,
                        modifier = Modifier.fillMaxSize()
                    )

                    val premiumNeedleMod = NeedlePremiumModifier(
                        proximity = prox,
                        isFacing = isFacing
                    )

                    LuxNeedle(
                        rotationDeg = -rotation,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(premiumNeedleMod)
                    )
                }
            }

            AppCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                InfoRowPro(
                    title = stringResource(id = R.string.qibla_direction),
                    value = "%.1f°".format(st.qiblaDeg)
                )
                InfoRowPro(
                    title = stringResource(id = R.string.distance_to_kaaba),
                    value = "${st.distanceKm.toInt()} km"
                )
                InfoRowPro(
                    title = stringResource(id = R.string.accuracy),
                    value = st.accuracyM?.let { "${it.toInt()} m" } ?: "--"
                )
            }
        }
    }
}

@Composable
fun InfoRowPro(title: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = Color.White.copy(alpha = 0.78f))
        Text(value, color = Color.White.copy(alpha = 0.96f))
    }
}
