package com.msa.qiblapro.ui.screens

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.msa.qiblapro.R
import com.msa.qiblapro.util.GpsUtils

@Composable
fun GpsEnableDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onEnabled: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Check status again on return
        if (GpsUtils.isLocationEnabled(context)) onEnabled()
    }

    if (!visible) return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.padding(top = 10.dp))

                Text(
                    text = stringResource(R.string.gps_disabled_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.padding(top = 6.dp))

                Text(
                    text = stringResource(R.string.gps_disabled_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.padding(top = 14.dp))

                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(stringResource(R.string.later))
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            if (activity != null) launcher.launch(intent) else onDismiss()
                        }
                    ) {
                        Icon(Icons.Filled.LocationOn, null)
                        Spacer(Modifier.padding(horizontal = 6.dp))
                        Text(stringResource(R.string.enable_gps))
                    }
                }
            }
        }
    }
}
