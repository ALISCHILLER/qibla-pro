package com.msa.qiblapro.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object Haptics {
    fun vibrate(context: Context, ms: Long = 80) {
        val vib = context.getSystemService(Vibrator::class.java) ?: return
        if (Build.VERSION.SDK_INT >= 26) {
            vib.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(ms)
        }
    }
}
