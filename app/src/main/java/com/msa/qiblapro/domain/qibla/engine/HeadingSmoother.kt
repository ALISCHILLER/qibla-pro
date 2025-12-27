package com.msa.qiblapro.domain.qibla.engine

import com.msa.qiblapro.domain.qibla.AngleMath
import kotlin.math.abs

/**
 * فیلتر نرم‌کننده زاویه با قابلیت تطبیق هوشمند
 * اگر تغییر ناگهانی زیاد باشد (چرخش سریع گوشی)، فیلتر موقتاً ضعیف می‌شود تا پاسخ سریع باشد.
 * اگر تغییرات کوچک باشد، فیلتر قوی می‌ماند تا لرزش (Jitter) گرفته شود.
 */
class HeadingSmoother(
    private var baseAlpha: Float,
    initial: Float = 0f
) {
    private var value: Float = AngleMath.norm360(initial)
    private var initialized = false

    fun setAlpha(alpha: Float) {
        baseAlpha = alpha.coerceIn(0.01f, 0.99f)
    }

    fun update(rawDeg: Float): Float {
        val x = AngleMath.norm360(rawDeg)

        if (!initialized) {
            initialized = true
            value = x
            return value
        }

        val delta = AngleMath.diffDeg(x, value)
        
        // --- مکانیزم سرعت هوشمند ---
        // اگر اختلاف زاویه زیاد باشد (مثلاً بیش از ۲۰ درجه)، یعنی کاربر گوشی را سریع چرخاند
        // در این صورت ضریب را بالا می‌بریم تا قطب‌نما سریع خودش را برساند.
        val dynamicAlpha = if (abs(delta) > 20f) {
            (baseAlpha * 2.5f).coerceAtMost(0.95f) 
        } else if (abs(delta) > 10f) {
            (baseAlpha * 1.8f).coerceAtMost(0.85f)
        } else {
            baseAlpha
        }

        value = AngleMath.norm360(value + dynamicAlpha * delta)
        return value
    }
}
