package com.msa.qiblapro.domain.qibla

import android.hardware.GeomagneticField
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object QiblaMath {

    // Kaaba
    private const val KAABA_LAT = 21.4225
    private const val KAABA_LON = 39.8262

    fun bearingToKaaba(lat: Double, lon: Double): Double {
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(KAABA_LAT)
        val dLambda = Math.toRadians(KAABA_LON - lon)

        val y = sin(dLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dLambda)
        val brng = atan2(y, x)
        return (Math.toDegrees(brng) + 360.0) % 360.0
    }

    fun distanceKmToKaaba(lat: Double, lon: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(KAABA_LAT - lat)
        val dLon = Math.toRadians(KAABA_LON - lon)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat)) * cos(Math.toRadians(KAABA_LAT)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    // declination degrees (magnetic -> true)
    fun declinationDeg(lat: Double, lon: Double, altMeters: Float, timeMs: Long): Float {
        val field = GeomagneticField(lat.toFloat(), lon.toFloat(), altMeters, timeMs)
        return field.declination
    }

    // rotation error: how much user must rotate to align ([-180..180])
    fun rotationErrorDeg(currentHeading: Float, targetBearing: Float): Float {
        val diff = ((targetBearing - currentHeading + 540f) % 360f) - 180f
        return diff
    }
}