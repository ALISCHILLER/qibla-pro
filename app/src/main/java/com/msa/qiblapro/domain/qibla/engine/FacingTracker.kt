package com.msa.qiblapro.domain.qibla.engine

import com.msa.qiblapro.domain.qibla.AngleMath

class FacingTracker(
    private val tolOn: Int,
    private val tolOff: Int
) {
    private var facing = false

    fun reset() {
        facing = false
    }

    fun update(targetDeg: Float, headingDeg: Float): Boolean {
        val err = AngleMath.absDiff(targetDeg, headingDeg)
        facing = if (!facing) err <= tolOn else err <= tolOff
        return facing
    }
}
