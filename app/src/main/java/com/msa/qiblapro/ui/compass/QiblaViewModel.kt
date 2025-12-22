package com.msa.qiblapro.ui.compass

import android.content.Context
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.qiblapro.data.compass.CompassReading
import com.msa.qiblapro.data.compass.CompassRepository
import com.msa.qiblapro.data.location.LocationRepository
import com.msa.qiblapro.data.settings.SettingsRepository
import com.msa.qiblapro.domain.qibla.QiblaMath
import com.msa.qiblapro.ui.events.AppEvent
import com.msa.qiblapro.util.GpsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class QiblaViewModel @Inject constructor(
    private val locRepo: LocationRepository,
    private val compassRepo: CompassRepository,
    private val settingsRepo: SettingsRepository,
    @ApplicationContext private val appCtx: Context
) : ViewModel() {

    private val _permission = MutableStateFlow(false)
    private val _state = MutableStateFlow(QiblaUiState())
    val state: StateFlow<QiblaUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    // آخرین پارامترها برای محاسبه دقیق
    private var lastDeclinationDeg: Float = 0f
    private var lastQiblaDeg: Float = 0f
    private var lastHeadingMag: Float? = null

    // Facing edge trigger (برای جلوگیری از ویبره/صدا پشت‌سرهم)
    private var facingLatched = false

    // ✅ Calibration by variance window + hysteresis
    private val headingWindow = AngleWindow(size = 60)
    private var varianceStrikes = 0
    private var accuracyStrikes = 0
    private var calibrationLatched = false

    fun setPermissionGranted(granted: Boolean) {
        _permission.value = granted
        _state.update { it.copy(hasLocationPermission = granted) }
    }

    init {
        observeLocation()
        observeCompass()
        observeSettingsToState()
    }

    private fun observeSettingsToState() {
        viewModelScope.launch {
            settingsRepo.settingsFlow.collect { s ->
                _state.update {
                    it.copy(
                        useTrueNorth = s.useTrueNorth,
                        smoothing = s.smoothing,
                        alignTolerance = s.alignmentToleranceDeg,
                        enableVibration = s.enableVibration,
                        enableSound = s.enableSound,
                        mapType = s.mapType,
                        showIranCities = s.showIranCities,
                        neonMapStyle = s.neonMapStyle,
                        showGpsPrompt = s.showGpsPrompt,
                        batterySaverMode = s.batterySaverMode,
                        bgUpdateFreqSec = s.bgUpdateFreqSec,
                        useLowPowerLocation = s.useLowPowerLocation,
                        autoCalibration = s.autoCalibration,
                        calibrationThreshold = s.calibrationThreshold
                    )
                }
            }
        }
    }

    private fun observeLocation() {
        viewModelScope.launch {
            _permission
                .flatMapLatest { granted ->
                    if (!granted) emptyFlow()
                    else combine(
                        locRepo.locationFlow(),
                        settingsRepo.settingsFlow
                    ) { loc, s -> loc to s }
                }
                .collect { (loc, settings) ->
                    val gpsEnabled = GpsUtils.isLocationEnabled(appCtx)

                    val qiblaDeg = QiblaMath.bearingToKaaba(loc.lat, loc.lon).toFloat()
                    val distance = QiblaMath.distanceKmToKaaba(loc.lat, loc.lon)

                    val decl = if (settings.useTrueNorth) {
                        QiblaMath.declinationDeg(
                            loc.lat, loc.lon, loc.alt, System.currentTimeMillis()
                        )
                    } else 0f

                    lastDeclinationDeg = decl
                    lastQiblaDeg = qiblaDeg

                    _state.update {
                        it.copy(
                            hasLocation = true,
                            lat = loc.lat,
                            lon = loc.lon,
                            accuracyM = loc.accuracyM,
                            qiblaDeg = qiblaDeg,
                            distanceKm = distance,
                            gpsEnabled = gpsEnabled,
                            showGpsDialog = !gpsEnabled && settings.showGpsPrompt
                        )
                    }
                }
        }
    }

    private fun observeCompass() {
        viewModelScope.launch {
            combine(_permission, settingsRepo.settingsFlow) { granted, settings ->
                granted to settings
            }
                .flatMapLatest { (granted, settings) ->
                    if (!granted) emptyFlow()
                    else {
                        val sensorDelay = if (settings.batterySaverMode) {
                            SensorManager.SENSOR_DELAY_UI
                        } else {
                            SensorManager.SENSOR_DELAY_GAME
                        }

                        val sampleMs = if (settings.batterySaverMode) 66L else 33L

                        compassRepo.compassFlow(sensorDelay)
                            .conflate()
                            .sample(sampleMs)
                            .map { reading -> settings to reading }
                            .catch { emit(settings to CompassReading(0f, 0)) }
                    }
                }
                .collect { (settings, reading) ->
                    onCompass(reading, settings)
                }
        }
    }

    private fun onCompass(reading: CompassReading, settings: com.msa.qiblapro.data.settings.AppSettings) {
        val raw = reading.headingMagneticDeg
        val current = lastHeadingMag

        // ✅ smoothing (زاویه-aware) + outlier rejection
        val smoothingFactor = settings.smoothing.coerceIn(0f, 1f)
        val t = 0.18f + (1f - smoothingFactor) * 0.72f // 0.18..0.90

        val nextMag = if (
            smoothingFactor == 0f ||
            current == null ||
            abs(angleDiff(current, raw)) > 35f
        ) raw else lerpAngle(current, raw, t)

        lastHeadingMag = nextMag

        val decl = if (settings.useTrueNorth) lastDeclinationDeg else 0f
        val trueHeading = (nextMag + decl + 360f) % 360f

        // Qibla error + facing
        val qibla = lastQiblaDeg
        val error = QiblaMath.rotationErrorDeg(trueHeading, qibla)
        val tol = settings.alignmentToleranceDeg.coerceIn(2, 20)

        // ✅ Hysteresis: وارد شدن به حالت facing سخت‌تر، خارج شدن کمی راحت‌تر
        val enterTol = tol
        val exitTol = (tol + 3).coerceAtMost(25)

        val isFacingNow = if (!facingLatched) abs(error) <= enterTol else abs(error) <= exitTol

        // Edge-trigger برای haptics/sound
        if (isFacingNow && !facingLatched) {
            facingLatched = true
            if (settings.enableVibration) _events.tryEmit(AppEvent.Vibrate)
            if (settings.enableSound) _events.tryEmit(AppEvent.Beep)
        } else if (!isFacingNow && facingLatched) {
            facingLatched = false
        }

        // ✅ Calibration by variance window (edge trigger)
        headingWindow.add(trueHeading)
        val variance = if (headingWindow.isReady()) headingWindow.circularVariance() else 0f

        val strikesNeeded = settings.calibrationThreshold.coerceIn(1, 10)

        // threshold ها با battery saver کمی ملایم‌تر
        val varianceOn = if (settings.batterySaverMode) 0.16f else 0.12f
        val varianceOff = varianceOn * 0.65f

        if (settings.autoCalibration && headingWindow.isReady()) {
            if (variance >= varianceOn) varianceStrikes++ else varianceStrikes = 0
            if (reading.accuracy == 0) accuracyStrikes++ else accuracyStrikes = 0
        } else {
            varianceStrikes = 0
            accuracyStrikes = 0
        }

        val needsNow = settings.autoCalibration && (
            varianceStrikes >= strikesNeeded || accuracyStrikes >= strikesNeeded
        )

        // latch + hysteresis
        if (needsNow) {
            calibrationLatched = true
        } else if (calibrationLatched && headingWindow.isReady() && variance < varianceOff && reading.accuracy != 0) {
            calibrationLatched = false
        }

        val needsCalibration = calibrationLatched || needsNow

        _state.update {
            it.copy(
                headingMagDeg = nextMag,
                headingTrue = trueHeading,
                rotationToQibla = error,
                facingQibla = isFacingNow,
                needsCalibration = needsCalibration
            )
        }
    }

    fun refreshSensors() {
        lastHeadingMag = null
        headingWindow.reset()
        varianceStrikes = 0
        accuracyStrikes = 0
        calibrationLatched = false
        facingLatched = false
    }

    fun hideGpsDialog() {
        _state.update { it.copy(showGpsDialog = false) }
    }

    // Settings setters (برای MapScreen هم لازم داریم)
    private fun io(block: suspend () -> Unit) = viewModelScope.launch { block() }

    fun setMapType(v: Int) = io { settingsRepo.setMapType(v) }
    fun setIranCities(v: Boolean) = io { settingsRepo.setShowIranCities(v) }
    fun setNeonMapStyle(v: Boolean) = io { settingsRepo.setNeonMapStyle(v) }

    // -------- angle helpers --------
    private fun lerpAngle(a: Float, b: Float, t: Float): Float {
        val diff = angleDiff(a, b)
        return (a + diff * t + 360f) % 360f
    }

    private fun angleDiff(a: Float, b: Float): Float {
        return ((b - a + 540f) % 360f) - 180f
    }

    private class AngleWindow(private val size: Int) {
        private val buf = FloatArray(size)
        private var idx = 0
        private var filled = 0

        fun add(angleDeg: Float) {
            buf[idx] = angleDeg
            idx = (idx + 1) % size
            if (filled < size) filled++
        }

        fun isReady(): Boolean = filled == size

        fun reset() {
            idx = 0
            filled = 0
        }

        fun circularVariance(): Float {
            var sumX = 0.0
            var sumY = 0.0
            for (i in 0 until filled) {
                val rad = Math.toRadians(buf[i].toDouble())
                sumX += cos(rad)
                sumY += sin(rad)
            }
            val n = filled.toDouble().coerceAtLeast(1.0)
            val r = sqrt(sumX * sumX + sumY * sumY) / n
            return (1.0 - r).toFloat() // 0 = stable, 1 = jitter
        }
    }
}
