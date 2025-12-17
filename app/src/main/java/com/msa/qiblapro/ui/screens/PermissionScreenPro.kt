package com.msa.qiblapro.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.pro.GlassCard
import com.msa.qiblapro.ui.pro.ProBackground
import com.msa.qiblapro.ui.pro.proShadow

@Composable
fun PermissionScreenPro(
    onGrantPermission: () -> Unit,
    isPermanentlyDenied: Boolean
) {
    val ctx = LocalContext.current
    val activity = ctx as? Activity

    ProBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .proShadow()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.permission_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.permission_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                    textAlign = TextAlign.Start
                )

                Spacer(Modifier.height(18.dp))

                // CTA ها
                if (!isPermanentlyDenied) {
                    Button(
                        onClick = onGrantPermission,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.grant_permission))
                    }
                } else {
                    Button(
                        onClick = {
                            activity?.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", ctx.packageName, null)
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(stringResource(R.string.open_settings))
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "• بدون ارسال اطلاعات به سرور\n• پردازش فقط روی دستگاه",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f)
                )
            }
        }
    }
}
