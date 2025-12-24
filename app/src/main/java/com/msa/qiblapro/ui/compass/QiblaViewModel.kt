package com.msa.qiblapro.ui.compass

import android.content.Context
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.qiblapro.data.compass.CompassRepository
import com.msa.qiblapro.data.location.LocationRepository
import com.msa.qiblapro.data.settings.AppSettings
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

    // ✅ آخرین تنظیمات برای استفاده داخل observeCompass/observeLocation بدون combine تکراری
    private var latestSettings: AppSettings = AppSettings()

    private var lastHapticTimeMs: Long = 0L
    private var calibrationGuideDismissedUntilMs: Long = 0L

    init {
        observeSettings()
        observeLocation()
        observeCompass()
    }

    private fun observeSettings() {
        settingsRepo.settingsFlow
            .distinctUntilChanged()
            .onEach { s ->
                latestSettings = s

                _state.update {
                    it.copy(
                        themeMode = s.themeMode,
                        accent = s.accent,
                        useTrueNorth = s.useTrueNorth,
                        smoothing = s.smoothing,
                        alignTolerance = s.alignmentToleranceDeg,
                        hapticStrength = s.hapticStrength,
                        hapticPattern = s.hapticPattern,
                        hapticCooldownMs = s.hapticCooldownMs,
                        languageCode = s.languageCode,
                        showGpsPrompt = s.showGpsPrompt,
                        autoCalibration = s.autoCalibration,
                        calibrationThreshold = s.calibrationThreshold,
                        batterySaverMode = s.batterySaverMode,
                        neonMapStyle = s.neonMapStyle,
                        showIranCities = s.showIranCities,
                        mapType = s.mapType,
                        enableVibration = s.enableVibration,
                        enableSound = s.enableSound
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeLocation() {
        _permission
            .flatMapLatest { granted ->
                if (!granted) emptyFlow()
                else locRepo.locationFlow()
            }
            .onEach { loc ->
                val s = latestSettings

                val qibla = QiblaMath.bearingToKaaba(loc.lat, loc.lon).toFloat()
                val decl = if (s.useTrueNorth) {
                    QiblaMath.declinationDeg(loc.lat, loc.lon, loc.alt, System.currentTimeMillis())
                } else 0f

                _state.update {
                    it.copy(
                        hasLocation = true,
                        userLat = loc.lat,
                        userLon = loc.lon,
                        locationAccuracyM = loc.accuracyM,
                        qiblaBearingDeg = qibla,
                        declinationDeg = decl,
                        distanceKm = QiblaMath.distanceKmToKaaba(loc.lat, loc.lon),
                        gpsEnabled = GpsUtils.isLocationEnabled(appCtx)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeCompass() {
        compassRestart
            .onStart { emit(Unit) }
            .flatMapLatest {
                _permission.map { granted -> granted }
            }
            .flatMapLatest { granted ->
                if (!granted) emptyFlow()
                else {
                    val s = latestSettings
                    val delay =
                        if (s.batterySaverMode) SensorManager.SENSOR_DELAY_UI else SensorManager.SENSOR_DELAY_GAME

                    compassRepo.compassFlow(delay)
                        .catch {
                            _state.update { it.copy(isSensorAvailable = false) }
                        }
                }
            }
            .onEach { reading ->
                val s = latestSettings

                val input = QiblaEngineInput(
                    rawHeadingDeg = reading.headingMagneticDeg,
                    qiblaBearingDeg = _state.value.qiblaBearingDeg,
                    declinationDeg = _state.value.declinationDeg,
                    useTrueNorth = s.useTrueNorth,
                    smoothingFactor = s.smoothing,
                    alignmentTolerance = s.alignmentToleranceDeg,
                    sensorAccuracy = reading.accuracy,
                    autoCalibration = s.autoCalibration,
                    calibrationThreshold = s.calibrationThreshold
                )

                val out = engine.calculate(input)

                handleHaptics(out.isFacing, s)

                _state.update {
                    it.copy(
                        headingDeg = out.headingDeg,
                        rotationErrorDeg = out.rotationErrorDeg,
                        isFacingQibla = out.isFacing,
                        needsCalibration = out.needsCalibration,
                        showCalibrationGuide = out.needsCalibration &&
                                System.currentTimeMillis() >= calibrationGuideDismissedUntilMs,
                        isSensorAvailable = true
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleHaptics(isFacing: Boolean, s: AppSettings) {
        // ✅ اگر ویبره خاموشه، هیچ event نده
        if (!s.enableVibration) return

        if (isFacing && !_state.value.isFacingQibla) {
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
                    // ✅ Normalize برای جلوگیری از fa vs fa-IR mismatch
                    val target = LanguageHelper.normalizeLanguageTag(action.langCode)

                    settingsRepo.setLanguageCode(target)
                    LanguageHelper.applyLanguage(target)

                    // ✅ به UI/Activity بگو باید recreate بشه تا strings دوباره load بشن
                    // (این event رو باید به AppEvent اضافه کنی)
                    _events.tryEmit(AppEvent.RecreateActivity)
                }

                is SettingsAction.SetUseTrueNorth -> settingsRepo.setUseTrueNorth(action.v)
                is SettingsAction.SetSmoothing -> settingsRepo.setSmoothing(action.v)
                is SettingsAction.SetAlignmentTol -> settingsRepo.setAlignmentTolerance(action.v)

                is SettingsAction.SetVibration -> settingsRepo.setVibration(action.v)
                is SettingsAction.SetHapticStrength -> settingsRepo.setHapticStrength(action.v)
                is SettingsAction.SetHapticPattern -> settingsRepo.setHapticPattern(action.v)
                is SettingsAction.SetHapticCooldown -> settingsRepo.setHapticCooldown(action.v)
                is SettingsAction.SetSound -> settingsRepo.setSound(action.v)

                is SettingsAction.SetMapType -> settingsRepo.setMapType(action.v)
                is SettingsAction.SetIranCities -> settingsRepo.setShowIranCities(action.v)
                is SettingsAction.SetShowGpsPrompt -> settingsRepo.setShowGpsPrompt(action.v)
                is SettingsAction.SetBatterySaver -> settingsRepo.setBatterySaver(action.v)
                is SettingsAction.SetBgUpdateFreq -> settingsRepo.setBgFreqSec(action.v)
                is SettingsAction.SetLowPowerLoc -> settingsRepo.setLowPowerLocation(action.v)
                is SettingsAction.SetAutoCalib -> settingsRepo.setAutoCalibration(action.v)
                is SettingsAction.SetCalibThreshold -> settingsRepo.setCalibrationThreshold(action.v)

                else -> Unit
            }
        }
    }

    /**
     * ✅ mapType در AppSettings از نوع Int است.
     * مقادیر معتبر 1..4 هستند (در SettingsRepository هم coerce می‌شوند).
     */
    fun setMapType(v: Int) {
        viewModelScope.launch { settingsRepo.setMapType(v) }
    }

    fun setPermissionGranted(v: Boolean) {
        _permission.value = v
        _state.update {
            val gpsEnabled = GpsUtils.isLocationEnabled(appCtx)
            it.copy(
                hasLocationPermission = v,
                gpsEnabled = gpsEnabled,
                showGpsDialog = v && !gpsEnabled && it.showGpsPrompt
            )
        }
    }

    fun restartCompass() {
        engine.reset()
        compassRestart.tryEmit(Unit)
    }

    fun dismissCalibrationGuide() {
        calibrationGuideDismissedUntilMs = System.currentTimeMillis() + 10 * 60 * 1000
        _state.update { it.copy(showCalibrationGuide = false) }
    }

    fun hideGpsDialog() { _state.update { it.copy(showGpsDialog = false) } }

    fun requestCalibration() { _state.update { it.copy(showCalibrationSheet = true) } }
}
