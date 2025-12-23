package com.msa.qiblapro.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object Intents {
    /** باز کردن تنظیمات دقیق اپلیکیشن برای مدیریت پرمیشن‌ها */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /** باز کردن وب‌سایت */
    fun openUrl(context: Context, url: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
