package com.msa.qiblapro.data.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore(name = "qibla_settings")

data class AppSettings(
    val useTrueNorth: Boolean = true,
    val smoothing: Float = 0.65f,
    val alignmentToleranceDeg: Int = 6,
    val showGpsPrompt: Boolean = true,
    val batterySaverMode: Boolean = false,
    val bgUpdateFreqSec: Int = 5,
    val useLowPowerLocation: Boolean = true,
    val autoCalibration: Boolean = true,
    val calibrationThreshold: Int = 3,

    val enableVibration: Boolean = true,
    val enableSound: Boolean = true,
    val mapType: Int = 1,
    val showIranCities: Boolean = true
)

class SettingsRepository(private val ctx: Context) {

    private object Keys {
        val USE_TRUE_NORTH = booleanPreferencesKey("use_true_north")
        val SMOOTHING = floatPreferencesKey("smoothing")
        val ALIGN_TOL = intPreferencesKey("align_tol")

        val SHOW_GPS_PROMPT = booleanPreferencesKey("show_gps_prompt")
        val BATTERY_SAVER = booleanPreferencesKey("battery_saver")
        val BG_FREQ_SEC = intPreferencesKey("bg_freq_sec")
        val LOW_POWER_LOC = booleanPreferencesKey("low_power_loc")

        val AUTO_CALIB = booleanPreferencesKey("auto_calib")
        val CALIB_THRESHOLD = intPreferencesKey("calib_threshold")

        // ✅ کلیدهای جدید
        val ENABLE_VIBRATION = booleanPreferencesKey("enable_vibration")
        val ENABLE_SOUND = booleanPreferencesKey("enable_sound")
        val MAP_TYPE = intPreferencesKey("map_type")
        val SHOW_IRAN_CITIES = booleanPreferencesKey("show_iran_cities")
    }

    val settingsFlow: Flow<AppSettings> = ctx.ds.data.map { p ->
        AppSettings(
            useTrueNorth = p[Keys.USE_TRUE_NORTH] ?: true,
            smoothing = p[Keys.SMOOTHING] ?: 0.65f,
            alignmentToleranceDeg = p[Keys.ALIGN_TOL] ?: 6,

            showGpsPrompt = p[Keys.SHOW_GPS_PROMPT] ?: true,
            batterySaverMode = p[Keys.BATTERY_SAVER] ?: false,
            bgUpdateFreqSec = p[Keys.BG_FREQ_SEC] ?: 5,
            useLowPowerLocation = p[Keys.LOW_POWER_LOC] ?: true,

            autoCalibration = p[Keys.AUTO_CALIB] ?: true,
            calibrationThreshold = p[Keys.CALIB_THRESHOLD] ?: 3,

            enableVibration = p[Keys.ENABLE_VIBRATION] ?: true,
            enableSound = p[Keys.ENABLE_SOUND] ?: true,
            mapType = p[Keys.MAP_TYPE] ?: 1,
            showIranCities = p[Keys.SHOW_IRAN_CITIES] ?: true
        )
    }

    // ✅ Setterهای موجود
    suspend fun setUseTrueNorth(v: Boolean) = ctx.ds.edit { it[Keys.USE_TRUE_NORTH] = v }
    suspend fun setSmoothing(v: Float) = ctx.ds.edit { it[Keys.SMOOTHING] = v.coerceIn(0f, 1f) }
    suspend fun setAlignmentTolerance(v: Int) = ctx.ds.edit { it[Keys.ALIGN_TOL] = v.coerceIn(2, 20) }

    suspend fun setShowGpsPrompt(v: Boolean) = ctx.ds.edit { it[Keys.SHOW_GPS_PROMPT] = v }
    suspend fun setBatterySaver(v: Boolean) = ctx.ds.edit { it[Keys.BATTERY_SAVER] = v }
    suspend fun setBgFreqSec(v: Int) = ctx.ds.edit { it[Keys.BG_FREQ_SEC] = v.coerceIn(2, 30) }
    suspend fun setLowPowerLocation(v: Boolean) = ctx.ds.edit { it[Keys.LOW_POWER_LOC] = v }

    suspend fun setAutoCalibration(v: Boolean) = ctx.ds.edit { it[Keys.AUTO_CALIB] = v }
    suspend fun setCalibrationThreshold(v: Int) = ctx.ds.edit { it[Keys.CALIB_THRESHOLD] = v.coerceIn(1, 10) }

    // ✅ Setterهای جدید
    suspend fun setVibration(v: Boolean) = ctx.ds.edit { it[Keys.ENABLE_VIBRATION] = v }
    suspend fun setSound(v: Boolean) = ctx.ds.edit { it[Keys.ENABLE_SOUND] = v }
    suspend fun setMapType(v: Int) = ctx.ds.edit { it[Keys.MAP_TYPE] = v }
    suspend fun setShowIranCities(v: Boolean) = ctx.ds.edit { it[Keys.SHOW_IRAN_CITIES] = v }
}
