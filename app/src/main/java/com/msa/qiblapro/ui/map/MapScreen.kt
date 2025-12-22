package com.msa.qiblapro.ui.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.compass.QiblaViewModel
import com.msa.qiblapro.util.IranCities
import kotlinx.coroutines.launch
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val KAABA = LatLng(21.4225, 39.8262)

@Composable
fun MapScreen(vm: QiblaViewModel) {
    val st by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userLatLng = st.lat?.let { lat -> st.lon?.let { lon -> LatLng(lat, lon) } }

    val qiblaIcon = remember { mutableStateOf<BitmapDescriptor?>(null) }
    LaunchedEffect(context) {
        qiblaIcon.value = bitmapDescriptorFromVector(context, R.drawable.ic_qibla_direction)
    }

    val mapType = when (st.mapType) {
        1 -> MapType.NORMAL
        2 -> MapType.SATELLITE
        3 -> MapType.TERRAIN
        4 -> MapType.HYBRID
        else -> MapType.NORMAL
    }

    val mapStyle = remember(st.neonMapStyle) {
        if (st.neonMapStyle) MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_neon_night)
        else null
    }

    val cameraState = rememberCameraPositionState()
    var didSetInitialCamera by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(userLatLng) {
        if (!didSetInitialCamera) {
            didSetInitialCamera = true
            if (userLatLng != null) {
                fitBounds(cameraState, userLatLng, KAABA, paddingPx = 140)
            } else {
                cameraState.move(CameraUpdateFactory.newLatLngZoom(KAABA, 4.2f))
            }
        }
    }

    val props = remember(st.hasLocationPermission, mapType, mapStyle) {
        MapProperties(
            isMyLocationEnabled = st.hasLocationPermission,
            mapType = mapType,
            mapStyleOptions = mapStyle
        )
    }

    val ui = remember(st.hasLocationPermission) {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = st.hasLocationPermission,
            mapToolbarEnabled = true
        )
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            properties = props,
            uiSettings = ui
        ) {
            // کعبه همیشه باشد
            Marker(state = MarkerState(KAABA), title = stringResource(R.string.kaaba_title))

            // شهرهای ایران (اختیاری)
            if (st.showIranCities) {
                IranCities.cities.forEach { c ->
                    Marker(
                        state = MarkerState(LatLng(c.lat, c.lon)),
                        title = c.name,
                        alpha = 0.85f
                    )
                }
            }

            // مسیر/قبله وقتی موقعیت داریم
            if (userLatLng != null) {
                // خط ژئودزیک (انیمیشن‌دار)
                AnimatedGreatCirclePolyline(from = userLatLng, to = KAABA)

                // مارکر قبله روی کاربر (با آیکون)
                qiblaIcon.value?.let { icon ->
                    Marker(
                        state = MarkerState(userLatLng),
                        title = stringResource(R.string.qibla_title),
                        icon = icon,
                        rotation = st.qiblaDeg,
                        flat = true,
                        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f)
                    )
                }
            }
        }

        // چیپ‌های MapType
        MapTypeChips(
            selected = st.mapType,
            onSelect = { vm.setMapType(it) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp)
        )

        // Empty / Loading / GPS off overlays
        when {
            !st.hasLocationPermission -> {
                MapOverlay(
                    title = stringResource(R.string.map_permission_title),
                    body = stringResource(R.string.map_permission_body)
                )
            }
            userLatLng == null -> {
                MapOverlay(
                    title = stringResource(R.string.map_waiting_title),
                    body = stringResource(R.string.map_waiting_body)
                )
            }
            !st.gpsEnabled -> {
                MapOverlay(
                    title = stringResource(R.string.map_gps_off_title),
                    body = stringResource(R.string.map_gps_off_body)
                )
            }
        }

        // FAB برای Fit bounds
        if (userLatLng != null) {
            FloatingActionButton(
                onClick = { scope.launch { fitBounds(cameraState, userLatLng, KAABA, paddingPx = 140) } },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color.Black.copy(alpha = 0.55f)
            ) {
                Text(stringResource(R.string.map_fit), color = Color.White)
            }
        }
    }
}

@Composable
private fun MapTypeChips(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        1 to "Normal",
        2 to "Satellite",
        3 to "Terrain",
        4 to "Hybrid"
    )

    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.45f),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        LazyRow(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { (id, title) ->
                FilterChip(
                    selected = selected == id,
                    onClick = { onSelect(id) },
                    label = { Text(title, color = Color.White) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        selectedContainerColor = Color.White.copy(alpha = 0.20f),
                        labelColor = Color.White,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
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

private suspend fun fitBounds(
    cameraState: CameraPositionState,
    a: LatLng,
    b: LatLng,
    paddingPx: Int
) {
    val bounds = LatLngBounds.builder().include(a).include(b).build()
    cameraState.animate(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx))
}

@Composable
private fun AnimatedGreatCirclePolyline(from: LatLng, to: LatLng) {
    val anim = remember { Animatable(0f) }

    LaunchedEffect(from, to) {
        anim.snapTo(0f)
        anim.animateTo(1f, animationSpec = tween(durationMillis = 900))
    }

    val fraction = anim.value.coerceIn(0f, 1f)
    val mid = interpolateSpherical(from, to, fraction)

    Polyline(
        points = listOf(from, mid),
        geodesic = true,
        width = 8f,
        color = Color.White.copy(alpha = 0.9f),
        pattern = listOf(Dash(30f), Gap(10f))
    )
}

private fun interpolateSpherical(a: LatLng, b: LatLng, t: Float): LatLng {
    val φ1 = Math.toRadians(a.latitude)
    val λ1 = Math.toRadians(a.longitude)
    val φ2 = Math.toRadians(b.latitude)
    val λ2 = Math.toRadians(b.longitude)

    val sinφ1 = sin(φ1); val cosφ1 = cos(φ1)
    val sinφ2 = sin(φ2); val cosφ2 = cos(φ2)

    val Δλ = λ2 - λ1
    val d = acos((sinφ1 * sinφ2) + (cosφ1 * cosφ2 * cos(Δλ))).coerceIn(0.0, Math.PI)
    if (d == 0.0) return a

    val A = sin((1 - t) * d) / sin(d)
    val B = sin(t * d) / sin(d)

    val x = A * cosφ1 * cos(λ1) + B * cosφ2 * cos(λ2)
    val y = A * cosφ1 * sin(λ1) + B * cosφ2 * sin(λ2)
    val z = A * sinφ1 + B * sinφ2

    val φi = atan2(z, sqrt(x * x + y * y))
    val λi = atan2(y, x)

    return LatLng(Math.toDegrees(φi), Math.toDegrees(λi))
}
