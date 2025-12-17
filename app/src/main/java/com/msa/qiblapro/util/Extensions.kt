package com.msa.qiblapro.util

import kotlin.math.abs

fun Double.normalize360(): Double {
    var x = this % 360.0
    if (x < 0) x += 360.0
    return x
}

fun angularDiffDeg(a: Double, b: Double): Double {
    val d = (a - b + 540.0) % 360.0 - 180.0
    return d
}

fun Double.isCloseToZero(tol: Double) = abs(this) <= tol
