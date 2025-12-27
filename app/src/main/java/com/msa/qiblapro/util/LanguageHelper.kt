package com.msa.qiblapro.util

import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageHelper {

    /**
     * نرمال‌سازی تگ زبان:
     * - "system" نگه داشته می‌شود
     * - "fa-IR" -> "fa"
     * - "ar-SA" -> "ar"
     */
    fun normalizeLanguageTag(input: String?): String {
        val raw = input?.trim()?.lowercase().orEmpty()
        if (raw.isBlank() || raw == "system") return "system"

        val base = raw
            .replace('_', '-')
            .split('-')
            .firstOrNull()
            .orEmpty()

        return when (base) {
            "en" -> "en"
            "fa" -> "fa"
            "ar" -> "ar"
            else -> "en"
        }
    }

    /**
     * زبان فعلی اپ
     */
    fun getCurrentLanguageTag(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return "system"
        
        val tag = locales.toLanguageTags()
        return normalizeLanguageTag(tag)
    }

    /**
     * اعمال زبان
     */
    fun applyLanguage(tag: String) {
        val normalized = normalizeLanguageTag(tag)
        if (normalized == "system") {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(normalized))
        }
    }

    /**
     * تشخیص RTL
     */
    fun isRtlLanguage(tag: String): Boolean {
        val normalized = normalizeLanguageTag(tag)
        val locale = if (normalized == "system") {
            Locale.getDefault()
        } else {
            Locale.forLanguageTag(normalized)
        }
        return TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL
    }

    /**
     * ایموجی پرچم بر اساس زبان:
     * - fa => 🇮🇷 (ایران)
     * - en => 🇬🇧 (انگلیس)
     * - ar => 🇸🇦 (عربستان)
     */
    fun getFlagEmoji(tag: String): String {
        val normalized = normalizeLanguageTag(tag)

        return when (normalized) {
            "fa" -> "🇮🇷"
            "en" -> "🇬🇧"
            "ar" -> "🇸🇦"
            "system" -> {
                when (Locale.getDefault().language.lowercase()) {
                    "fa" -> "🇮🇷"
                    "ar" -> "🇸🇦"
                    else -> "🇬🇧" // پیش‌فرض برای انگلیسی یا سایر
                }
            }
            else -> "🇬🇧"
        }
    }
}
