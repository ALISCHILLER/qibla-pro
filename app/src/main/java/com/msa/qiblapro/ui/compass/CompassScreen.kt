package com.msa.qiblapro.ui.compass

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.pro.FacingGlowPill
import com.msa.qiblapro.ui.compass.widgets.CompassRose
import com.msa.qiblapro.ui.gps.GpsEnableDialog
import com.msa.qiblapro.ui.permissions.PermissionScreenPro
import com.msa.qiblapro.util.GpsUtils
import com.msa.qiblapro.util.LanguageHelper
import kotlinx.coroutines.launch

@Composable
fun CompassScreen(
    vm: QiblaViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val granted = fine || coarse
        vm.setPermissionGranted(granted)
    }

    LaunchedEffect(Unit) {
        val granted = isLocationPermissionGranted(context)
        vm.setPermissionGranted(granted)

        if (!granted && !hasRequestedPermission) {
            hasRequestedPermission = true
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val gpsOffTitle = stringResource(R.string.gps_off_snackbar_title)
    val gpsOffAction = stringResource(R.string.gps_off_snackbar_action)
    val airplaneMsg = stringResource(R.string.airplane_mode_on_msg)
    val languageLabel = stringResource(R.string.language_label)

    LaunchedEffect(state.gpsEnabled, state.hasLocationPermission, gpsOffTitle, gpsOffAction, airplaneMsg) {
        if (!state.hasLocationPermission) return@LaunchedEffect

        if (!state.gpsEnabled || !GpsUtils.isLocationEnabled(context)) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = gpsOffTitle,
                    actionLabel = gpsOffAction,
                    duration = SnackbarDuration.Long
                )
            }
        } else if (GpsUtils.isAirplaneModeOn(context)) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = airplaneMsg,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    if (!state.hasLocationPermission) {
        PermissionScreenPro(onPermissionGranted = {
            val granted = isLocationPermissionGranted(context)
            vm.setPermissionGranted(granted)
        })
        return
    }

    GpsEnableDialog(
        visible = state.showGpsDialog,
        onDismiss = vm::hideGpsDialog,
        onEnabled = vm::hideGpsDialog
    )

    val currentLangCode = LanguageHelper.getCurrentLanguage(context)
    val currentFlag = LanguageHelper.getFlagEmoji(currentLangCode)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // نمایش زبان
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
                Text(
                    text = currentFlag,
                    fontSize = 18.sp
                )
            }

            FacingGlowPill(
                text = if (state.facingQibla)
                    stringResource(R.string.facing_qibla)
                else
                    stringResource(
                        R.string.rotate_to_qibla,
                        state.rotationToQibla?.toInt() ?: 0
                    ),
                isFacing = state.facingQibla,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.85f)
            )

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
                        headingDeg = state.headingTrue ?: state.headingMagDeg,
                        qiblaDeg = state.qiblaDeg,
                        isFacingQibla = state.facingQibla,
                        onRefresh = { vm.refreshSensors() }
                    )
                }
            }

            AppCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InfoRow(
                        title = stringResource(R.string.qibla_direction),
                        value = "${state.qiblaDeg.toInt()}°"
                    )
                    InfoRow(
                        title = stringResource(R.string.distance_to_kaaba),
                        value = String.format("%.1f km", state.distanceKm)
                    )
                    InfoRow(
                        title = stringResource(R.string.accuracy),
                        value = state.accuracyM?.let { "${it.toInt()} m" } ?: "—"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun isLocationPermissionGranted(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    return fine || coarse
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
