package com.msa.qiblapro.ui.map

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import kotlin.math.*

internal suspend fun fitBounds(
    cameraState: CameraPositionState,
    a: LatLng,
    b: LatLng,
    paddingPx: Int
) {
    val bounds = LatLngBounds.builder().include(a).include(b).build()
    cameraState.animate(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx))
}

internal fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

internal fun interpolateSpherical(a: LatLng, b: LatLng, t: Float): LatLng {
    val phi1 = Math.toRadians(a.latitude)
    val lambda1 = Math.toRadians(a.longitude)
    val phi2 = Math.toRadians(b.latitude)
    val lambda2 = Math.toRadians(b.longitude)

    val sinPhi1 = sin(phi1); val cosPhi1 = cos(phi1)
    val sinPhi2 = sin(phi2); val cosPhi2 = cos(phi2)

    val dLambda = lambda2 - lambda1
    val d = acos((sinPhi1 * sinPhi2) + (cosPhi1 * cosPhi2 * cos(dLambda))).coerceIn(0.0, Math.PI)
    if (d == 0.0) return a

    val A = sin((1 - t) * d) / sin(d)
    val B = sin(t * d) / sin(d)

    val x = A * cosPhi1 * cos(lambda1) + B * cosPhi2 * cos(lambda2)
    val y = A * cosPhi1 * sin(lambda1) + B * cosPhi2 * sin(lambda2)
    val z = A * sinPhi1 + B * sinPhi2

    val phiI = atan2(z, sqrt(x * x + y * y))
    val lambdaI = atan2(y, x)

    return LatLng(Math.toDegrees(phiI), Math.toDegrees(lambdaI))
}
