package com.msa.qiblapro.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.msa.qiblapro.data.settings.SettingsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

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
    private data class LocationConfig(
        val lowPower: Boolean,
        val intervalSec: Int
    )

    /**
     * ✅ ری‌اکتیو واقعی:
     * هر تغییر در Settings (low power / interval) => درخواست جدید به FusedLocation
     */
    fun locationFlow(): Flow<UserLocation> {
        return settingsRepo.settingsFlow
            .map { s ->
                LocationConfig(
                    lowPower = s.useLowPowerLocation,
                    intervalSec = s.bgUpdateFreqSec.coerceIn(2, 30)
                )
            }
            .distinctUntilChanged()
            .flatMapLatest { cfg -> requestLocationUpdates(cfg) }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates(cfg: LocationConfig): Flow<UserLocation> = callbackFlow {
        val priority = if (cfg.lowPower) {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        } else {
            Priority.PRIORITY_HIGH_ACCURACY
        }

        val intervalMs = cfg.intervalSec * 1000L

        val req = LocationRequest.Builder(priority, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs)
            .setWaitForAccurateLocation(!cfg.lowPower)
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
