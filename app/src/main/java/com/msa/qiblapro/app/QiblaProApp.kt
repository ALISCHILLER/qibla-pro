package com.msa.qiblapro.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QiblaProApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
