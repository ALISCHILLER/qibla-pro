package com.msa.qiblapro.util

import android.app.Activity
import android.content.IntentSender
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Tasks

object GpsResolver {

    fun buildEnableLocationIntentSender(activity: Activity): IntentSender? {
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()
        val settingsReq = LocationSettingsRequest.Builder()
            .addLocationRequest(req)
            .setAlwaysShow(true)
            .build()

        val client = LocationServices.getSettingsClient(activity)
        val task = client.checkLocationSettings(settingsReq)

        return try {
            Tasks.await(task)
            null
        } catch (e: Exception) {
            val ex = (e.cause ?: e)
            if (ex is ResolvableApiException) ex.resolution.intentSender else null
        }
    }
}
