package com.msa.qiblapro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.ProBackground
import com.msa.qiblapro.ui.viewmodels.QiblaViewModel
import androidx.hilt.navigation.compose.hiltViewModel

private val KAABA = LatLng(21.4225, 39.8262)

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    vm: QiblaViewModel = hiltViewModel()
) {
    val st by vm.state.collectAsState()
    val context = LocalContext.current

    val userLocation = st.lat?.let { lat -> st.lon?.let { lon -> LatLng(lat, lon) } }

    val qiblaIcon = remember {
        bitmapDescriptorFromVector(context, R.drawable.ic_qibla_direction)
    }

    val mapType = remember(st.mapType) {
        when (st.mapType) {
            1 -> MapType.NORMAL
            2 -> MapType.SATELLITE
            3 -> MapType.TERRAIN
            4 -> MapType.HYBRID
            else -> MapType.NORMAL
        }
    }

    val userMarkerState = remember(userLocation) {
        userLocation?.let { MarkerState(it) }
    }

    val kaabaMarkerState = remember {
        MarkerState(KAABA)
    }

    ProBackground(modifier.fillMaxSize()) {
        GoogleMap(
            modifier = modifier.fillMaxSize(),
            properties = remember {
                MapProperties(
                    isMyLocationEnabled = false,
                    mapType = mapType
                )
            },
            uiSettings = remember {
                MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = true
                )
            }
        ) {
            if (userMarkerState != null) {
                Marker(
                    state = userMarkerState,
                    title = "Qibla",
                    icon = qiblaIcon,
                    rotation = st.qiblaDeg,
                    flat = true
                )

                Marker(
                    state = kaabaMarkerState,
                    title = "Kaaba"
                )

                Polyline(
                    points = listOf(userLocation!!, KAABA),
                    geodesic = true,
                    width = 8f,
                    pattern = listOf(Dash(30f), Gap(10f))
                )
            }
        }
    }
}
