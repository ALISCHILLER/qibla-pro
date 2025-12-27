package com.msa.qiblapro.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.msa.qiblapro.util.LanguageHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

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

        val THEME_MODE_V2 = stringPreferencesKey("theme_mode_v2")
        val ACCENT_TYPE_V2 = stringPreferencesKey("accent_type_v2")

        // legacy keys to clean on write
        val LEGACY_THEME_MODE = stringPreferencesKey("theme_mode")
        val LEGACY_THEME_MODE_INT = intPreferencesKey("theme_mode")
        val LEGACY_ACCENT_TYPE = stringPreferencesKey("accent_type")
        val LEGACY_ACCENT_TYPE_INT = intPreferencesKey("accent_type")

        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
    }

    val settingsFlow: Flow<AppSettings> = dataStore.data.map { p ->
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

            themeMode = themeModeFromValue(p[Keys.THEME_MODE_V2]),
            accent = accentFromValue(p[Keys.ACCENT_TYPE_V2]),

            hasSeenOnboarding = p[Keys.HAS_SEEN_ONBOARDING] ?: false,
            languageCode = p[Keys.LANGUAGE_CODE] ?: "en"
        )
    }

    suspend fun setUseTrueNorth(v: Boolean) = dataStore.edit { it[Keys.USE_TRUE_NORTH] = v }
    suspend fun setSmoothing(v: Float) = dataStore.edit { it[Keys.SMOOTHING] = v.coerceIn(0f, 1f) }
    suspend fun setAlignmentTolerance(v: Int) = dataStore.edit { it[Keys.ALIGN_TOL] = v.coerceIn(2, 20) }

    suspend fun setShowGpsPrompt(v: Boolean) = dataStore.edit { it[Keys.SHOW_GPS_PROMPT] = v }
    suspend fun setBatterySaver(v: Boolean) = dataStore.edit { it[Keys.BATTERY_SAVER] = v }
    suspend fun setBgFreqSec(v: Int) = dataStore.edit { it[Keys.BG_FREQ_SEC] = v.coerceIn(2, 30) }
    suspend fun setLowPowerLocation(v: Boolean) = dataStore.edit { it[Keys.LOW_POWER_LOC] = v }

    suspend fun setAutoCalibration(v: Boolean) = dataStore.edit { it[Keys.AUTO_CALIB] = v }
    suspend fun setCalibrationThreshold(v: Int) = dataStore.edit { it[Keys.CALIB_THRESHOLD] = v.coerceIn(1, 10) }

    suspend fun setVibration(v: Boolean) = dataStore.edit { it[Keys.ENABLE_VIBRATION] = v }
    suspend fun setHapticStrength(v: Int) = dataStore.edit { it[Keys.HAPTIC_STRENGTH] = v.coerceIn(1, 3) }
    suspend fun setHapticPattern(v: Int) = dataStore.edit { it[Keys.HAPTIC_PATTERN] = v.coerceIn(1, 3) }
    suspend fun setHapticCooldown(v: Long) = dataStore.edit { it[Keys.HAPTIC_COOLDOWN] = v.coerceAtLeast(250L) }

    suspend fun setSound(v: Boolean) = dataStore.edit { it[Keys.ENABLE_SOUND] = v }

    suspend fun setMapType(v: Int) = dataStore.edit { it[Keys.MAP_TYPE] = v.coerceIn(1, 4) }
    suspend fun setShowIranCities(v: Boolean) = dataStore.edit { it[Keys.SHOW_IRAN_CITIES] = v }
    suspend fun setNeonMapStyle(v: Boolean) = dataStore.edit { it[Keys.NEON_MAP_STYLE] = v }

    suspend fun setThemeMode(mode: ThemeMode) = dataStore.edit {
        it[Keys.THEME_MODE_V2] = themeModeValue(mode)
        it.remove(Keys.LEGACY_THEME_MODE)
        it.remove(Keys.LEGACY_THEME_MODE_INT)
    }

    suspend fun setAccent(accent: NeonAccent) = dataStore.edit {
        it[Keys.ACCENT_TYPE_V2] = accentValue(accent)
        it.remove(Keys.LEGACY_ACCENT_TYPE)
        it.remove(Keys.LEGACY_ACCENT_TYPE_INT)
    }

    suspend fun setHasSeenOnboarding(v: Boolean) = dataStore.edit { it[Keys.HAS_SEEN_ONBOARDING] = v }

    suspend fun setLanguageCode(v: String) = dataStore.edit {
        it[Keys.LANGUAGE_CODE] = LanguageHelper.normalizeLanguageTag(v)
    }

    suspend fun resetToDefaults() {
        val defaults = AppSettings()
        dataStore.edit { prefs ->
            prefs.clear()
            prefs[Keys.USE_TRUE_NORTH] = defaults.useTrueNorth
            prefs[Keys.SMOOTHING] = defaults.smoothing
            prefs[Keys.ALIGN_TOL] = defaults.alignmentToleranceDeg

            prefs[Keys.SHOW_GPS_PROMPT] = defaults.showGpsPrompt
            prefs[Keys.BATTERY_SAVER] = defaults.batterySaverMode
            prefs[Keys.BG_FREQ_SEC] = defaults.bgUpdateFreqSec
            prefs[Keys.LOW_POWER_LOC] = defaults.useLowPowerLocation

            prefs[Keys.AUTO_CALIB] = defaults.autoCalibration
            prefs[Keys.CALIB_THRESHOLD] = defaults.calibrationThreshold

            prefs[Keys.ENABLE_VIBRATION] = defaults.enableVibration
            prefs[Keys.HAPTIC_STRENGTH] = defaults.hapticStrength
            prefs[Keys.HAPTIC_PATTERN] = defaults.hapticPattern
            prefs[Keys.HAPTIC_COOLDOWN] = defaults.hapticCooldownMs
            prefs[Keys.ENABLE_SOUND] = defaults.enableSound

            prefs[Keys.MAP_TYPE] = defaults.mapType
            prefs[Keys.SHOW_IRAN_CITIES] = defaults.showIranCities
            prefs[Keys.NEON_MAP_STYLE] = defaults.neonMapStyle

            prefs[Keys.THEME_MODE_V2] = themeModeValue(defaults.themeMode)
            prefs[Keys.ACCENT_TYPE_V2] = accentValue(defaults.accent)
            prefs[Keys.HAS_SEEN_ONBOARDING] = defaults.hasSeenOnboarding
            prefs[Keys.LANGUAGE_CODE] = LanguageHelper.normalizeLanguageTag(defaults.languageCode)
        }
    }

    private fun themeModeFromValue(value: String?): ThemeMode {
        return when (value) {
            "system" -> ThemeMode.SYSTEM
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.DARK
        }
    }

    private fun themeModeValue(mode: ThemeMode): String = when (mode) {
        ThemeMode.SYSTEM -> "system"
        ThemeMode.LIGHT -> "light"
        ThemeMode.DARK -> "dark"
    }

    private fun accentFromValue(value: String?): NeonAccent {
        return when (value) {
            "blue" -> NeonAccent.BLUE
            "purple" -> NeonAccent.PURPLE
            "pink" -> NeonAccent.PINK
            "green" -> NeonAccent.GREEN
            else -> NeonAccent.GREEN
        }
    }

    private fun accentValue(accent: NeonAccent): String = when (accent) {
        NeonAccent.GREEN -> "green"
        NeonAccent.BLUE -> "blue"
        NeonAccent.PURPLE -> "purple"
        NeonAccent.PINK -> "pink"
    }
}
