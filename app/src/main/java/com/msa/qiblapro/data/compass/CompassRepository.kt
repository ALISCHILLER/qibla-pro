package com.msa.qiblapro.data.compass

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.Log
import com.msa.qiblapro.BuildConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class CompassReading(
    val headingMagneticDeg: Float,
    val accuracy: Int
)

class CompassRepository(
    private val sensorManager: SensorManager
) {

    fun compassFlow(sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME): Flow<CompassReading> = callbackFlow {
        val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (BuildConfig.DEBUG) {
            Log.d(
                "CompassRepository",
                "Sensors: rotVec=${rotationVector != null}, acc=${accelerometer != null}, mag=${magnetometer != null}"
            )
        }

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)

        var lastLogTimeMs = SystemClock.elapsedRealtime()
        var eventsCount = 0

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                var heading = -1f

                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        heading = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, gravity, 0, 3)
                        if (rotationVector != null) return
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                        if (rotationVector != null) return
                    }
                }

                // Fallback اگر Rotation Vector نداریم
                if (rotationVector == null && gravity[0] != 0f && geomagnetic[0] != 0f) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        heading = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
                    }
                }

                if (heading >= 0f) {
                    if (BuildConfig.DEBUG) {
                        eventsCount++
                        val now = SystemClock.elapsedRealtime()
                        val dt = now - lastLogTimeMs
                        if (dt >= 1000L) {
                            val hz = eventsCount.toFloat() * 1000f / dt.toFloat()
                            Log.d("CompassRepository", "Hz=%.1f heading=%.1f".format(hz, heading))
                            lastLogTimeMs = now
                            eventsCount = 0
                        }
                    }
                    trySend(CompassReading(heading, event.accuracy))
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (rotationVector != null) {
            sensorManager.registerListener(listener, rotationVector, sensorDelay)
        } else {
            // ✅ اگر Rotation Vector نداریم، باید هم acc و هم mag موجود باشند
            if (accelerometer == null || magnetometer == null) {
                if (BuildConfig.DEBUG) {
                    Log.w(
                        "CompassRepository",
                        "No usable compass sensors. acc=${accelerometer != null} mag=${magnetometer != null}"
                    )
                }
                // با cause می‌بندیم تا upstream catch در ViewModel فعال شود
                close(IllegalStateException("No usable compass sensors (need accelerometer + magnetometer or rotation vector)."))
                return@callbackFlow
            }

            sensorManager.registerListener(listener, accelerometer, sensorDelay)
            sensorManager.registerListener(listener, magnetometer, sensorDelay)
        }

        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
