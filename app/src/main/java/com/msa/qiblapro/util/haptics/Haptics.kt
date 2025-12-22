package com.msa.qiblapro.util.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object Haptics {
    fun vibrate(context: Context, strength: Int = 2, pattern: Int = 1) {
        val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (!vib.hasVibrator()) return

        val amplitude = when (strength) {
            1 -> 50   // Low
            3 -> 255  // High
            else -> 150 // Medium
        }

        if (Build.VERSION.SDK_INT >= 26) {
            val effect = when (pattern) {
                2 -> VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 40), intArrayOf(0, amplitude, 0, amplitude), -1)
                3 -> VibrationEffect.createOneShot(250, amplitude)
                else -> VibrationEffect.createOneShot(60, amplitude) // Short
            }
            vib.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            when (pattern) {
                2 -> vib.vibrate(longArrayOf(0, 40, 60, 40), -1)
                3 -> vib.vibrate(250)
                else -> vib.vibrate(60)
            }
        }
    }
}
