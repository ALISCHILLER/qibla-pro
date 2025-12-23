package com.msa.qiblapro.domain.qibla.engine

import com.msa.qiblapro.domain.qibla.QiblaMath

data class QiblaEngineOutput(
    val headingDeg: Float,
    val rotationErrorDeg: Float,
    val isFacing: Boolean,
    val needsCalibration: Boolean
)

data class QiblaEngineInput(
    val rawHeadingDeg: Float,
    val qiblaBearingDeg: Float,
    val declinationDeg: Float,
    val useTrueNorth: Boolean,
    val smoothingFactor: Float,
    val alignmentTolerance: Int,
    val sensorAccuracy: Int
)

class QiblaEngine(
    private val calibTracker: CalibrationTracker = CalibrationTracker()
) {
    private var smoother: HeadingSmoother? = null
    private var lastAlpha: Float = -1f
    private var facingTracker: FacingTracker = FacingTracker(tolOn = 6, tolOff = 10)
    private var lastTol: Int = -1

    fun reset() {
        smoother = null
        lastAlpha = -1f
        lastTol = -1
        calibTracker.reset()
    }

    fun calculate(input: QiblaEngineInput): QiblaEngineOutput {
        val northAdjusted = if (input.useTrueNorth) {
            AngleMath.norm360(input.rawHeadingDeg + input.declinationDeg)
        } else {
            AngleMath.norm360(input.rawHeadingDeg)
        }

        if (input.alignmentTolerance != lastTol) {
            lastTol = input.alignmentTolerance
            facingTracker = FacingTracker(
                tolOn = input.alignmentTolerance,
                tolOff = input.alignmentTolerance + 4
            )
        }

        if (smoother == null) {
            smoother = HeadingSmoother(input.smoothingFactor, northAdjusted)
            lastAlpha = input.smoothingFactor
        } else if (input.smoothingFactor != lastAlpha) {
            smoother!!.setAlpha(input.smoothingFactor)
            lastAlpha = input.smoothingFactor
        }

        val smoothedHeading = smoother!!.update(northAdjusted)
        val error = AngleMath.diffDeg(input.qiblaBearingDeg, smoothedHeading)
        val isFacing = facingTracker.update(input.qiblaBearingDeg, smoothedHeading)
        val needsCalib = (input.sensorAccuracy == 0) || calibTracker.update(smoothedHeading)

        return QiblaEngineOutput(
            headingDeg = smoothedHeading,
            rotationErrorDeg = error,
            isFacing = isFacing,
            needsCalibration = needsCalib
        )
    }
}
