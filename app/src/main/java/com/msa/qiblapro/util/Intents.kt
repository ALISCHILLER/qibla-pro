package com.msa.qiblapro.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log

object Intents {

    private const val TAG = "Intents"

    fun openAppSettings(context: Context) {
        val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        safeStart(context, i)
    }

    fun openLocationSettings(context: Context) {
        val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        safeStart(context, i)
    }

    fun openUrl(context: Context, url: String) {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        safeStart(context, i)
    }

    private fun safeStart(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "No activity found for intent=$intent", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start activity for intent=$intent", e)
        }
    }
}
