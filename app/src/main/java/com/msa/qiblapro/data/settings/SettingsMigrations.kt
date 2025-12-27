package com.msa.qiblapro.data.settings

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Migration از کلیدهای legacy که int بودند و با key name قدیمی ذخیره شده‌اند،
 * به کلیدهای v2 که string هستند.
 */
class SettingsV1ToV2Migration : DataMigration<Preferences> {

    private val legacyTheme = intPreferencesKey("theme_mode")
    private val legacyAccent = intPreferencesKey("accent_type")
    private val legacyThemeString = stringPreferencesKey("theme_mode")
    private val legacyAccentString = stringPreferencesKey("accent_type")

    private val themeV2 = stringPreferencesKey("theme_mode_v2")
    private val accentV2 = stringPreferencesKey("accent_type_v2")

    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        val map = currentData.asMap()
        val hasLegacyTheme = map.keys.any { it.name == legacyTheme.name } && !currentData.contains(themeV2)
        val hasLegacyAccent = map.keys.any { it.name == legacyAccent.name } && !currentData.contains(accentV2)
        return hasLegacyTheme || hasLegacyAccent
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val map = currentData.asMap()
        val mutable = currentData.toMutablePreferences()

        val themeRaw = map.entries.firstOrNull { it.key.name == legacyTheme.name }?.value
        val themeString = when (themeRaw) {
            is String -> themeRaw
            is Int -> when (themeRaw) {
                0 -> "system"
                1 -> "light"
                else -> "dark"
            }
            else -> null
        }?.normalizeTheme()

        if (themeString != null && !currentData.contains(themeV2)) {
            mutable[themeV2] = themeString
        }

        val accentRaw = map.entries.firstOrNull { it.key.name == legacyAccent.name }?.value
        val accentString = when (accentRaw) {
            is String -> accentRaw
            is Int -> when (accentRaw) {
                0 -> "green"
                1 -> "blue"
                2 -> "purple"
                else -> "pink"
            }
            else -> null
        }?.normalizeAccent()

        if (accentString != null && !currentData.contains(accentV2)) {
            mutable[accentV2] = accentString
        }

        // پاکسازی کلیدهای legacy (هم int هم string با همان name)
        mutable.remove(legacyTheme)
        mutable.remove(legacyAccent)
        mutable.remove(legacyThemeString)
        mutable.remove(legacyAccentString)

        return mutable.toPreferences()
    }

    override suspend fun cleanUp() = Unit

    private fun String.normalizeTheme(): String = when (trim().lowercase()) {
        "system" -> "system"
        "light" -> "light"
        "dark" -> "dark"
        else -> "dark"
    }

    private fun String.normalizeAccent(): String = when (trim().lowercase()) {
        "green" -> "green"
        "blue" -> "blue"
        "purple" -> "purple"
        "pink" -> "pink"
        else -> "green"
    }
}
