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
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.*

@Composable
fun PermissionScreen(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
        return fine || coarse
    }

    var denied by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val ok = (result[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (result[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        if (ok) onPermissionGranted() else denied = true
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission()) onPermissionGranted()
    }

    ProBackground(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .proShadow(),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.permission_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.permission_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))

                    if (denied) {
                        Text(
                            text = stringResource(R.string.permission_denied),
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(i)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text(stringResource(R.string.open_settings))
                        }
                        Button(onClick = {
                            launcher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }) {
                            Text(stringResource(R.string.grant_permission))
                        }
                    }
                }
            }
        }
    }
}
