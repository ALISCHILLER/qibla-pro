package com.msa.qiblapro.domain.qibla.engine

import kotlin.math.abs

object AngleMath {
    /** Normalize angle to [0, 360) range */
    fun norm360(deg: Float): Float {
        var x = deg % 360f
        if (x < 0f) x += 360f
        return x
    }

    /** Shortest signed difference (a - b) in (-180..180] */
    fun diffDeg(a: Float, b: Float): Float {
        var d = (a - b + 540f) % 360f - 180f
        return d
    }

    fun absDiff(a: Float, b: Float): Float = abs(diffDeg(a, b))
}
