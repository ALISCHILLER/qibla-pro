package com.msa.qiblapro.ui.map

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.clustering.Clustering
import com.msa.qiblapro.R
import com.msa.qiblapro.util.IranCities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@Composable
internal fun MapMapContent(
    cameraState: com.google.maps.android.compose.CameraPositionState,
    props: MapProperties,
    ui: MapUiSettings,
    qiblaIcon: State<BitmapDescriptor?>,
    userLatLng: LatLng?,
    showIranCities: Boolean,
    qiblaDeg: Float,
    scope: CoroutineScope,
    mapConfig: MapConfig
) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraState,
        properties = props,
        uiSettings = ui,
        onMapLoaded = { Log.d(MAP_LOG_TAG, "✅ onMapLoaded") }
    ) {
        Marker(state = MarkerState(KAABA_LATLNG), title = stringResource(R.string.kaaba_title))

        val enableIranCities =
            showIranCities &&
                    cameraState.position.zoom >= mapConfig.iranCitiesMinZoom

        if (enableIranCities) {
            val items = remember(mapConfig.maxIranCityMarkers) {
                IranCities.cities
                    .take(mapConfig.maxIranCityMarkers)
                    .map { CityClusterItem(it) }
            }

            if (mapConfig.clusteringEnabled) {
                Clustering(
                    items = items,
                    onClusterClick = { cluster ->
                        scope.launch {
                            val builder = LatLngBounds.builder()
                            cluster.items.forEach { builder.include(it.position) }
                            cameraState.animate(CameraUpdateFactory.newLatLngBounds(builder.build(), 120))
                        }
                        true
                    }
                )
            } else {
                items.forEach { item ->
                    Marker(
                        state = MarkerState(item.position),
                        title = item.title
                    )
                }
            }
        }

        if (userLatLng != null) {
            AnimatedGreatCirclePolyline(from = userLatLng, to = KAABA_LATLNG)
            qiblaIcon.value?.let { icon ->
                Marker(
                    state = MarkerState(userLatLng),
                    title = stringResource(R.string.qibla_title),
                    icon = icon,
                    rotation = qiblaDeg,
                    flat = true,
                    anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f)
                )
            }
        }
    }
}
