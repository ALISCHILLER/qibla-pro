package com.msa.qiblapro.data.sensors

import android.content.Context
import android.hardware.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class CompassData(
    val azimuthDeg: Double,
    val accuracy: Int
)

class CompassRepository(context: Context) {

    private val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rot = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    fun compassFlow(): Flow<CompassData> = callbackFlow {
        if (rot == null) {
            close()
            return@callbackFlow
        }

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)

                val azimuthRad = orientation[0]
                var azimuthDeg = Math.toDegrees(azimuthRad.toDouble())
                if (azimuthDeg < 0) azimuthDeg += 360.0

                trySend(CompassData(azimuthDeg, event.accuracy))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sm.registerListener(listener, rot, SensorManager.SENSOR_DELAY_GAME)
        awaitClose { sm.unregisterListener(listener) }
    }
}
