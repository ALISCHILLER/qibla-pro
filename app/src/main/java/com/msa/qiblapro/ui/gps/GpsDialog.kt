package com.msa.qiblapro.ui.gps

import android.app.Activity
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.layout.*
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
import com.msa.qiblapro.util.GpsResolver
import com.msa.qiblapro.util.GpsUtils
import kotlinx.coroutines.launch

@Composable
fun GpsEnableDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onEnabled: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (GpsUtils.isLocationEnabled(context)) onEnabled()
    }

    val intentSenderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        if (GpsUtils.isLocationEnabled(context)) onEnabled()
    }

    if (!visible) return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.gps_disabled_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.gps_disabled_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    ) {
                        Text(stringResource(R.string.later))
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (activity == null) {
                                onDismiss()
                                return@Button
                            }

                            scope.launch {
                                val intentSender = GpsResolver.buildEnableLocationIntentSender(activity)
                                if (intentSender != null) {
                                    intentSenderLauncher.launch(
                                        IntentSenderRequest.Builder(intentSender).build()
                                    )
                                } else {
                                    settingsLauncher.launch(
                                        android.content.Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.LocationOn, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.enable_gps))
                    }
                }
            }
        }
    }
}
