package com.msa.qiblapro.ui.screens

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.msa.qiblapro.util.GpsResolver
import com.msa.qiblapro.util.GpsUtils

@Composable
fun GpsDialogScreen(
    show: Boolean,
    onDismiss: () -> Unit,
    onEnabled: () -> Unit
) {
    val ctx = LocalContext.current
    val activity = ctx as? Activity ?: return

    val intentSenderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        // بعد از برگشت از دیالوگ سیستم، دوباره وضعیت رو چک می‌کنیم
        val enabled = GpsUtils.isLocationEnabled(activity)
        if (enabled) onEnabled() else onDismiss()
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val enabled = GpsUtils.isLocationEnabled(activity)
        if (enabled) onEnabled() else onDismiss()
    }

    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("GPS غیرفعال است") },
        text = { Text("برای دقت بهتر، لطفاً سرویس موقعیت (Location/GPS) را فعال کنید.") },
        confirmButton = {
            Button(onClick = {
                val sender = GpsResolver.buildEnableLocationIntentSender(activity, highAccuracy = true)
                if (sender != null) {
                    val req = IntentSenderRequest.Builder(sender).build()
                    intentSenderLauncher.launch(req)
                } else {
                    // اگر sender null شد یعنی یا فعال است یا قابل حل نیست
                    if (GpsUtils.isLocationEnabled(activity)) {
                        onEnabled()
                    } else {
                        // fallback: settings
                        settingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }
            }) { Text("فعال‌سازی") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("بعداً") }
        }
    )
}
