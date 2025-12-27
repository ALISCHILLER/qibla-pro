package com.msa.qiblapro.util

import android.app.Activity
import android.content.IntentSender
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object GpsResolver {

    suspend fun buildEnableLocationIntentSender(
        activity: Activity,
        highAccuracy: Boolean = true
    ): IntentSender? {
        val priority = if (highAccuracy) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val req = LocationRequest.Builder(priority, 1000L).build()

        val settingsReq = LocationSettingsRequest.Builder()
            .addLocationRequest(req)
            .setAlwaysShow(true)
            .build()

        val client = LocationServices.getSettingsClient(activity)
        val task = client.checkLocationSettings(settingsReq)

        return try {
            task.await()
            null
        } catch (e: Exception) {
            val ex = (e.cause ?: e)
            if (ex is ResolvableApiException) ex.resolution.intentSender else null
        }
    }
}
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}