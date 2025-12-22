package com.msa.qiblapro.util.haptics

import android.media.AudioManager
import android.media.ToneGenerator

class SoundFx {
    private var tone: ToneGenerator? = ToneGenerator(AudioManager.STREAM_MUSIC, 80)

    fun beep() {
        tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }

    fun release() {
        tone?.release()
        tone = null
    }
}