package com.msa.qiblapro.app

import android.app.Application
import com.msa.qiblapro.data.settings.SettingsRepository
import com.msa.qiblapro.util.LanguageHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class QiblaProApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val settingsRepo = SettingsRepository(this)
        val langCode = runBlocking { settingsRepo.settingsFlow.first().languageCode }
        if (LanguageHelper.getCurrentLanguageTag() != langCode) {
            LanguageHelper.applyLanguage(langCode)
        }
    }
}
