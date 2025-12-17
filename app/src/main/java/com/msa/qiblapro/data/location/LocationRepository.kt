package com.msa.qiblapro.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.*
import com.msa.qiblapro.data.settings.SettingsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first

data class UserLocation(
    val lat: Double,
    val lon: Double,
    val alt: Float,
    val accuracyM: Float
)

class LocationRepository(
    private val ctx: Context,
    private val fused: FusedLocationProviderClient,
    private val settingsRepo: SettingsRepository
) {
    @SuppressLint("MissingPermission")
    fun locationFlow(): Flow<UserLocation> = callbackFlow {
        val s = settingsRepo.settingsFlow.first()

        val priority = if (s.useLowPowerLocation) {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        } else {
            Priority.PRIORITY_HIGH_ACCURACY
        }

        val intervalMs = (s.bgUpdateFreqSec.coerceIn(2, 30) * 1000L)

        val req = LocationRequest.Builder(priority, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs)
            .setWaitForAccurateLocation(!s.useLowPowerLocation)
            .build()

        val cb = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc: Location = result.lastLocation ?: return
                trySend(
                    UserLocation(
                        lat = loc.latitude,
                        lon = loc.longitude,
                        alt = loc.altitude.toFloat(),
                        accuracyM = loc.accuracy
                    )
                )
            }
        }

        fused.requestLocationUpdates(req, cb, ctx.mainLooper)
        awaitClose { fused.removeLocationUpdates(cb) }
    }
}
