package com.msa.qiblapro.ui.compass

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.qiblapro.data.compass.CompassReading
import com.msa.qiblapro.data.compass.CompassRepository
import com.msa.qiblapro.data.location.LocationRepository
import com.msa.qiblapro.data.settings.SettingsRepository
import com.msa.qiblapro.domain.qibla.QiblaMath
import com.msa.qiblapro.ui.compass.QiblaUiState
import com.msa.qiblapro.util.GpsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class QiblaViewModel @Inject constructor(
    private val locRepo: LocationRepository,
    private val compassRepo: CompassRepository,
    private val settingsRepo: SettingsRepository,
    @ApplicationContext private val appCtx: Context
) : ViewModel() {

    private val _permission = MutableStateFlow(false)
    private val _state = MutableStateFlow(QiblaUiState())
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val state: StateFlow<QiblaUiState> = _state.asStateFlow()

    // آخرین heading و پارامترهای مربوط به قبله
    private var lastHeadingMag: Float? = null
    private var lastDeclinationDeg: Float = 0f
    private var lastQiblaDeg: Float = 0f

    init {
        observeCompass()
        observeLocation()
    }

    fun setPermissionGranted(granted: Boolean) {
        _permission.value = granted
        _state.update { it.copy(hasLocationPermission = granted) }
    }

    // ----------- COMPASS FLOW (سرعت بالا، همه چیز اینجا آپدیت می‌شود) -----------
    private fun observeCompass() {
        viewModelScope.launch {
            combine(
                _permission,
                refreshTrigger.onStart { emit(Unit) },
                settingsRepo.settingsFlow,
                compassRepo.compassFlow().catch { emit(CompassReading(0f, 0)) }
            ) { permission, _, settings, compass ->
                Triple(permission, settings, compass)
            }.collect { (hasPermission, settings, compass) ->
                if (!hasPermission) return@collect

                val raw = compass.headingMagneticDeg
                val current = lastHeadingMag

                // اگر smoothing = 0 → خامِ خام
                val smoothingFactor = settings.smoothing.coerceIn(0f, 1f)
                val t = 0.2f + (1f - smoothingFactor) * 0.7f // بین 0.2 تا 0.9

                val nextHeading = if (
                    smoothingFactor == 0f ||
                    current == null ||
                    abs(angleDiff(current, raw)) > 30f
                ) {
                    raw
                } else {
                    lerpAngle(current, raw, t)
                }

                lastHeadingMag = nextHeading

                // True North براساس declination آخرین لوکیشن
                val decl = if (settings.useTrueNorth) lastDeclinationDeg else 0f
                val trueHeading = (nextHeading + decl + 360f) % 360f

                // خطا نسبت به قبله و وضعیت facing
                val qibla = lastQiblaDeg
                val error = QiblaMath.rotationErrorDeg(trueHeading, qibla)
                val isFacing = abs(error) <= settings.alignmentToleranceDeg.coerceIn(2, 20)

                _state.update {
                    it.copy(
                        headingMagDeg = nextHeading,
                        headingTrue = trueHeading,
                        rotationToQibla = error,
                        facingQibla = isFacing,
                        needsCalibration = compass.accuracy == 0,
                        useTrueNorth = settings.useTrueNorth,
                        smoothing = settings.smoothing,
                        alignTolerance = settings.alignmentToleranceDeg,
                        enableVibration = settings.enableVibration,
                        enableSound = settings.enableSound,
                        mapType = settings.mapType,
                        showIranCities = settings.showIranCities,
                        showGpsPrompt = settings.showGpsPrompt,
                        batterySaverMode = settings.batterySaverMode,
                        bgUpdateFreqSec = settings.bgUpdateFreqSec,
                        useLowPowerLocation = settings.useLowPowerLocation,
                        autoCalibration = settings.autoCalibration,
                        calibrationThreshold = settings.calibrationThreshold
                    )
                }
            }
        }
    }

    // ----------- LOCATION FLOW (فقط لوکیشن، قبله، فاصله و GPS) -----------
    private fun observeLocation() {
        viewModelScope.launch {
            _permission.flatMapLatest { granted ->
                if (!granted) emptyFlow() else locRepo.locationFlow()
            }.collect { loc ->
                val settings = settingsRepo.settingsFlow.first()
                val gpsEnabled = GpsUtils.isLocationEnabled(appCtx)

                val qiblaDeg = QiblaMath.bearingToKaaba(loc.lat, loc.lon).toFloat()
                val distance = QiblaMath.distanceKmToKaaba(loc.lat, loc.lon)

                val declination = if (settings.useTrueNorth)
                    QiblaMath.declinationDeg(
                        loc.lat,
                        loc.lon,
                        loc.alt,
                        System.currentTimeMillis()
                    )
                else 0f

                // برای استفاده در observeCompass
                lastDeclinationDeg = declination
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

    fun refreshSensors() {
        lastHeadingMag = null
        refreshTrigger.tryEmit(Unit)
    }

    fun hideGpsDialog() {
        _state.update { it.copy(showGpsDialog = false) }
    }

    // ----------- کمک‌متدهای زاویه -----------

    private fun lerpAngle(a: Float, b: Float, t: Float): Float {
        val diff = angleDiff(a, b)
        return (a + diff * t + 360f) % 360f
    }

    private fun angleDiff(a: Float, b: Float): Float {
        return ((b - a + 540f) % 360f) - 180f
    }

    // ----------- تنظیمات ذخیره در SettingsRepo مثل قبل -----------

    private fun launchIO(block: suspend () -> Unit) = viewModelScope.launch { block() }

    fun setTrueNorth(v: Boolean) = launchIO { settingsRepo.setUseTrueNorth(v) }
    fun setSmoothing(v: Float) = launchIO { settingsRepo.setSmoothing(v) }
    fun setTolerance(v: Double) = launchIO { settingsRepo.setAlignmentTolerance(v.toInt()) }
    fun setGpsPrompt(v: Boolean) = launchIO { settingsRepo.setShowGpsPrompt(v) }
    fun setMapType(v: Int) = launchIO { settingsRepo.setMapType(v) }
    fun setIranCities(v: Boolean) = launchIO { settingsRepo.setShowIranCities(v) }
    fun setVibration(v: Boolean) = launchIO { settingsRepo.setVibration(v) }
    fun setSound(v: Boolean) = launchIO { settingsRepo.setSound(v) }
    fun setBatterySaver(v: Boolean) = launchIO { settingsRepo.setBatterySaver(v) }
    fun setBgFreq(v: Int) = launchIO { settingsRepo.setBgFreqSec(v) }
    fun setLowPowerLocation(v: Boolean) = launchIO { settingsRepo.setLowPowerLocation(v) }
    fun setAutoCalibration(v: Boolean) = launchIO { settingsRepo.setAutoCalibration(v) }
    fun setCalibrationThreshold(v: Int) = launchIO { settingsRepo.setCalibrationThreshold(v) }
}