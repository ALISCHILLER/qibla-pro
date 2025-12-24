package com.msa.qiblapro.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageHelper {

    /**
     * اعمال زبان به کل اپلیکیشن (UI + اعداد)
     */
    fun applyLanguage(langCode: String) {
        // تنظیم Locale برای اعداد و فرمت‌ها
        val locale = Locale.forLanguageTag(langCode)
        Locale.setDefault(locale)

        // اعمال به منابع سیستم با AppCompatDelegate برای تغییر متون UI
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(langCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    /**
     * دریافت زبان فعلی اپلیکیشن
     */
    fun getCurrentLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (!locales.isEmpty) {
            locales[0]?.language ?: "en"
        } else {
            Locale.getDefault().language
        }
    }

    fun getFlagEmoji(lang: String): String = when (lang) {
        "fa" -> "\uD83C\uDDEE\uD83C\uDDF7" // 🇮🇷
        "ar" -> "\uD83C\uDDF8\uD83C\uDDE6" // 🇸🇦
        "en" -> "\uD83C\uDDFA\uD83C\uDDF8" // 🇺🇸
        else -> "🌐"
    }
}
