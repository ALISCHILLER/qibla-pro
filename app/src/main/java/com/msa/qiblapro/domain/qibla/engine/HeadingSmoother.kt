package com.msa.qiblapro.domain.qibla.engine

import com.msa.qiblapro.domain.qibla.AngleMath

class HeadingSmoother(
    alpha: Float,
    initial: Float = 0f
) {
    private var a: Float = alpha.coerceIn(0.01f, 0.99f)
    private var value: Float = AngleMath.norm360(initial)
    private var initialized = false

    fun setAlpha(alpha: Float) {
        a = alpha.coerceIn(0.01f, 0.99f)
    }

    fun update(rawDeg: Float): Float {
        val x = AngleMath.norm360(rawDeg)

        if (!initialized) {
            initialized = true
            value = x
            return value
        }

        val delta = AngleMath.diffDeg(x, value)
        value = AngleMath.norm360(value + a * delta)
        return value
    }
}
