package com.msa.qiblapro.data.compass

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.Log
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

    fun compassFlow(): Flow<CompassReading> = callbackFlow {
        Log.d("CompassRepository", "🌐 Starting HIGH-SPEED RAW flow (GAME delay)")

        val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        Log.d(
            "CompassRepository",
            "Sensors → ROT_VEC=${rotationVector != null}, ACC=${accelerometer != null}, MAG=${magnetometer != null}"
        )

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)

        // متغیرهای لاگ سرعت
        var lastLogTimeMs = SystemClock.elapsedRealtime()
        var eventsCount = 0

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                var rawHeading = -1f
                var source = "?"

                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        rawHeading =
                            (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
                        source = "ROTATION_VECTOR"
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

                // Fallback زمانی که Rotation Vector موجود نیست
                if (rotationVector == null && gravity[0] != 0f && geomagnetic[0] != 0f) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        rawHeading =
                            (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
                        source = "ACC+MAG"
                    }
                }

                if (rawHeading >= 0f) {
                    // ⚡ لاگ سرعت هر حدوداً ۱ ثانیه یکبار
                    eventsCount++
                    val now = SystemClock.elapsedRealtime()
                    val dt = now - lastLogTimeMs
                    if (dt >= 1000L) {
                        val hz = eventsCount.toFloat() * 1000f / dt.toFloat()
                        Log.d(
                            "CompassRepository",
                            "⚡ Hz=%.1f  events=%d  dt=%dms  heading=%.1f°  src=%s".format(
                                hz, eventsCount, dt, rawHeading, source
                            )
                        )
                        lastLogTimeMs = now
                        eventsCount = 0
                    }

                    trySend(CompassReading(rawHeading, event.accuracy))
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val delay = SensorManager.SENSOR_DELAY_GAME
        Log.d("CompassRepository", "Register listener with delay=SENSOR_DELAY_GAME")

        if (rotationVector != null) {
            sensorManager.registerListener(listener, rotationVector, delay)
        } else {
            sensorManager.registerListener(listener, accelerometer, delay)
            sensorManager.registerListener(listener, magnetometer, delay)
        }

        awaitClose {
            Log.d("CompassRepository", "🛑 Stopping compass listener")
            sensorManager.unregisterListener(listener)
        }
    }
}
