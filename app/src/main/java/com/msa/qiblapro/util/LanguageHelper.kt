package com.msa.qiblapro.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageHelper {

    /**
     * اعمال زبان به کل اپلیکیشن (UI + اعداد)
     */
    fun applyLanguage(languageCode: String) {
        // ۱. برای فارسی شدن اعداد و محاسبات ریاضی
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)

        // ۲. برای فارسی شدن متون رابط کاربری (UI Strings)
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    /**
     * دریافت کد زبان فعلی اپلیکیشن
     */
    fun getCurrentLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (!locales.isEmpty) {
            locales[0]?.language ?: "en"
        } else {
            Locale.getDefault().language ?: "en"
        }
    }

    fun getFlagEmoji(lang: String): String = when (lang) {
        "fa" -> "\uD83C\uDDEE\uD83C\uDDF7" // 🇮🇷
        "ar" -> "\uD83C\uDDF8\uD83C\uDDE6" // 🇸🇦
        "en" -> "\uD83C\uDDFA\uD83C\uDDF8" // 🇺🇸
        else -> "🌐"
    }
}
