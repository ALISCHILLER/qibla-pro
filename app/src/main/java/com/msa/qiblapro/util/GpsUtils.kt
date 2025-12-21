package com.msa.qiblapro.util

import android.content.Context
import android.location.LocationManager
import android.provider.Settings

object GpsUtils {

    /** آیا لوکیشن (GPS یا Network) روشن است؟ */
    fun isLocationEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false

        val gps = try {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (_: Exception) {
            false
        }

        val network = try {
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) {
            false
        }

        return gps || network
    }

    /** آیا حالت پرواز روشن است؟ */
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
}
