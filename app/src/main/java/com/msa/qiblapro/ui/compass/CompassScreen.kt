package com.msa.qiblapro.ui.compass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
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

    // --- Strings (به جای context.getString) ---
    val gpsOffMsg = stringResource(R.string.gps_off_snackbar_title)
    val gpsOffAction = stringResource(R.string.gps_off_snackbar_action)

    // Sound FX (release on dispose)
    val soundFx = remember { SoundFx() }
    DisposableEffect(Unit) { onDispose { soundFx.release() } }

    // Events (vibrate/beep)
    LaunchedEffect(Unit) {
        vm.events.collect { ev ->
            when (ev) {
                is AppEvent.VibratePattern -> Haptics.vibrate(context, ev.strength, ev.pattern)
                AppEvent.Beep -> soundFx.beep()
                else -> Unit
            }
        }
    }

    // GPS Snackbars
    var lastGpsOk by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(state.gpsEnabled, state.hasLocationPermission) {
        if (!state.hasLocationPermission) return@LaunchedEffect

        val gpsOk = state.gpsEnabled && GpsUtils.isLocationEnabled(context)
        if (!gpsOk && lastGpsOk) {
            lastGpsOk = false
            scope.launch {
                val res = snackbarHostState.showSnackbar(
                    message = gpsOffMsg,
                    actionLabel = gpsOffAction,
                    duration = SnackbarDuration.Long
                )
                if (res == SnackbarResult.ActionPerformed) {
                    GpsUtils.openLocationSettings(context)
                }
            }
        }
        if (gpsOk) lastGpsOk = true
    }

    // Calibration guide sheet
    if (state.showCalibrationGuide) {
        CalibrationGuideSheet(onDismiss = { vm.dismissCalibrationGuide() })
    }

    // GPS enable dialog
    GpsEnableDialog(
        visible = state.showGpsDialog,
        onDismiss = vm::hideGpsDialog,
        onEnabled = vm::hideGpsDialog
    )

    // Heading animation
    val targetHeading = (state.headingDeg % 360f)
    val animatedHeading = rememberAngleAnim(targetHeading)

    CompassScaffold(snackbarHostState = snackbarHostState) {
        // Header
        CompassHeader(languageLabel = stringResource(R.string.language_label))

        // Calibration hint card
        if (state.isSensorAvailable && state.needsCalibration) {
            AppCard(Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.calibration_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.calibration_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Facing pill
        FacingGlowPill(
            text = if (state.isFacingQibla) {
                stringResource(R.string.facing_qibla)
            } else {
                stringResource(R.string.rotate_to_qibla, abs(state.rotationErrorDeg).toInt())
            },
            isFacing = state.isFacingQibla && state.isSensorAvailable,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.92f)
        )

        // Compass card
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CompassRose(
                    modifier = Modifier.fillMaxSize(0.95f),
                    headingDeg = if (state.isSensorAvailable) animatedHeading else 0f,
                    qiblaDeg = state.qiblaDeg,
                    isFacingQibla = state.isFacingQibla && state.isSensorAvailable,
                    onRefresh = vm::restartCompass
                )
            }
        }

        // Status row
        CompassStatusRow(state = state)

        // Actions row
        CompassActionsRow(
            onCalibration = { vm.requestCalibration() },
            onSettings = onNavigateToSettings
        )

        // No compass overlay
        if (!state.isSensorAvailable) {
            NoCompassOverlay(
                onTryAgain = vm::restartCompass,
                onUseMap = onNavigateToMap
            )
        }
    }
}

@Composable
private fun rememberAngleAnim(targetDeg: Float): Float {
    val anim = remember { Animatable(targetDeg) }

    LaunchedEffect(targetDeg) {
        val current = anim.value
        val delta = ((targetDeg - current + 540f) % 360f) - 180f
        anim.animateTo(
            targetValue = current + delta,
            animationSpec = spring(dampingRatio = 0.85f, stiffness = 320f)
        )
    }

    return anim.value
}
