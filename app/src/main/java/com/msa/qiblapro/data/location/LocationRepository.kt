package com.msa.qiblapro.data.location

import android.Manifest
import androidx.core.content.ContextCompat
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult as GmsLocationResult
import com.google.android.gms.location.Priority
import com.msa.qiblapro.data.settings.SettingsRepository
import com.msa.qiblapro.util.GpsUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive

data class UserLocation(
    val lat: Double,
    val lon: Double,
    val alt: Float,
    val accuracyM: Float
)

sealed interface UserLocationResult {
    data class Ok(val loc: UserLocation) : UserLocationResult
    data object PermissionDenied : UserLocationResult
    data object GpsDisabled : UserLocationResult
    data class Error(val t: Throwable) : UserLocationResult
}

class LocationRepository(
    private val ctx: Context,
    private val fused: FusedLocationProviderClient,
    private val settingsRepo: SettingsRepository
) {
    private data class LocationConfig(
        val lowPower: Boolean,
        val intervalSec: Int
    )

    fun locationFlow(): Flow<UserLocationResult> {
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

    private fun requestLocationUpdates(cfg: LocationConfig): Flow<UserLocationResult> = callbackFlow {
        // حلقه برای چک کردن مداوم وضعیت GPS وقتی غیرفعال است
        while (isActive) {
            val hasFine = ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            if (!hasFine && !hasCoarse) {
                trySend(UserLocationResult.PermissionDenied)
                delay(3000) // هر ۳ ثانیه چک کن
                continue
            }

            if (!GpsUtils.isLocationEnabled(ctx)) {
                trySend(UserLocationResult.GpsDisabled)
                delay(2000) // اگر GPS خاموش بود، هر ۲ ثانیه چک کن
                continue
            }

            // اگر همه چیز اوکی بود، از حلقه خارج شو و آپدیت‌ها را شروع کن
            break
        }

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
            override fun onLocationResult(result: GmsLocationResult) {
                val loc: Location = result.lastLocation ?: return
                trySend(
                    UserLocationResult.Ok(
                        UserLocation(
                            lat = loc.latitude,
                            lon = loc.longitude,
                            alt = loc.altitude.toFloat(),
                            accuracyM = loc.accuracy
                        )
                    )
                )
            }
        }

        try {
            fused.requestLocationUpdates(req, cb, ctx.mainLooper)
            awaitClose { fused.removeLocationUpdates(cb) }
        } catch (t: Throwable) {
            trySend(UserLocationResult.Error(t))
            close(t)
        }
    }
}
