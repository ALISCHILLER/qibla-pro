package com.msa.qiblapro.util

import android.content.Context
import android.content.ContextWrapper
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

    fun setLocale(context: Context, langCode: String): ContextWrapper {
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        val newContext = context.createConfigurationContext(config)
        return ContextWrapper(newContext)
    }

    fun getFlagEmoji(lang: String): String = when (lang) {
        "fa" -> "\uD83C\uDDEE\uD83C\uDDF7" // 🇮🇷
        "ar" -> "\uD83C\uDDF8\uD83C\uDDE6" // 🇸🇦
        "en" -> "\uD83C\uDDFA\uD83C\uDDF8" // 🇺🇸
        else -> "🌐"
    }
}
