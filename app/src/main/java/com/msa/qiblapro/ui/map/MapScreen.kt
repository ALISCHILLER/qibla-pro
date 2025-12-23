package com.msa.qiblapro.ui.map

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.msa.qiblapro.R
import com.msa.qiblapro.util.IranCities
import com.msa.qiblapro.util.IranCity
import kotlinx.coroutines.launch

@Composable
internal fun MapScreen(
    st: com.msa.qiblapro.ui.compass.QiblaUiState,
    onSetMapType: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userLatLng = remember(st.lat, st.lon) {
        if (st.lat != null && st.lon != null) LatLng(st.lat, st.lon) else null
    }

    val qiblaIcon = remember { mutableStateOf<BitmapDescriptor?>(null) }
    LaunchedEffect(Unit) {
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
    var didMoveToKaaba by rememberSaveable { mutableStateOf(false) }
    var didFitOnce by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(userLatLng) {
        if (userLatLng == null && !didMoveToKaaba) {
            didMoveToKaaba = true
            cameraState.move(CameraUpdateFactory.newLatLngZoom(KAABA_LATLNG, 4.2f))
        }
        if (userLatLng != null && !didFitOnce) {
            didFitOnce = true
            fitBounds(cameraState, userLatLng, KAABA_LATLNG, 140)
        }
    }

    val props = remember(st.hasLocationPermission, mapType, mapStyle) {
        MapProperties(
            isMyLocationEnabled = st.hasLocationPermission,
            mapType = mapType,
            mapStyleOptions = mapStyle
        )
    }

    val ui = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        )
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isNearbyMode by rememberSaveable { mutableStateOf(false) }

    val displayResults: List<Pair<IranCity, Double?>> = remember(searchQuery, isNearbyMode, userLatLng) {
        when {
            isNearbyMode && userLatLng != null -> {
                IranCities.cities
                    .map { it to haversineKm(userLatLng.latitude, userLatLng.longitude, it.lat, it.lon) }
                    .sortedBy { it.second }
                    .take(8)
            }
            searchQuery.isNotBlank() -> {
                IranCities.cities
                    .filter {
                        it.nameFa.contains(searchQuery, ignoreCase = true) ||
                                it.nameEn.contains(searchQuery, ignoreCase = true) ||
                                it.provinceFa.contains(searchQuery, ignoreCase = true)
                    }
                    .map { it to null }
                    .take(5)
            }
            else -> emptyList()
        }
    }

    Box(Modifier.fillMaxSize()) {
        MapMapContent(
            cameraState = cameraState,
            props = props,
            ui = ui,
            qiblaIcon = qiblaIcon,
            userLatLng = userLatLng,
            showIranCities = st.showIranCities,
            qiblaDeg = st.qiblaDeg,
            scope = scope
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp)
                .fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MapSearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    if (it.isNotBlank()) isNearbyMode = false
                },
                isNearbyActive = isNearbyMode,
                onNearbyToggle = {
                    isNearbyMode = !isNearbyMode
                    if (isNearbyMode) searchQuery = ""
                },
                results = displayResults,
                onCityClick = { city ->
                    searchQuery = ""
                    isNearbyMode = false
                    scope.launch {
                        cameraState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(city.lat, city.lon), 11f))
                    }
                },
                isLocationAvailable = userLatLng != null
            )

            if (searchQuery.isEmpty() && !isNearbyMode) {
                MapTypeChips(
                    selected = st.mapType,
                    onSelect = onSetMapType,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        MapOverlays(
            hasPermission = st.hasLocationPermission,
            gpsEnabled = st.gpsEnabled,
            userLatLng = userLatLng
        )

        if (userLatLng != null) {
            MapFabs(
                onFit = { scope.launch { fitBounds(cameraState, userLatLng, KAABA_LATLNG, 140) } },
                onCenterUser = { scope.launch { cameraState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 14.5f)) } }
            )
        }
    }
}
