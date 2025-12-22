package com.msa.qiblapro.ui.compass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.compass.widgets.CompassRose
import com.msa.qiblapro.ui.events.AppEvent
import com.msa.qiblapro.ui.gps.GpsEnableDialog
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.pro.FacingGlowPill
import com.msa.qiblapro.util.GpsUtils
import com.msa.qiblapro.util.LanguageHelper
import com.msa.qiblapro.util.haptics.Haptics
import com.msa.qiblapro.util.haptics.SoundFx
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun CompassScreen(vm: QiblaViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Sound FX (release on dispose)
    val soundFx = remember { SoundFx() }
    DisposableEffect(Unit) { onDispose { soundFx.release() } }

    // Events (beep/vibrate edge-trigger already in VM)
    LaunchedEffect(Unit) {
        vm.events.collect { ev ->
            when (ev) {
                AppEvent.Vibrate -> Haptics.vibrate(context, 35)
                AppEvent.Beep -> soundFx.beep()
                is AppEvent.Snack -> snackbarHostState.showSnackbar(ev.msg)
            }
        }
    }

    // Snackbar edge-trigger برای GPS/Airplane
    var lastGpsOk by rememberSaveable { mutableStateOf(true) }
    var lastAirplane by rememberSaveable { mutableStateOf(false) }

    val gpsOffTitle = stringResource(R.string.gps_off_snackbar_title)
    val gpsOffAction = stringResource(R.string.gps_off_snackbar_action)
    val airplaneMsg = stringResource(R.string.airplane_mode_on_msg)
    val languageLabel = stringResource(R.string.language_label)

    LaunchedEffect(state.gpsEnabled, state.hasLocationPermission, gpsOffTitle, gpsOffAction, airplaneMsg) {
        if (!state.hasLocationPermission) return@LaunchedEffect

        val gpsOk = state.gpsEnabled && GpsUtils.isLocationEnabled(context)
        val airplaneOn = GpsUtils.isAirplaneModeOn(context)

        if (!gpsOk && lastGpsOk) {
            lastGpsOk = false
            scope.launch {
                val res = snackbarHostState.showSnackbar(
                    message = gpsOffTitle,
                    actionLabel = gpsOffAction,
                    duration = SnackbarDuration.Long
                )
                if (res == SnackbarResult.ActionPerformed) {
                    GpsUtils.openLocationSettings(context)
                }
            }
        }
        if (gpsOk) lastGpsOk = true

        if (airplaneOn && !lastAirplane) {
            lastAirplane = true
            scope.launch { snackbarHostState.showSnackbar(airplaneMsg, duration = SnackbarDuration.Long) }
        }
        if (!airplaneOn) lastAirplane = false
    }

    // اگر Permission route درست باشه معمولاً اینجا نمیاد؛ ولی safe-guard:
    if (!state.hasLocationPermission) {
        AppCard(Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.permission_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.permission_message), style = MaterialTheme.typography.bodyMedium)
            }
        }
        return
    }

    GpsEnableDialog(
        visible = state.showGpsDialog,
        onDismiss = vm::hideGpsDialog,
        onEnabled = vm::hideGpsDialog
    )

    val currentLangCode = LanguageHelper.getCurrentLanguage(context)
    val currentFlag = LanguageHelper.getFlagEmoji(currentLangCode)

    val targetHeading = (state.headingTrue ?: state.headingMagDeg) % 360f
    val animatedHeading = rememberAngleAnim(targetHeading)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$languageLabel: ",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(text = currentFlag, fontSize = 18.sp)
            }

            if (state.needsCalibration) {
                AppCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(stringResource(R.string.calibration_title), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(R.string.calibration_hint), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            FacingGlowPill(
                text = if (state.facingQibla) {
                    stringResource(R.string.facing_qibla)
                } else {
                    stringResource(R.string.rotate_to_qibla, abs(state.rotationToQibla ?: 0f).toInt())
                },
                isFacing = state.facingQibla,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.92f)
            )

            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CompassRose(
                        modifier = Modifier.fillMaxSize(0.95f),
                        headingDeg = animatedHeading,
                        qiblaDeg = state.qiblaDeg,
                        isFacingQibla = state.facingQibla,
                        onRefresh = vm::refreshSensors
                    )
                }
            }

            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoRow(stringResource(R.string.qibla_direction), "${state.qiblaDeg.toInt()}°")
                    InfoRow(stringResource(R.string.distance_to_kaaba), String.format("%.1f km", state.distanceKm))
                    InfoRow(stringResource(R.string.accuracy), state.accuracyM?.let { "${it.toInt()} m" } ?: "—")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun rememberAngleAnim(targetDeg: Float): Float {
    val anim = remember { Animatable(targetDeg) }
    LaunchedEffect(targetDeg) {
        val current = anim.value
        val delta = shortestDelta(current, targetDeg)
        val newTarget = (current + delta + 360f) % 360f
        anim.animateTo(
            targetValue = newTarget,
            animationSpec = spring(dampingRatio = 0.85f, stiffness = 320f)
        )
    }
    return anim.value
}

private fun shortestDelta(from: Float, to: Float): Float {
    return ((to - from + 540f) % 360f) - 180f
}
