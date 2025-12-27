package com.msa.qiblapro.data.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class CompassReading(
    val headingMagneticDeg: Float,
    val accuracy: Int,
    val timestampMs: Long
)

sealed interface CompassResult {
    data class Success(val reading: CompassReading) : CompassResult
    data object SensorUnavailable : CompassResult
    data class Failure(val t: Throwable) : CompassResult
}

class CompassRepository(
    private val context: Context,
    private val sensorManager: SensorManager
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val accelMagFilterAlpha = 0.8f
    fun compassFlow(sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME): Flow<CompassResult> =
        callbackFlow {
            val rotationSensor = selectRotationSensor()
            val accelerometer = if (rotationSensor == null) {
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            } else {
                null
            }
            val magnetometer = if (rotationSensor == null) {
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            } else {
                null
            }

            val rotationMatrix = FloatArray(9)
            val outRotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            val gravity = FloatArray(3)
            val geomagnetic = FloatArray(3)


            val minEmitIntervalMs = when (sensorDelay) {
                SensorManager.SENSOR_DELAY_FASTEST -> 16L
                SensorManager.SENSOR_DELAY_UI -> 80L
                else -> 40L
            }
            var lastEmitMs = 0L

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val now = System.currentTimeMillis()
                    if (now - lastEmitMs < minEmitIntervalMs) return

                    var heading = -1f
                    val rotation = getDisplayRotation()

                    when (event.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR,
                        Sensor.TYPE_GAME_ROTATION_VECTOR,
                        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                            remapAndGetHeading(
                                rotation,
                                rotationMatrix,
                                outRotationMatrix,
                                orientation
                            )?.let {
                                heading = it
                            }
                        }

                        Sensor.TYPE_ACCELEROMETER -> {
                            lowPassFilter(event.values, gravity, accelMagFilterAlpha)
                        }

                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            lowPassFilter(event.values, geomagnetic, accelMagFilterAlpha)
                        }
                    }

                    if (rotationSensor == null && gravity[0] != 0f && geomagnetic[0] != 0f) {
                        if (SensorManager.getRotationMatrix(
                                rotationMatrix,
                                null,
                                gravity,
                                geomagnetic
                            )
                        ) {
                            remapAndGetHeading(
                                rotation,
                                rotationMatrix,
                                outRotationMatrix,
                                orientation
                            )?.let {
                                heading = it
                            }
                        }
                    }

                    if (heading >= 0f) {
                        lastEmitMs = now
                        val result = trySend(
                            CompassResult.Success(
                                CompassReading(heading, event.accuracy, now)
                            )
                        )
                        if (result.isFailure) {
                            Log.w(
                                "CompassRepository",
                                "Dropped compass reading",
                                result.exceptionOrNull()
                            )
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            try {
                if (rotationSensor != null) {
                    sensorManager.registerListener(listener, rotationSensor, sensorDelay)
                } else if (accelerometer != null && magnetometer != null) {
                    sensorManager.registerListener(listener, accelerometer, sensorDelay)
                    sensorManager.registerListener(listener, magnetometer, sensorDelay)
                } else {
                    trySend(CompassResult.SensorUnavailable)
                    close()
                    return@callbackFlow
                }

                awaitClose {
                    try {
                        sensorManager.unregisterListener(listener)
                    } catch (e: Exception) {
                        Log.e("CompassRepository", "Error unregistering listener", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("CompassRepository", "Error registering listener", e)
                trySend(CompassResult.Failure(e))
                close(e)
            }
        }

    private fun selectRotationSensor(): Sensor? {
        val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val geomagneticVector = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        val gameRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)

        return when {
            rotationVector != null -> rotationVector
            geomagneticVector != null -> geomagneticVector
            gameRotationVector != null -> {
                Log.w("CompassRepository", "Using GAME_ROTATION_VECTOR (may drift).")
                gameRotationVector
            }
            else -> null
        }
    }

    private fun lowPassFilter(input: FloatArray, output: FloatArray, alpha: Float) {
        for (i in 0 until 3) {
            output[i] = alpha * output[i] + (1 - alpha) * input[i]
        }
    }

    private fun getDisplayRotation(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.rotation ?: run {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.rotation
            }
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.rotation
        }
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
            Surface.ROTATION_90 -> {
                worldAxisX = SensorManager.AXIS_Y; worldAxisY = SensorManager.AXIS_MINUS_X
            }

            Surface.ROTATION_180 -> {
                worldAxisX = SensorManager.AXIS_MINUS_X; worldAxisY = SensorManager.AXIS_MINUS_Y
            }

            Surface.ROTATION_270 -> {
                worldAxisX = SensorManager.AXIS_MINUS_Y; worldAxisY = SensorManager.AXIS_X
            }
        }

        return if (SensorManager.remapCoordinateSystem(inR, worldAxisX, worldAxisY, outR)) {
            SensorManager.getOrientation(outR, orientation)
            (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
        } else null
    }
}
