package com.msa.qiblapro.ui.compass

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.compass.components.*
import com.msa.qiblapro.ui.compass.widgets.CompassRose
import com.msa.qiblapro.ui.events.AppEvent
import com.msa.qiblapro.ui.gps.GpsEnableDialog
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.pro.FacingGlowPill
import com.msa.qiblapro.util.GpsUtils
import com.msa.qiblapro.util.haptics.Haptics
import com.msa.qiblapro.util.haptics.SoundFx
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun CompassScreen(
    vm: QiblaViewModel,
    onNavigateToMap: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Sound FX (release on dispose)
    val soundFx = remember { SoundFx() }
    DisposableEffect(Unit) { onDispose { soundFx.release() } }

    // Events (vibrate/beep)
    LaunchedEffect(Unit) {
        vm.events.collect { ev ->
            when (ev) {
                is AppEvent.VibratePattern -> Haptics.vibrate(context, ev.strength, ev.pattern)
                AppEvent.Beep -> soundFx.beep()
                else -> {}
            }
        }
    }

    // GPS/Airplane Snackbars
    var lastGpsOk by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(state.gpsEnabled, state.hasLocationPermission) {
        if (!state.hasLocationPermission) return@LaunchedEffect
        val gpsOk = state.gpsEnabled && GpsUtils.isLocationEnabled(context)
        if (!gpsOk && lastGpsOk) {
            lastGpsOk = false
            scope.launch {
                val res = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.gps_off_snackbar_title),
                    actionLabel = context.getString(R.string.gps_off_snackbar_action),
                    duration = SnackbarDuration.Long
                )
                if (res == SnackbarResult.ActionPerformed) GpsUtils.openLocationSettings(context)
            }
        }
        if (gpsOk) lastGpsOk = true
    }

    if (state.showCalibrationGuide) {
        CalibrationGuideSheet(onDismiss = { vm.dismissCalibrationGuide() })
    }

    GpsEnableDialog(
        visible = state.showGpsDialog,
        onDismiss = vm::hideGpsDialog,
        onEnabled = vm::hideGpsDialog
    )

    val targetHeading = state.headingDeg % 360f
    val animatedHeading = rememberAngleAnim(targetHeading)

    CompassScaffold(snackbarHostState = snackbarHostState) {
        CompassHeader(languageLabel = stringResource(R.string.language_label))

        if (state.isSensorAvailable && state.needsCalibration) {
            AppCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.calibration_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.calibration_hint), style = MaterialTheme.typography.bodyMedium)
            }
        }

        FacingGlowPill(
            text = if (state.isFacingQibla) stringResource(R.string.facing_qibla) 
                   else stringResource(R.string.rotate_to_qibla, abs(state.rotationErrorDeg).toInt()),
            isFacing = state.isFacingQibla && state.isSensorAvailable,
            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.92f)
        )

        AppCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CompassRose(
                    modifier = Modifier.fillMaxSize(0.95f),
                    headingDeg = if (state.isSensorAvailable) animatedHeading else 0f,
                    qiblaDeg = state.qiblaDeg,
                    isFacingQibla = state.isFacingQibla && state.isSensorAvailable,
                    onRefresh = vm::restartCompass
                )
            }
        }

        CompassStatusRow(state = state)

        CompassActionsRow(
            onCalibration = { vm.requestCalibration() },
            onSettings = onNavigateToSettings
        )

        if (!state.isSensorAvailable) {
            NoCompassOverlay(onTryAgain = { vm.restartCompass() }, onUseMap = onNavigateToMap)
        }
    }
}

@Composable
private fun rememberAngleAnim(targetDeg: Float): Float {
    val anim = remember { Animatable(targetDeg) }
    LaunchedEffect(targetDeg) {
        val current = anim.value
        val delta = ((targetDeg - current + 540f) % 360f) - 180f
        anim.animateTo(current + delta, spring(dampingRatio = 0.85f, stiffness = 320f))
    }
    return anim.value
}
