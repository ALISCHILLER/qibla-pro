package com.msa.qiblapro.ui.map

import android.app.ActivityManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class DeviceTier { LOW, MID, HIGH }

data class MapConfig(
    val clusteringEnabled: Boolean,
    val iranCitiesMinZoom: Float,
    val maxIranCityMarkers: Int
)

@Singleton
class MapPerformanceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getConfig(): MapConfig {
        val tier = detectDeviceTier()
        return when (tier) {
            DeviceTier.LOW -> MapConfig(
                clusteringEnabled = false,
                iranCitiesMinZoom = 7.5f,
                maxIranCityMarkers = 15
            )
            DeviceTier.MID -> MapConfig(
                clusteringEnabled = true,
                iranCitiesMinZoom = 6.5f,
                maxIranCityMarkers = 30
            )
            DeviceTier.HIGH -> MapConfig(
                clusteringEnabled = true,
                iranCitiesMinZoom = 5.5f,
                maxIranCityMarkers = 60
            )
        }
    }

    private fun detectDeviceTier(): DeviceTier {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mem = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mem)

        return when {
            mem.totalMem < 3_000_000_000L -> DeviceTier.LOW
            mem.totalMem < 6_000_000_000L -> DeviceTier.MID
            else -> DeviceTier.HIGH
        }
    }
}
