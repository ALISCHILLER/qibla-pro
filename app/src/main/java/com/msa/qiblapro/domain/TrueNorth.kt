package com.msa.qiblapro.domain

import android.hardware.GeomagneticField

object TrueNorth {
    fun applyDeclination(magneticAzimuthDeg: Double, lat: Double, lon: Double, altitudeMeters: Double = 0.0): Double {
        val geo = GeomagneticField(
            lat.toFloat(),
            lon.toFloat(),
            altitudeMeters.toFloat(),
            System.currentTimeMillis()
        )
        val trueAz = magneticAzimuthDeg + geo.declination
        var v = trueAz % 360.0
        if (v < 0) v += 360.0
        return v
    }
}
