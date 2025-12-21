package com.msa.qiblapro.util

import android.content.Context

object LanguageHelper {

    /** زبان فعلی (fa / ar / en / …) */
    fun getCurrentLanguage(context: Context): String {
        val config = context.resources.configuration
        val locale = if (config.locales.isEmpty) {
            @Suppress("DEPRECATION")
            config.locale
        } else {
            config.locales[0]
        }
        return locale.language
    }

    /** زبان fallback منطقی: اگر fa → ar، اگر ar → en، بقیه → en */
    fun getFallbackLanguage(context: Context): String = when (getCurrentLanguage(context)) {
        "fa" -> "ar"
        "ar" -> "en"
        else -> "en"
    }

    /** ایموجی پرچم برای هر زبان */
    fun getFlagEmoji(lang: String): String = when (lang) {
        "fa" -> "\uD83C\uDDEE\uD83C\uDDF7" // 🇮🇷
        "ar" -> "\uD83C\uDDF8\uD83C\uDDE6" // 🇸🇦
        "en" -> "\uD83C\uDDFA\uD83C\uDDF8" // 🇺🇸
        else -> "🌐"
    }
}
