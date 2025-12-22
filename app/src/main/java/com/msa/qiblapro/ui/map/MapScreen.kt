package com.msa.qiblapro.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.ProBackground
import com.msa.qiblapro.ui.compass.QiblaViewModel
import androidx.hilt.navigation.compose.hiltViewModel

private val KAABA = LatLng(21.4225, 39.8262)



@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    vm: QiblaViewModel = hiltViewModel()
) {
    val st by vm.state.collectAsState()
    val context = LocalContext.current

    // کاربر مکان‌یابی شده؟
    val userLocation = st.lat?.let { lat -> st.lon?.let { lon -> LatLng(lat, lon) } }

    // وضعیت آیکون قبله (با تأخیر امن بارگذاری می‌شود)
    val qiblaIcon = remember { mutableStateOf<BitmapDescriptor?>(null) }

    LaunchedEffect(context) {
        // از کرش جلوگیری می‌کند چون ممکن است MapInitializer هنوز آماده نباشد
        qiblaIcon.value = bitmapDescriptorFromVector(context, R.drawable.ic_qibla_direction)
    }

    val mapType = when (st.mapType) {
        1 -> MapType.NORMAL
        2 -> MapType.SATELLITE
        3 -> MapType.TERRAIN
        4 -> MapType.HYBRID
        else -> MapType.NORMAL
    }

    val userMarkerState = remember(userLocation) {
        userLocation?.let { MarkerState(it) }
    }

    val kaabaMarkerState = remember { MarkerState(KAABA) }

    val mapProperties = remember(mapType) {
        MapProperties(
            isMyLocationEnabled = false,
            mapType = mapType
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = true
        )
    }

    ProBackground(modifier.fillMaxSize()) {
        GoogleMap(
            modifier = modifier.fillMaxSize(),
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            if (userMarkerState != null && userLocation != null && qiblaIcon.value != null) {
                // مارکر قبله
                Marker(
                    state = userMarkerState,
                    title = "Qibla",
                    icon = qiblaIcon.value,
                    rotation = st.qiblaDeg,
                    flat = true
                )

                // مارکر کعبه
                Marker(
                    state = kaabaMarkerState,
                    title = "Kaaba"
                )

                // خط قبله
                Polyline(
                    points = listOf(userLocation, KAABA),
                    geodesic = true,
                    width = 8f,
                    pattern = listOf(Dash(30f), Gap(10f))
                )
            }
        }
    }
}
