package com.msa.qiblapro.ui.compass

import android.content.Context
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.qiblapro.data.compass.CompassRepository
import com.msa.qiblapro.data.location.LocationRepository
import com.msa.qiblapro.data.settings.SettingsRepository
import com.msa.qiblapro.domain.qibla.QiblaMath
import com.msa.qiblapro.domain.qibla.engine.QiblaEngine
import com.msa.qiblapro.domain.qibla.engine.QiblaEngineInput
import com.msa.qiblapro.ui.events.AppEvent
import com.msa.qiblapro.ui.settings.SettingsAction
import com.msa.qiblapro.util.GpsUtils
import com.msa.qiblapro.util.LanguageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private val compassRestart = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val engine = QiblaEngine()
    
    private var lastHapticTimeMs: Long = 0L
    private var calibrationGuideDismissedUntilMs: Long = 0L

    init {
        observeSettings()
        observeLocation()
        observeCompass()
    }

    private fun observeSettings() {
        settingsRepo.settingsFlow
            .onEach { s ->
                _state.update { it.copy(
                    themeMode = s.themeMode, 
                    accent = s.accent,
                    useTrueNorth = s.useTrueNorth,
                    smoothing = s.smoothing,
                    alignTolerance = s.alignmentToleranceDeg,
                    hapticStrength = s.hapticStrength,
                    hapticPattern = s.hapticPattern,
                    hapticCooldownMs = s.hapticCooldownMs,
                    languageCode = s.languageCode
                ) }
            }.launchIn(viewModelScope)
    }

    private fun observeLocation() {
        _permission
            .flatMapLatest { granted ->
                if (!granted) emptyFlow()
                else combine(locRepo.locationFlow(), settingsRepo.settingsFlow) { l, s -> l to s }
            }
            .onEach { (loc, s) ->
                val qibla = QiblaMath.bearingToKaaba(loc.lat, loc.lon).toFloat()
                val decl = if (s.useTrueNorth) QiblaMath.declinationDeg(loc.lat, loc.lon, loc.alt, System.currentTimeMillis()) else 0f
                
                _state.update { it.copy(
                    hasLocation = true,
                    lat = loc.lat, lon = loc.lon, 
                    qiblaDeg = qibla, declinationDeg = decl,
                    distanceKm = QiblaMath.distanceKmToKaaba(loc.lat, loc.lon),
                    gpsEnabled = GpsUtils.isLocationEnabled(appCtx)
                ) }
            }.launchIn(viewModelScope)
    }

    private fun observeCompass() {
        compassRestart.onStart { emit(Unit) }
            .flatMapLatest { combine(_permission, settingsRepo.settingsFlow) { p, s -> p to s } }
            .flatMapLatest { (granted, s) ->
                if (!granted) emptyFlow()
                else {
                    val delay = if (s.batterySaverMode) SensorManager.SENSOR_DELAY_UI else SensorManager.SENSOR_DELAY_GAME
                    compassRepo.compassFlow(delay).catch { _state.update { it.copy(isSensorAvailable = false) } }
                }
            }
            .onEach { reading ->
                val s = settingsRepo.settingsFlow.first()
                val input = QiblaEngineInput(
                    rawHeadingDeg = reading.headingMagneticDeg,
                    qiblaBearingDeg = _state.value.qiblaDeg,
                    declinationDeg = _state.value.declinationDeg,
                    useTrueNorth = s.useTrueNorth,
                    smoothingFactor = s.smoothing,
                    alignmentTolerance = s.alignmentToleranceDeg,
                    sensorAccuracy = reading.accuracy
                )
                val out = engine.calculate(input)
                
                handleHaptics(out.isFacing, s)
                
                _state.update { it.copy(
                    headingMagDeg = out.headingDeg,
                    facingQibla = out.isFacing,
                    needsCalibration = out.needsCalibration,
                    showCalibrationGuide = out.needsCalibration && System.currentTimeMillis() >= calibrationGuideDismissedUntilMs
                ) }
            }.launchIn(viewModelScope)
    }

    private fun handleHaptics(isFacingNow: Boolean, s: com.msa.qiblapro.data.settings.AppSettings) {
        if (isFacingNow && !_state.value.facingQibla) {
            val now = System.currentTimeMillis()
            if (now - lastHapticTimeMs >= s.hapticCooldownMs) {
                lastHapticTimeMs = now
                _events.tryEmit(AppEvent.VibratePattern(s.hapticStrength, s.hapticPattern))
            }
        }
    }

    fun onSettingsAction(action: SettingsAction) {
        viewModelScope.launch {
            when (action) {
                is SettingsAction.SetThemeMode -> settingsRepo.setThemeMode(action.mode)
                is SettingsAction.SetAccent -> settingsRepo.setAccent(action.accent)
                is SettingsAction.SetLanguage -> {
                    settingsRepo.setLanguageCode(action.langCode)
                    LanguageHelper.applyLanguage(appCtx, action.langCode)
                }
                is SettingsAction.SetUseTrueNorth -> settingsRepo.setUseTrueNorth(action.enabled)
                is SettingsAction.SetSmoothing -> settingsRepo.setSmoothing(action.factor)
                is SettingsAction.SetAlignmentTolerance -> settingsRepo.setAlignmentTolerance(action.degrees)
                is SettingsAction.SetVibration -> settingsRepo.setVibration(action.enabled)
                is SettingsAction.SetHapticStrength -> settingsRepo.setHapticStrength(action.strength)
                is SettingsAction.SetHapticPattern -> settingsRepo.setHapticPattern(action.pattern)
                is SettingsAction.SetHapticCooldown -> settingsRepo.setHapticCooldown(action.ms)
                is SettingsAction.SetSound -> settingsRepo.setSound(action.enabled)
                is SettingsAction.SetMapType -> settingsRepo.setMapType(action.v)
                is SettingsAction.SetIranCities -> settingsRepo.setShowIranCities(action.v)
                else -> {}
            }
        }
    }

    fun setPermissionGranted(v: Boolean) { _permission.value = v }
    fun restartCompass() { engine.reset(); compassRestart.tryEmit(Unit) }
    fun dismissCalibrationGuide() { 
        calibrationGuideDismissedUntilMs = System.currentTimeMillis() + 10 * 60 * 1000
        _state.update { it.copy(showCalibrationGuide = false) }
    }
    fun hideGpsDialog() { _state.update { it.copy(showGpsDialog = false) } }
    fun requestCalibration() { _state.update { it.copy(showCalibrationSheet = true) } }
}
