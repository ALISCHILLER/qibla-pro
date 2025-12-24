package com.msa.qiblapro.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageHelper {

    /**
     * Normalize language tags to avoid mismatches:
     * "fa" or "fa-IR" -> "fa"
     * "en" or "en-US" -> "en"
     * you can keep full tags if you want, but then compare tags consistently.
     */
    fun normalizeLanguageTag(languageCode: String): String {
        val trimmed = languageCode.trim().lowercase()
        if (trimmed.isBlank()) return "en"
        // you can choose to keep region; here we keep only base language
        return trimmed.split("-", "_").first()
    }

    fun isRtlLanguage(languageCode: String): Boolean {
        return normalizeLanguageTag(languageCode) in setOf("fa", "ar", "ur")
    }

    /**
     * Apply language to the whole app (resources + formatting).
     * Note: Locale.setDefault helps number/date formatting.
     */
    fun applyLanguage(languageCode: String) {
        val tag = normalizeLanguageTag(languageCode)

        // 1) Default locale for formatting
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)

        // 2) AppCompat per-app language (Android 13- and also support lib)
        val appLocales = if (tag == "system") {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(tag)
        }
        AppCompatDelegate.setApplicationLocales(appLocales)
    }

    /**
     * Current language tag from AppCompat (per-app locales if set),
     * otherwise falls back to system default.
     */
    fun getCurrentLanguageTag(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        val tag = if (!locales.isEmpty) {
            locales[0]?.toLanguageTag()
        } else {
            Locale.getDefault().toLanguageTag()
        } ?: "en"

        return normalizeLanguageTag(tag)
    }

    fun getFlagEmoji(lang: String): String = when (normalizeLanguageTag(lang)) {
        "fa" -> "\uD83C\uDDEE\uD83C\uDDF7" // 🇮🇷
        "ar" -> "\uD83C\uDDF8\uD83C\uDDE6" // 🇸🇦
        "en" -> "\uD83C\uDDFA\uD83C\uDDF8" // 🇺🇸
        else -> "🌐"
    }
}
