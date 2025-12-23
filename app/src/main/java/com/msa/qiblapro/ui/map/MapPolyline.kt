package com.msa.qiblapro.ui.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Polyline

@Composable
internal fun AnimatedGreatCirclePolyline(from: LatLng, to: LatLng) {
    val anim = remember { Animatable(0f) }

    LaunchedEffect(from, to) {
        anim.snapTo(0f)
        anim.animateTo(1f, animationSpec = tween(durationMillis = 900))
    }

    val mid = interpolateSpherical(from, to, anim.value.coerceIn(0f, 1f))

    Polyline(
        points = listOf(from, mid),
        geodesic = true,
        width = 8f,
        color = Color.White.copy(alpha = 0.9f),
        pattern = listOf(Dash(30f), Gap(10f))
    )
}
