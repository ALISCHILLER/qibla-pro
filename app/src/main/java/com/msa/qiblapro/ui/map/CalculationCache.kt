package com.msa.qiblapro.ui.map

/**
 * کش خیلی سبک برای محاسبه‌های تکراری distance در سرچ nearby.
 * کلیدها string هستند (مثلاً "lat,lon|cityLat,cityLon").
 */
object CalculationCache {
    private const val MAX_SIZE = 256

    private val lru = object : LinkedHashMap<String, Double>(MAX_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Double>?): Boolean {
            return size > MAX_SIZE
        }
    }

    @Synchronized
    fun getDistance(key: String, compute: () -> Double): Double {
        val cached = lru[key]
        if (cached != null) return cached
        val v = compute()
        lru[key] = v
        return v
    }

    @Synchronized
    fun clear() {
        lru.clear()
    }
}
