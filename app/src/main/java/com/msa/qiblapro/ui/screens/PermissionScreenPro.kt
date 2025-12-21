package com.msa.qiblapro.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.pro.ProBackground

@Composable
fun PermissionScreenPro(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val granted =
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        Log.d("PermissionScreenPro", "Initial check → granted=$granted")

        if (granted) {
            onPermissionGranted()
        }
    }

    ProBackground {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AppCard(Modifier.padding(20.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.permission_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.permission_message),
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) {
                        Text(stringResource(R.string.open_settings))
                    }
                }
            }
        }
    }
}

