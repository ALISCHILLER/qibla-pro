package com.msa.qiblapro.util

import android.content.Context
import android.location.LocationManager
import android.provider.Settings

object GpsUtils {

    fun isLocationEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gps = try { lm.isProviderEnabled(LocationManager.GPS_PROVIDER) } catch (_: Exception) { false }
        val network = try { lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { false }
        return gps || network
    }

    fun isAirplaneModeOn(context: Context): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                0
            ) == 1
        } catch (_: Exception) {
            false
        }
    }

    fun openLocationSettings(context: Context) {
        try {
            context.startActivity(
                android.content.Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (_: Exception) {}
    }
}
