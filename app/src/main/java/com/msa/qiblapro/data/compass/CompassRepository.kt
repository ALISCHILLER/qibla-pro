package com.msa.qiblapro.data.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import com.msa.qiblapro.BuildConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class CompassReading(
    val headingMagneticDeg: Float,
    val accuracy: Int
)

class CompassRepository(
    private val context: Context,
    private val sensorManager: SensorManager
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    fun compassFlow(sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME): Flow<CompassReading> = callbackFlow {
        val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val rotationMatrix = FloatArray(9)
        val outRotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                var heading = -1f
                val rotation = windowManager.defaultDisplay.rotation

                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        remapAndGetHeading(rotation, rotationMatrix, outRotationMatrix, orientation)?.let {
                            heading = it
                        }
                    }
                    Sensor.TYPE_ACCELEROMETER -> System.arraycopy(event.values, 0, gravity, 0, 3)
                    Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                }

                if (rotationVector == null && gravity[0] != 0f && geomagnetic[0] != 0f) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                        remapAndGetHeading(rotation, rotationMatrix, outRotationMatrix, orientation)?.let {
                            heading = it
                        }
                    }
                }

                if (heading >= 0f) {
                    trySend(CompassReading(heading, event.accuracy))
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (rotationVector != null) {
            sensorManager.registerListener(listener, rotationVector, sensorDelay)
        } else if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(listener, accelerometer, sensorDelay)
            sensorManager.registerListener(listener, magnetometer, sensorDelay)
        } else {
            close(IllegalStateException("No usable sensors"))
            return@callbackFlow
        }

        awaitClose { sensorManager.unregisterListener(listener) }
    }

    private fun remapAndGetHeading(
        rotation: Int,
        inR: FloatArray,
        outR: FloatArray,
        orientation: FloatArray
    ): Float? {
        var worldAxisX = SensorManager.AXIS_X
        var worldAxisY = SensorManager.AXIS_Y

        when (rotation) {
            Surface.ROTATION_90 -> { worldAxisX = SensorManager.AXIS_Y; worldAxisY = SensorManager.AXIS_MINUS_X }
            Surface.ROTATION_180 -> { worldAxisX = SensorManager.AXIS_MINUS_X; worldAxisY = SensorManager.AXIS_MINUS_Y }
            Surface.ROTATION_270 -> { worldAxisX = SensorManager.AXIS_MINUS_Y; worldAxisY = SensorManager.AXIS_X }
        }

        return if (SensorManager.remapCoordinateSystem(inR, worldAxisX, worldAxisY, outR)) {
            SensorManager.getOrientation(outR, orientation)
            (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
        } else null
    }
}
