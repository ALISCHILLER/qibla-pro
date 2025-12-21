package com.msa.qiblapro.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.pro.ProBackground

@Composable
fun PermissionScreen(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var denied by remember { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }

    // بررسی وضعیت مجوزها
    fun isPermissionGranted(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        Log.d("PermissionScreen", "Permission check → fine=$fine, coarse=$coarse")
        return fine || coarse
    }

    // لانچر درخواست مجوز
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val granted = fine || coarse

        Log.d("PermissionScreen", "Permission result → fine=$fine, coarse=$coarse, granted=$granted")

        if (granted) {
            Log.d("PermissionScreen", "Permission granted → calling callback")
            onPermissionGranted()
        } else {
            Log.w("PermissionScreen", "Permission denied")
            denied = true
            permanentlyDenied = result.entries.any { (permission, isGranted) ->
                !isGranted && activity != null &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            }
            Log.w("PermissionScreen", "permanentlyDenied=$permanentlyDenied")
        }
    }

    // بررسی اولیه فقط یکبار
    LaunchedEffect(Unit) {
        if (isPermissionGranted()) {
            Log.d("PermissionScreen", "Initial check → already granted → calling callback")
            onPermissionGranted()
        } else {
            Log.d("PermissionScreen", "Initial check → not granted → waiting for user action")
        }
    }

    // رابط کاربری
    ProBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.permission_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    Text(
                        text = stringResource(R.string.permission_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )

                    if (denied) {
                        Text(
                            text = if (permanentlyDenied)
                                stringResource(R.string.permission_denied_permanently)
                            else
                                stringResource(R.string.permission_denied),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    if (permanentlyDenied) {
                        Log.w("PermissionScreen", "User permanently denied permission → showing settings button")
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.open_settings))
                        }
                    } else {
                        Button(
                            onClick = {
                                Log.d("PermissionScreen", "Requesting permission again")
                                denied = false
                                permanentlyDenied = false
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.grant_permission))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "• ${
                            stringResource(R.string.permission_message)
                                .split("\n")
                                .getOrNull(1)
                                .orEmpty()
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
