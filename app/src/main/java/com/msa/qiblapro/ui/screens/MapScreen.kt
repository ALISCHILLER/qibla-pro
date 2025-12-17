package com.msa.qiblapro.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.ProBackground
import com.msa.qiblapro.ui.viewmodels.QiblaViewModel

private val KAABA = LatLng(21.4225, 39.8262)

@Composable
fun MapScreen(vm: QiblaViewModel = hiltViewModel()) {
    val st by vm.state.collectAsState()
    val context = LocalContext.current

    val user = st.lat?.let { lat -> st.lon?.let { lon -> LatLng(lat, lon) } }

    val qiblaIcon = remember {
        bitmapDescriptorFromVector(context, R.drawable.ic_qibla_direction)
    }

    ProBackground(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            if (user != null) {
                Marker(
                    state = MarkerState(user),
                    title = "Qibla",
                    icon = qiblaIcon,
                    rotation = st.qiblaDeg,
                    flat = true
                )
                Marker(state = MarkerState(KAABA), title = "Kaaba")

                Polyline(
                    points = listOf(user, KAABA),
                    geodesic = true,
                    width = 8f,
                    pattern = listOf(Dash(30f), Gap(10f))
                )
            }
        }
    }
}
