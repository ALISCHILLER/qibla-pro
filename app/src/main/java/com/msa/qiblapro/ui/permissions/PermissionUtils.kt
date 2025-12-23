package com.msa.qiblapro.ui.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.msa.qiblapro.util.Intents

fun isLocationPermissionGranted(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarse = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fine || coarse
}

fun isLocationPermissionPermanentlyDenied(activity: Activity): Boolean {
    val fineRationale = ActivityCompat.shouldShowRequestPermissionRationale(
        activity, Manifest.permission.ACCESS_FINE_LOCATION
    )
    val coarseRationale = ActivityCompat.shouldShowRequestPermissionRationale(
        activity, Manifest.permission.ACCESS_COARSE_LOCATION
    )
    return !(fineRationale || coarseRationale)
}

/** 
 * باز کردن تنظیمات اپلیکیشن (از Intents.kt استفاده می‌کند)
 */
fun openAppSettings(context: Context) {
    Intents.openAppSettings(context)
}
