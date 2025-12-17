package com.msa.qiblapro.domain

import kotlin.math.*

class CircularEma(private val alpha: Double) {
    private var inited = false
    private var x = 0.0
    private var y = 0.0

    fun filter(angleDeg: Double): Double {
        val rad = Math.toRadians(angleDeg)
        val cx = cos(rad)
        val sy = sin(rad)

        if (!inited) {
            x = cx; y = sy
            inited = true
        } else {
            x = alpha * x + (1 - alpha) * cx
            y = alpha * y + (1 - alpha) * sy
        }

        val out = Math.toDegrees(atan2(y, x))
        return (out + 360.0) % 360.0
    }
}
