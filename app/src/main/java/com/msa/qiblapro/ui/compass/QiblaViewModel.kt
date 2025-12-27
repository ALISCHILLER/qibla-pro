package com.msa.qiblapro.ui.compass

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.qiblapro.data.compass.CompassRepository
import com.msa.qiblapro.data.compass.CompassResult
import com.msa.qiblapro.data.location.LocationRepository
import com.msa.qiblapro.data.location.UserLocationResult
import com.msa.qiblapro.data.settings.AppSettings
import com.msa.qiblapro.data.settings.SettingsRepository
import com.msa.qiblapro.domain.qibla.AngleMath
import com.msa.qiblapro.domain.qibla.QiblaMath
import com.msa.qiblapro.domain.qibla.engine.QiblaEngine
import com.msa.qiblapro.domain.qibla.engine.QiblaEngineInput
import com.msa.qiblapro.ui.events.AppEvent
import com.msa.qiblapro.ui.settings.SettingsAction
import com.msa.qiblapro.util.GpsUtils
import com.msa.qiblapro.util.LanguageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    /**
     * ✅ settingsState را از settingsFlow می‌گیریم، و distinct را قبل از stateIn اعمال می‌کنیم
     * تا هم درست باشد، هم warning ندهد.
     */
    private val settingsState = settingsRepo.settingsFlow
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings()
        )

    private var lastHapticTimeMs: Long = 0L
    private var calibrationGuideDismissedUntilMs: Long = 0L

    init {
        observeSettings()
        observeLocation()
        observeCompass()
        observeGpsState()
    }

    private fun observeSettings() {
        settingsState
            .onEach { s ->
                _state.update {
                    it.copy(
                        themeMode = s.themeMode,
                        accent = s.accent,

                        // engine-related
                        useTrueNorth = s.useTrueNorth,
                        smoothing = s.smoothing,
                        alignTolerance = s.alignmentToleranceDeg,
                        autoCalibration = s.autoCalibration,
                        calibrationThreshold = s.calibrationThreshold,
                        batterySaverMode = s.batterySaverMode,

                        // feedback
                        enableVibration = s.enableVibration,
                        hapticStrength = s.hapticStrength,
                        hapticPattern = s.hapticPattern,
                        hapticCooldownMs = s.hapticCooldownMs,
                        enableSound = s.enableSound,

                        // map
                        mapType = s.mapType,
                        showIranCities = s.showIranCities,
                        neonMapStyle = s.neonMapStyle,

                        // ui
                        showGpsPrompt = s.showGpsPrompt,
                        languageCode = s.languageCode
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeLocation() {
        _permission
            .flatMapLatest { granted ->
                if (!granted) flowOf(UserLocationResult.PermissionDenied)
                else locRepo.locationFlow()
            }
            .catch {
                _state.update { it.copy(hasLocation = false) }
            }
            .combine(settingsState) { result, s -> result to s }
            .onEach { (result, s) ->
                when (result) {
                    is UserLocationResult.Ok -> {
                        val loc = result.loc
                        val qibla = QiblaMath.bearingToKaaba(loc.lat, loc.lon).toFloat()
                        val decl = if (s.useTrueNorth) {
                            QiblaMath.declinationDeg(
                                loc.lat,
                                loc.lon,
                                loc.alt,
                                System.currentTimeMillis()
                            )
                        } else {
                            0f
                        }

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

                    UserLocationResult.PermissionDenied -> {
                        _state.update {
                            it.copy(
                                hasLocation = false,
                                hasLocationPermission = false,
                                showGpsDialog = false
                            )
                        }
                    }

                    UserLocationResult.GpsDisabled -> {
                        _state.update {
                            it.copy(
                                hasLocation = false,
                                gpsEnabled = false,
                                showGpsDialog = it.hasLocationPermission && it.showGpsPrompt
                            )
                        }
                    }

                    is UserLocationResult.Error -> {
                        _state.update { it.copy(hasLocation = false) }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeCompass() {
        compassRestart
            .onStart { emit(Unit) }
            .flatMapLatest {
                _permission.combine(settingsState) { granted, s -> granted to s }
            }
            .flatMapLatest { (granted, s) ->
                if (!granted) {
                    emptyFlow()
                } else {
                    val delay = if (s.batterySaverMode) {
                        SensorManager.SENSOR_DELAY_UI
                    } else {
                        SensorManager.SENSOR_DELAY_GAME
                    }

                    compassRepo.compassFlow(delay)
                        .map { result -> result to s }
                        .catch {
                            _state.update { it.copy(isSensorAvailable = false) }
                        }
                }
            }
            .onEach { (result, s) ->
                when (result) {
                    is CompassResult.Success -> {
                        val reading = result.reading

                        val input = QiblaEngineInput(
                            rawHeadingDeg = reading.headingMagneticDeg,
                            qiblaBearingDeg = _state.value.qiblaBearingDeg,
                            declinationDeg = _state.value.declinationDeg,
                            // ✅ True North فقط وقتی location داریم معنی داره
                            useTrueNorth = s.useTrueNorth && _state.value.hasLocation,
                            smoothingFactor = s.smoothing,
                            alignmentTolerance = s.alignmentToleranceDeg,
                            sensorAccuracy = reading.accuracy,
                            autoCalibration = s.autoCalibration,
                            calibrationThreshold = s.calibrationThreshold
                        )

                        val out = engine.calculate(input)
                        val headingTrue = if (_state.value.hasLocation) {
                            if (input.useTrueNorth) out.headingDeg
                            else AngleMath.norm360(out.headingDeg + _state.value.declinationDeg)
                        } else {
                            null
                        }
                        handleHaptics(out.isFacing, s)

                        _state.update {
                            it.copy(
                                headingDeg = out.headingDeg,
                                headingTrue = headingTrue,
                                rotationErrorDeg = out.rotationErrorDeg,
                                isFacingQibla = out.isFacing,
                                needsCalibration = out.needsCalibration,
                                showCalibrationGuide = out.needsCalibration &&
                                        System.currentTimeMillis() >= calibrationGuideDismissedUntilMs,
                                isSensorAvailable = true
                            )
                        }
                    }

                    CompassResult.SensorUnavailable -> {
                        _state.update { it.copy(isSensorAvailable = false) }
                    }

                    is CompassResult.Failure -> {
                        _state.update { it.copy(isSensorAvailable = false) }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeGpsState() {
        gpsStateFlow()
            .onEach { (gpsEnabled, airplaneMode) ->
                _state.update {
                    it.copy(
                        gpsEnabled = gpsEnabled,
                        airplaneModeOn = airplaneMode,
                        showGpsDialog = it.hasLocationPermission && !gpsEnabled && it.showGpsPrompt
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun gpsStateFlow() = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val enabled = GpsUtils.isLocationEnabled(appCtx)
                val airplane = GpsUtils.isAirplaneModeOn(appCtx)
                trySend(enabled to airplane)
            }
        }

        val filter = IntentFilter().apply {
            addAction(android.location.LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction(android.location.LocationManager.MODE_CHANGED_ACTION)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }

        // اولین مقدار
        trySend(GpsUtils.isLocationEnabled(appCtx) to GpsUtils.isAirplaneModeOn(appCtx))

        appCtx.registerReceiver(receiver, filter)
        awaitClose { appCtx.unregisterReceiver(receiver) }
    }

    private fun handleHaptics(isFacing: Boolean, s: AppSettings) {
        val canDoAnything = s.enableVibration || s.enableSound
        if (!canDoAnything) return

        // edge trigger: فقط وقتی وارد حالت facing می‌شیم
        if (isFacing && !_state.value.isFacingQibla) {
            val now = System.currentTimeMillis()
            if (now - lastHapticTimeMs >= s.hapticCooldownMs) {
                lastHapticTimeMs = now
                if (s.enableVibration) {
                    _events.tryEmit(AppEvent.VibratePattern(s.hapticStrength, s.hapticPattern))
                }
                if (s.enableSound) {
                    _events.tryEmit(AppEvent.Beep)
                }
            }
        }
    }

    fun onSettingsAction(action: SettingsAction) {
        viewModelScope.launch {
            when (action) {
                is SettingsAction.SetThemeMode -> settingsRepo.setThemeMode(action.mode)
                is SettingsAction.SetAccent -> settingsRepo.setAccent(action.accent)

                is SettingsAction.SetLanguage -> {
                    val target = LanguageHelper.normalizeLanguageTag(action.langCode)
                    settingsRepo.setLanguageCode(target)
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

    fun hideCalibrationSheet() {
        _state.update { it.copy(showCalibrationSheet = false) }
    }

    fun requestCalibrationGuide() {
        _state.update { it.copy(showCalibrationGuide = true, showCalibrationSheet = false) }
    }

    fun hideGpsDialog() {
        _state.update { it.copy(showGpsDialog = false) }
    }

    fun requestCalibration() {
        _state.update { it.copy(showCalibrationSheet = true) }
    }
}
