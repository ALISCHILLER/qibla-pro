package com.msa.qiblapro.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.navigation.compose.hiltViewModel
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.*
import com.msa.qiblapro.ui.viewmodels.QiblaViewModel
import com.msa.qiblapro.ui.widgets.CompassRose

@Composable
fun CompassScreen(vm: QiblaViewModel = hiltViewModel()) {
    val st by vm.state.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        val ok = (res[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (res[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        vm.setPermissionGranted(ok)
    }

    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
        vm.setPermissionGranted(fine || coarse)
    }

    if (!st.hasLocationPermission) {
        ProBackground(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).proShadow()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.permission_title), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(10.dp))
                        Text(stringResource(R.string.permission_message), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = {
                                val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(i)
                            }) { Text(stringResource(R.string.open_settings)) }

                            Button(onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }) { Text(stringResource(R.string.grant_permission)) }
                        }
                    }
                }
            }
        }
        return
    }

    // GPS Dialog
    GpsEnableDialog(
        visible = st.showGpsDialog,
        onDismiss = vm::hideGpsDialog,
        onEnabled = { vm.hideGpsDialog() }
    )

    val tolerance = 6f // از settings می‌تونی بخونی؛ فعلاً ساده
    val prox = proximity01(st.rotationDeg, tolerance)

    ProBackground(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FacingGlowPill(
                text = if (st.facingQibla) context.getString(R.string.facing_qibla)
                else context.getString(R.string.rotate_to_qibla, st.rotationDeg.toInt()),
                isFacing = st.facingQibla,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            GlassCard(
                modifier = Modifier.fillMaxWidth().height(360.dp).proShadow()
            ) {
                CompassRose(
                    modifier = Modifier.fillMaxSize(),
                    headingDeg = st.headingTrueDeg,
                    qiblaDeg = st.qiblaDeg,
                    needleModifier = NeedlePremiumModifier(prox, st.facingQibla)
                )
            }

            GlassCard(modifier = Modifier.fillMaxWidth().proShadow()) {
                InfoRow("Qibla", "${st.qiblaDeg.toInt()}°")
                Spacer(Modifier.height(8.dp))
                InfoRow("Distance", String.format("%.1f km", st.distanceKm))
                Spacer(Modifier.height(8.dp))
                InfoRow("Accuracy", st.accuracyM?.let { "${it.toInt()} m" } ?: "—")
            }
        }
    }
}

@Composable
private fun InfoRow(title: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = Color.White.copy(alpha = 0.75f))
        Text(value, color = Color.White)
    }
}
