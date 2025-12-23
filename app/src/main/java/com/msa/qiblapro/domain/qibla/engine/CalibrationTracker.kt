package com.msa.qiblapro.domain.qibla.engine

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CalibrationTracker(
    private val window: Int = 60,
    private val varOn: Float = 0.35f,
    private val varOff: Float = 0.25f
) {
    private val buf = ArrayDeque<Float>(window)
    private var need = false

    fun reset() {
        buf.clear()
        need = false
    }

    fun update(headingDeg: Float): Boolean {
        if (buf.size == window) buf.removeFirst()
        buf.addLast(headingDeg)

        if (buf.size < window / 2) return need

        val v = circularVariance(buf)
        need = if (!need) v > varOn else v > varOff
        return need
    }

    private fun circularVariance(samples: Collection<Float>): Float {
        var sx = 0.0
        var sy = 0.0
        val n = samples.size.coerceAtLeast(1)

        for (deg in samples) {
            val rad = Math.toRadians(deg.toDouble())
            sx += cos(rad)
            sy += sin(rad)
        }

        val mx = sx / n
        val my = sy / n
        val r = sqrt(mx * mx + my * my)
        return (1.0 - r).toFloat()
    }
}
