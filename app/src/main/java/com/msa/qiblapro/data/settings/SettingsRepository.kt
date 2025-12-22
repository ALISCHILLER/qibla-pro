package com.msa.qiblapro.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore(name = "qibla_settings")

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

        val ENABLE_VIBRATION = booleanPreferencesKey("enable_vibration")
        val HAPTIC_STRENGTH = intPreferencesKey("haptic_strength")
        val HAPTIC_PATTERN = intPreferencesKey("haptic_pattern")
        val HAPTIC_COOLDOWN = longPreferencesKey("haptic_cooldown")
        
        val ENABLE_SOUND = booleanPreferencesKey("enable_sound")
        val MAP_TYPE = intPreferencesKey("map_type")
        val SHOW_IRAN_CITIES = booleanPreferencesKey("show_iran_cities")

        val NEON_MAP_STYLE = booleanPreferencesKey("neon_map_style")
        
        val THEME_MODE = intPreferencesKey("theme_mode")
        val ACCENT_TYPE = intPreferencesKey("accent_type")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
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
            hapticStrength = p[Keys.HAPTIC_STRENGTH] ?: 2,
            hapticPattern = p[Keys.HAPTIC_PATTERN] ?: 1,
            hapticCooldownMs = p[Keys.HAPTIC_COOLDOWN] ?: 1500L,
            enableSound = p[Keys.ENABLE_SOUND] ?: true,
            mapType = p[Keys.MAP_TYPE] ?: 1,
            showIranCities = p[Keys.SHOW_IRAN_CITIES] ?: true,
            neonMapStyle = p[Keys.NEON_MAP_STYLE] ?: true,
            themeMode = ThemeMode.entries.getOrElse(p[Keys.THEME_MODE] ?: 2) { ThemeMode.DARK },
            accent = NeonAccent.entries.getOrElse(p[Keys.ACCENT_TYPE] ?: 0) { NeonAccent.GREEN },
            hasSeenOnboarding = p[Keys.HAS_SEEN_ONBOARDING] ?: false
        )
    }

    suspend fun setUseTrueNorth(v: Boolean) = ctx.ds.edit { it[Keys.USE_TRUE_NORTH] = v }
    suspend fun setSmoothing(v: Float) = ctx.ds.edit { it[Keys.SMOOTHING] = v.coerceIn(0f, 1f) }
    suspend fun setAlignmentTolerance(v: Int) = ctx.ds.edit { it[Keys.ALIGN_TOL] = v.coerceIn(2, 20) }

    suspend fun setShowGpsPrompt(v: Boolean) = ctx.ds.edit { it[Keys.SHOW_GPS_PROMPT] = v }
    suspend fun setBatterySaver(v: Boolean) = ctx.ds.edit { it[Keys.BATTERY_SAVER] = v }
    suspend fun setBgFreqSec(v: Int) = ctx.ds.edit { it[Keys.BG_FREQ_SEC] = v.coerceIn(2, 30) }
    suspend fun setLowPowerLocation(v: Boolean) = ctx.ds.edit { it[Keys.LOW_POWER_LOC] = v }

    suspend fun setAutoCalibration(v: Boolean) = ctx.ds.edit { it[Keys.AUTO_CALIB] = v }
    suspend fun setCalibrationThreshold(v: Int) = ctx.ds.edit { it[Keys.CALIB_THRESHOLD] = v.coerceIn(1, 10) }

    suspend fun setVibration(v: Boolean) = ctx.ds.edit { it[Keys.ENABLE_VIBRATION] = v }
    suspend fun setHapticStrength(v: Int) = ctx.ds.edit { it[Keys.HAPTIC_STRENGTH] = v.coerceIn(1, 3) }
    suspend fun setHapticPattern(v: Int) = ctx.ds.edit { it[Keys.HAPTIC_PATTERN] = v.coerceIn(1, 3) }
    suspend fun setHapticCooldown(v: Long) = ctx.ds.edit { it[Keys.HAPTIC_COOLDOWN] = v }

    suspend fun setSound(v: Boolean) = ctx.ds.edit { it[Keys.ENABLE_SOUND] = v }

    suspend fun setMapType(v: Int) = ctx.ds.edit { it[Keys.MAP_TYPE] = v.coerceIn(1, 4) }
    suspend fun setShowIranCities(v: Boolean) = ctx.ds.edit { it[Keys.SHOW_IRAN_CITIES] = v }
    suspend fun setNeonMapStyle(v: Boolean) = ctx.ds.edit { it[Keys.NEON_MAP_STYLE] = v }
    
    suspend fun setThemeMode(mode: ThemeMode) = ctx.ds.edit { it[Keys.THEME_MODE] = mode.ordinal }
    suspend fun setAccent(accent: NeonAccent) = ctx.ds.edit { it[Keys.ACCENT_TYPE] = accent.ordinal }
    suspend fun setHasSeenOnboarding(v: Boolean) = ctx.ds.edit { it[Keys.HAS_SEEN_ONBOARDING] = v }
}
