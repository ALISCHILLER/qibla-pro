package com.msa.qiblapro.ui.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.msa.qiblapro.util.IranCity

internal const val MAP_LOG_TAG = "MapScreen"
internal val KAABA_LATLNG = LatLng(21.4225, 39.8262)

internal data class CityClusterItem(val city: IranCity) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(city.lat, city.lon)
    override fun getTitle(): String = city.nameFa
    override fun getSnippet(): String = city.provinceFa
    override fun getZIndex(): Float? = null
}
