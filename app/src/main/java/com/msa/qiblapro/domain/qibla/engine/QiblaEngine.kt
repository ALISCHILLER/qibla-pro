package com.msa.qiblapro.domain.qibla.engine

import com.msa.qiblapro.domain.qibla.AngleMath
import com.msa.qiblapro.domain.qibla.QiblaMath

/**
 * خروجی محاسبه جهت‌یابی قبله
 */
data class QiblaEngineOutput(
    val headingDeg: Float,            // سر سمتی فعلی دستگاه (smooth شده)
    val rotationErrorDeg: Float,      // فاصله زاویه‌ای با قبله (مثبت یا منفی)
    val isFacing: Boolean,            // آیا دستگاه در محدوده قبله است؟
    val needsCalibration: Boolean     // آیا سنسور نیاز به کالیبراسیون دارد؟
)

/**
 * ورودی برای سیستم پردازش جهت‌یابی قبله
 */
data class QiblaEngineInput(
    val rawHeadingDeg: Float,         // سرسمت خام از سنسور
    val qiblaBearingDeg: Float,       // زاویه قبله نسبت به شمال
    val declinationDeg: Float,        // میل مغناطیسی
    val useTrueNorth: Boolean,        // استفاده از شمال حقیقی یا مغناطیسی
    val smoothingFactor: Float,       // ضریب فیلتر نرم‌کننده (0..1)
    val alignmentTolerance: Int,      // تولرانس مجاز برای جهت قبله
    val sensorAccuracy: Int,          // دقت سنسور (0 ضعیف، 3 عالی)
    val autoCalibration: Boolean,     // آیا کالیبراسیون خودکار فعال است؟
    val calibrationThreshold: Int     // آستانه تشخیص نیاز به کالیبراسیون
)

/**
 * موتور اصلی محاسبه جهت قبله و تشخیص کالیبراسیون
 */
class QiblaEngine(
    private var calibTracker: CalibrationTracker = CalibrationTracker()
) {
    private var smoother: HeadingSmoother? = null
    private var lastAlpha: Float = -1f
    private var facingTracker: FacingTracker = FacingTracker(tolOn = 6, tolOff = 10)
    private var lastTol: Int = -1
    private var lastCalibThreshold: Int = -1
    private var lowAccuracyCount: Int = 0

    /**
     * ری‌ست کلی موتور و حافظه داخلی
     */
    fun reset() {
        smoother = null
        lastAlpha = -1f
        lastTol = -1
        lastCalibThreshold = -1
        lowAccuracyCount = 0
        calibTracker.reset()
    }

    /**
     * محاسبه خروجی براساس ورودی فعلی
     */
    fun calculate(input: QiblaEngineInput): QiblaEngineOutput {
        val effectiveAlpha = adjustAlphaForAccuracy(input.smoothingFactor, input.sensorAccuracy)
        // north-adjusted = تنظیم‌شده بر اساس declination
        val northAdjusted = if (input.useTrueNorth) {
            AngleMath.norm360(input.rawHeadingDeg + input.declinationDeg)
        } else {
            AngleMath.norm360(input.rawHeadingDeg)
        }

        // اگر tolerance عوض شده بود → facingTracker جدید بساز
        if (input.alignmentTolerance != lastTol) {
            lastTol = input.alignmentTolerance
            facingTracker = FacingTracker(
                tolOn = input.alignmentTolerance,
                tolOff = input.alignmentTolerance + 4
            )
        }

        // اگر smoother جدید نیاز بود (یا alpha تغییر کرده)
        if (smoother == null) {
            smoother = HeadingSmoother(effectiveAlpha, northAdjusted)
            lastAlpha = effectiveAlpha
        } else if (effectiveAlpha != lastAlpha) {
            smoother!!.setAlpha(effectiveAlpha)
            lastAlpha = effectiveAlpha
        }

        // اگر آستانه کالیبراسیون تغییر کرده، ترکر را ریست کن
        if (input.calibrationThreshold != lastCalibThreshold) {
            lastCalibThreshold = input.calibrationThreshold
            calibTracker.reset()
        }

        val smoothedHeading = smoother!!.update(northAdjusted)
        val error = AngleMath.diffDeg(input.qiblaBearingDeg, smoothedHeading)
        val isFacing = facingTracker.update(input.qiblaBearingDeg, smoothedHeading)

        // تشخیص نیاز به کالیبراسیون بخاطر دقت پایین
        val accuracyNeedsCalib = if (input.sensorAccuracy == 0) {
            lowAccuracyCount += 1
            lowAccuracyCount >= input.calibrationThreshold.coerceIn(1, 10)
        } else {
            lowAccuracyCount = 0
            false
        }

        // ترکیب: خودکار یا فقط وقتی سنسور ضعیفه
        val needsCalib = if (input.autoCalibration) {
            accuracyNeedsCalib || calibTracker.update(smoothedHeading)
        } else {
            input.sensorAccuracy == 0
        }

        return QiblaEngineOutput(
            headingDeg = smoothedHeading,
            rotationErrorDeg = error,
            isFacing = isFacing,
            needsCalibration = needsCalib
        )
    }
    private fun adjustAlphaForAccuracy(alpha: Float, accuracy: Int): Float {
        val cap = when (accuracy) {
            0 -> 0.20f
            1 -> 0.35f
            2 -> 0.60f
            else -> 0.90f
        }
        return alpha.coerceIn(0.01f, cap)
    }
}
