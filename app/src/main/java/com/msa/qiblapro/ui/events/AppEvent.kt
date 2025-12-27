package com.msa.qiblapro.ui.events

sealed interface AppEvent {
    data class Snack(val msg: String) : AppEvent
    data object Beep : AppEvent
    data class VibratePattern(val strength: Int, val pattern: Int) : AppEvent

}
