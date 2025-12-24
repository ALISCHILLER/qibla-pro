package com.msa.qiblapro.ui.compass.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun CompassRose(
    modifier: Modifier = Modifier,
    headingDeg: Float,
    qiblaDeg: Float,
    isFacingQibla: Boolean,
    needleModifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null
) {
    val measurer = rememberTextMeasurer()
    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = modifier.aspectRatio(1f)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(needleModifier)
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val radius = min(w, h) * 0.46f

            // 1) Background with Theme Colors
            val bgBrush = Brush.radialGradient(
                colors = listOf(colorScheme.surfaceVariant, colorScheme.surface),
                center = Offset(cx, cy),
                radius = radius * 1.1f
            )
            drawCircle(brush = bgBrush, radius = radius, center = Offset(cx, cy))

            drawCircle(
                color = colorScheme.onSurface.copy(alpha = 0.12f),
                radius = radius * 0.98f,
                center = Offset(cx, cy),
                style = Stroke(width = 3f)
            )

            rotate(degrees = -headingDeg, pivot = Offset(cx, cy)) {
                // Ticks
                for (deg in 0 until 360 step 10) {
                    val isMain = deg % 90 == 0
                    val isMajor = !isMain && deg % 30 == 0
                    val tickLen = when {
                        isMain -> radius * 0.13f
                        isMajor -> radius * 0.08f
                        else -> radius * 0.045f
                    }
                    val angleRad = Math.toRadians(deg.toDouble() - 90).toFloat()
                    drawLine(
                        color = colorScheme.onSurface.copy(alpha = if (isMain) 0.75f else 0.2f),
                        start = Offset(cx + cos(angleRad) * (radius - tickLen), cy + sin(angleRad) * (radius - tickLen)),
                        end = Offset(cx + cos(angleRad) * radius, cy + sin(angleRad) * radius),
                        strokeWidth = if (isMain) 3.5f else 1.5f
                    )
                }

                // 3) Qibla Marker
                val qiblaRad = Math.toRadians(qiblaDeg.toDouble() - 90).toFloat()
                val qx = cx + cos(qiblaRad) * radius * 0.80f
                val qy = cy + sin(qiblaRad) * radius * 0.80f
                val qiblaCenter = Offset(qx, qy)

                if (isFacingQibla) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(colorScheme.primary.copy(0.4f), Color.Transparent),
                            center = qiblaCenter,
                            radius = radius * 0.3f
                        ),
                        radius = radius * 0.3f,
                        center = qiblaCenter
                    )
                }

                drawLine(
                    color = colorScheme.primary,
                    start = Offset(cx, cy),
                    end = qiblaCenter,
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )

                // N / E / S / W
                val labels = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
                labels.forEach { (text, angle) ->
                    val angleRad = Math.toRadians(angle.toDouble() - 90).toFloat()
                    val lx = cx + cos(angleRad) * radius * 0.68f
                    val ly = cy + sin(angleRad) * radius * 0.68f
                    val layout = measurer.measure(
                        text = text,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (text == "N") colorScheme.error else colorScheme.onSurface
                        )
                    )
                    rotate(degrees = headingDeg, pivot = Offset(lx, ly)) {
                        drawText(layout, topLeft = Offset(lx - layout.size.width / 2f, ly - layout.size.height / 2f))
                    }
                }
            }
        }

        if (onRefresh != null) {
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(48.dp)
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = colorScheme.onSurface)
            }
        }
    }
}
