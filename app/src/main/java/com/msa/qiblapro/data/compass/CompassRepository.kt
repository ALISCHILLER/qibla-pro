package com.msa.qiblapro.data.compass

import android.content.Context
import android.hardware.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.roundToInt

data class CompassReading(
    val headingMagneticDeg: Float,  // 0..360
    val accuracy: Int
)

class CompassRepository(
    private val ctx: Context,
    private val sensorManager: SensorManager
) {
    fun compassFlow(): Flow<CompassReading> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: run {
                close()
                return@callbackFlow
            }

        val rot = FloatArray(9)
        val orient = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rot, event.values)
                SensorManager.getOrientation(rot, orient)
                val azimuthRad = orient[0]
                val azimuthDeg = (Math.toDegrees(azimuthRad.toDouble()).toFloat() + 360f) % 360f
                trySend(CompassReading(headingMagneticDeg = azimuthDeg, accuracy = event.accuracy))
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
