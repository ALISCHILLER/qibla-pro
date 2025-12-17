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
    val headingTrueDeg: Float = 0f,

    val qiblaDeg: Float = 0f,
    val distanceKm: Double = 0.0,

    val rotationDeg: Float = 0f,       // error [-180..180]
    val facingQibla: Boolean = false,

    val needsCalibration: Boolean = false,

    val gpsEnabled: Boolean = true,
    val showGpsDialog: Boolean = false
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
                _state.update { it.copy(hasLocationPermission = true) }

                val alpha = settings.smoothing.coerceIn(0f, 1f)
                smoothHeading = if (smoothHeading == 0f) compass.headingMagneticDeg
                else lerpAngle(smoothHeading, compass.headingMagneticDeg, alpha)

                _state.update {
                    it.copy(
                        headingMagDeg = compass.headingMagneticDeg,
                        needsCalibration = settings.autoCalibration && (compass.accuracy == 0)
                    )
                }
            }
        }

        // location stream (only when permission)
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

                _state.update {
                    it.copy(
                        lat = loc.lat,
                        lon = loc.lon,
                        accuracyM = loc.accuracyM,
                        qiblaDeg = qibla,
                        distanceKm = dist,
                        headingTrueDeg = trueHeading,
                        rotationDeg = rotErr,
                        facingQibla = facing
                    )
                }

                val gpsEnabled = GpsUtils.isLocationEnabled(appCtx)
                _state.update {
                    it.copy(
                        gpsEnabled = gpsEnabled,
                        showGpsDialog = (!gpsEnabled && settings.showGpsPrompt)
                    )
                }
            }
        }
    }

    fun hideGpsDialog() = _state.update { it.copy(showGpsDialog = false) }

    private fun lerpAngle(a: Float, b: Float, t: Float): Float {
        val diff = ((b - a + 540f) % 360f) - 180f
        return (a + diff * t + 360f) % 360f
    }
}
