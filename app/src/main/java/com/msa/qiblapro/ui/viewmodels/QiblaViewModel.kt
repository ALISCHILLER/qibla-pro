package com.msa.qiblapro.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.qiblapro.data.compass.CompassRepository
import com.msa.qiblapro.data.location.LocationRepository
import com.msa.qiblapro.data.settings.SettingsRepository
import com.msa.qiblapro.domain.QiblaMath
import com.msa.qiblapro.util.GpsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

data class QiblaUiState(
    val hasLocationPermission: Boolean = false,

    val lat: Double? = null,
    val lon: Double? = null,
    val accuracyM: Float? = null,

    val headingMagDeg: Float = 0f,
    val headingTrue: Float? = null,

    val qiblaDeg: Float = 0f,
    val distanceKm: Double = 0.0,

    val rotationToQibla: Float? = null,
    val facingQibla: Boolean = false,

    val needsCalibration: Boolean = false,

    val gpsEnabled: Boolean = true,
    val showGpsDialog: Boolean = false,

    // تنظیمات
    val useTrueNorth: Boolean = true,
    val smoothing: Float = 0.65f,
    val alignTolerance: Int = 6,
    val enableVibration: Boolean = true,
    val enableSound: Boolean = true,
    val mapType: Int = 1,
    val showIranCities: Boolean = true,
    val showGpsPrompt: Boolean = true,
    val batterySaverMode: Boolean = false,
    val bgUpdateFreqSec: Int = 5,
    val useLowPowerLocation: Boolean = true,
    val autoCalibration: Boolean = true,
    val calibrationThreshold: Int = 3,
)

@HiltViewModel
class QiblaViewModel @Inject constructor(
    private val locRepo: LocationRepository,
    private val compassRepo: CompassRepository,
    private val settingsRepo: SettingsRepository,
    @ApplicationContext private val appCtx: Context
) : ViewModel() {

    private val _permission = MutableStateFlow(false)
    fun setPermissionGranted(v: Boolean) { _permission.value = v }

    private val _state = MutableStateFlow(QiblaUiState())
    val state: StateFlow<QiblaUiState> = _state.asStateFlow()

    private var smoothHeading: Float = 0f

    init {
        // Combine: تنظیمات + قطب‌نما + مجوز
        viewModelScope.launch {
            combine(
                _permission,
                settingsRepo.settingsFlow,
                compassRepo.compassFlow()
                    .catch { emit(com.msa.qiblapro.data.compass.CompassReading(0f, 0)) }
                    .onStart { emit(com.msa.qiblapro.data.compass.CompassReading(0f, 0)) }
            ) { perm, settings, compass ->
                Triple(perm, settings, compass)
            }.collect { (perm, settings, compass) ->
                if (!perm) {
                    _state.update { it.copy(hasLocationPermission = false) }
                    return@collect
                }

                // Filter smoothing
                val alpha = settings.smoothing.coerceIn(0f, 1f)
                smoothHeading = if (smoothHeading == 0f) compass.headingMagneticDeg
                else lerpAngle(smoothHeading, compass.headingMagneticDeg, alpha)

                _state.update {
                    it.copy(
                        hasLocationPermission = true,
                        headingMagDeg = compass.headingMagneticDeg,
                        needsCalibration = settings.autoCalibration && (compass.accuracy == 0),

                        // تنظیمات
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

        // دریافت موقعیت مکانی و محاسبه قبله
        viewModelScope.launch {
            _permission.flatMapLatest { perm ->
                if (!perm) emptyFlow() else locRepo.locationFlow()
            }.collect { loc ->
                val settings = settingsRepo.settingsFlow.first()

                val qibla = QiblaMath.bearingToKaaba(loc.lat, loc.lon).toFloat()
                val dist = QiblaMath.distanceKmToKaaba(loc.lat, loc.lon)

                val decl = if (settings.useTrueNorth)
                    QiblaMath.declinationDeg(loc.lat, loc.lon, loc.alt, System.currentTimeMillis())
                else 0f

                val trueHeading = (smoothHeading + decl + 360f) % 360f
                val rotErr = QiblaMath.rotationErrorDeg(trueHeading, qibla)

                val facing = abs(rotErr) <= settings.alignmentToleranceDeg.coerceIn(2, 20)

                val gpsEnabled = GpsUtils.isLocationEnabled(appCtx)

                _state.update {
                    it.copy(
                        lat = loc.lat,
                        lon = loc.lon,
                        accuracyM = loc.accuracyM,
                        qiblaDeg = qibla,
                        distanceKm = dist,
                        headingTrue = trueHeading,
                        rotationToQibla = rotErr,
                        facingQibla = facing,
                        gpsEnabled = gpsEnabled,
                        showGpsDialog = (!gpsEnabled && settings.showGpsPrompt)
                    )
                }
            }
        }
    }

    private fun lerpAngle(a: Float, b: Float, t: Float): Float {
        val diff = ((b - a + 540f) % 360f) - 180f
        return (a + diff * t + 360f) % 360f
    }

    fun hideGpsDialog() = _state.update { it.copy(showGpsDialog = false) }

    // Setter ها برای تنظیمات

    fun setTrueNorth(v: Boolean) {
        viewModelScope.launch { settingsRepo.setUseTrueNorth(v) }
    }

    fun setSmoothing(v: Float) {
        viewModelScope.launch { settingsRepo.setSmoothing(v) }
    }

    fun setTolerance(v: Double) {
        viewModelScope.launch { settingsRepo.setAlignmentTolerance(v.toInt()) }
    }

    fun setGpsPrompt(v: Boolean) {
        viewModelScope.launch { settingsRepo.setShowGpsPrompt(v) }
    }

    fun setMapType(v: Int) {
        viewModelScope.launch { settingsRepo.setMapType(v) }
    }

    fun setIranCities(v: Boolean) {
        viewModelScope.launch { settingsRepo.setShowIranCities(v) }
    }

    fun setVibration(v: Boolean) {
        viewModelScope.launch { settingsRepo.setVibration(v) }
    }

    fun setSound(v: Boolean) {
        viewModelScope.launch { settingsRepo.setSound(v) }
    }

    fun setBatterySaver(v: Boolean) {
        viewModelScope.launch { settingsRepo.setBatterySaver(v) }
    }

    fun setBgFreq(v: Int) {
        viewModelScope.launch { settingsRepo.setBgFreqSec(v) }
    }

    fun setLowPowerLocation(v: Boolean) {
        viewModelScope.launch { settingsRepo.setLowPowerLocation(v) }
    }

    fun setAutoCalibration(v: Boolean) {
        viewModelScope.launch { settingsRepo.setAutoCalibration(v) }
    }

    fun setCalibrationThreshold(v: Int) {
        viewModelScope.launch { settingsRepo.setCalibrationThreshold(v) }
    }
}
