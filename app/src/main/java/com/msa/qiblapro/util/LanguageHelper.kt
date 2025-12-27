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
     * - اگر ناشناخته بود -> "en"
     */
    fun normalizeLanguageTag(input: String?): String {
        val raw = input?.trim()?.lowercase().orEmpty()
        if (raw.isBlank()) return "en"
        if (raw == "system") return "system"

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
     * زبان فعلی اپ (همون چیزی که AppCompatDelegate ست کرده)
     * اگر چیزی ست نشده باشد => "system"
     */
    fun getCurrentLanguageTag(): String {
        val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        return if (tags.isNullOrBlank()) "system" else normalizeLanguageTag(tags)
    }

    /**
     * اعمال زبان در سطح اپ:
     * - system => خالی کردن app locales (برگرد به زبان سیستم)
     * - غیر از آن => ست کردن locale
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
     * RTL/LTR بر اساس زبان (fa/ar => RTL)
     * اگر "system" باشد از Locale فعلی دستگاه استفاده می‌کند.
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
     * - fa => 🇮🇷
     * - ar => 🇸🇦
     * - en => 🇺🇸
     * - system => 🌐 (یا بر اساس زبان سیستم)
     */
    fun getFlagEmoji(tag: String): String {
        val normalized = normalizeLanguageTag(tag)

        return when (normalized) {
            "fa" -> "🇮🇷"
            "ar" -> "🇸🇦"
            "en" -> "🇺🇸"
            "system" -> {
                // اگر دوست داری، می‌تونی سیستم رو هم مپ کنی به پرچم مربوطه:
                when (Locale.getDefault().language.lowercase()) {
                    "fa" -> "🇮🇷"
                    "ar" -> "🇸🇦"
                    else -> "🌐"
                }
            }
            else -> "🌐"
        }
    }
}
