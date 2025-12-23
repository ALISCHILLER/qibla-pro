package com.msa.qiblapro.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LanguageHelper {

    fun getCurrentLanguage(context: Context): String {
        val config = context.resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.locales[0].language
        } else {
            @Suppress("DEPRECATION")
            config.locale.language
        } ?: "en"
    }

    /** 
     * اعمال زبان به تنظیمات اپلیکیشن
     */
    fun applyLanguage(context: Context, langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        
        val resources = context.resources
        val config = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun getFlagEmoji(lang: String): String = when (lang) {
        "fa" -> "\uD83C\uDDEE\uD83C\uDDF7" // 🇮🇷
        "ar" -> "\uD83C\uDDF8\uD83C\uDDE6" // 🇸🇦
        "en" -> "\uD83C\uDDFA\uD83C\uDDF8" // 🇺🇸
        else -> "🌐"
    }
}
