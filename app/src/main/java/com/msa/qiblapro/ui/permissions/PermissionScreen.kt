package com.msa.qiblapro.ui.permissions

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.AppCard
import com.msa.qiblapro.ui.pro.ProBackground
import com.msa.qiblapro.util.Permissions

@Composable
fun PermissionScreen(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var permanentlyDenied by remember { mutableStateOf(false) }
    var deniedOnce by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it }
        if (granted) {
            onPermissionGranted()
        } else {
            deniedOnce = true
            permanentlyDenied = activity?.let { isLocationPermissionPermanentlyDenied(it) } == true
        }
    }

    LaunchedEffect(Unit) {
        if (isLocationPermissionGranted(context)) onPermissionGranted()
    }

    val title = stringResource(R.string.permission_title)
    val msg = stringResource(R.string.permission_message)
    val allow = stringResource(R.string.grant_permission)
    val openSettings = stringResource(R.string.open_settings)

    ProBackground {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AppCard(Modifier.fillMaxWidth(0.92f)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    if (deniedOnce) {
                        Text(
                            text = if (permanentlyDenied)
                                stringResource(R.string.permission_denied_permanently)
                            else
                                stringResource(R.string.permission_denied),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (permanentlyDenied) {
                        OutlinedButton(
                            onClick = { openAppSettings(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(openSettings) }
                    } else {
                        Button(
                            onClick = { launcher.launch(Permissions.location) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(allow) }
                    }
                }
            }
        }
    }
}
