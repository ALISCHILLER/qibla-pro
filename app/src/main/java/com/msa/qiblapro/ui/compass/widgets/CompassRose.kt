package com.msa.qiblapro.ui.compass.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

            // 1) Background
            val bgBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0F172A),
                    Color(0xFF020617)
                ),
                center = Offset(cx, cy),
                radius = radius * 1.1f
            )
            drawCircle(
                brush = bgBrush,
                radius = radius,
                center = Offset(cx, cy)
            )

            // Outer ring
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = radius * 0.98f,
                center = Offset(cx, cy),
                style = Stroke(width = 3f)
            )

            // Inner ring
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = radius * 0.70f,
                center = Offset(cx, cy),
                style = Stroke(width = 2f)
            )

            // 2) Rotate dial by phone heading
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
                        color = Color.White.copy(
                            alpha = when {
                                isMain -> 0.75f
                                isMajor -> 0.45f
                                else -> 0.18f
                            }
                        ),
                        start = Offset(
                            cx + cos(angleRad) * (radius - tickLen),
                            cy + sin(angleRad) * (radius - tickLen)
                        ),
                        end = Offset(
                            cx + cos(angleRad) * radius,
                            cy + sin(angleRad) * radius
                        ),
                        strokeWidth = when {
                            isMain -> 3.5f
                            isMajor -> 2.2f
                            else -> 1.4f
                        }
                    )
                }

                // 3) Qibla + Kaaba
                val qiblaRad = Math.toRadians(qiblaDeg.toDouble() - 90).toFloat()
                val qx = cx + cos(qiblaRad) * radius * 0.80f
                val qy = cy + sin(qiblaRad) * radius * 0.80f
                val qiblaCenter = Offset(qx, qy)

                // Glow when exactly facing Qibla
                if (isFacingQibla) {
                    val glowBrush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x55FF6B6B),
                            Color.Transparent
                        ),
                        center = qiblaCenter,
                        radius = radius * 0.28f
                    )
                    drawCircle(
                        brush = glowBrush,
                        radius = radius * 0.28f,
                        center = qiblaCenter
                    )
                }

                // Qibla line
                drawLine(
                    color = Color(0xFFFFC857),
                    start = Offset(cx, cy),
                    end = qiblaCenter,
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )

                // Tiny circle under Kaaba
                drawCircle(
                    color = Color(0xFFFFC857).copy(alpha = 0.85f),
                    radius = radius * 0.03f,
                    center = qiblaCenter
                )

                // Kaaba icon (small, clean)
                val kaabaWidth = radius * 0.14f
                val kaabaHeight = radius * 0.10f

                val kaabaTopLeft = Offset(
                    x = qx - kaabaWidth / 2f,
                    y = qy - kaabaHeight / 2f
                )

                // Black body
                drawRoundRect(
                    color = Color(0xFF050505),
                    topLeft = kaabaTopLeft,
                    size = Size(kaabaWidth, kaabaHeight),
                    cornerRadius = CornerRadius(
                        kaabaWidth * 0.18f,
                        kaabaWidth * 0.18f
                    )
                )

                // Golden band
                val bandHeight = kaabaHeight * 0.28f
                drawRoundRect(
                    color = Color(0xFFFFC857),
                    topLeft = Offset(
                        x = kaabaTopLeft.x,
                        y = kaabaTopLeft.y + bandHeight * 0.55f
                    ),
                    size = Size(kaabaWidth, bandHeight * 0.65f),
                    cornerRadius = CornerRadius(
                        bandHeight * 0.4f,
                        bandHeight * 0.4f
                    )
                )

                // Small golden door
                val doorWidth = kaabaWidth * 0.22f
                val doorHeight = kaabaHeight * 0.34f
                drawRoundRect(
                    color = Color(0xFFFFE29F),
                    topLeft = Offset(
                        x = kaabaTopLeft.x + kaabaWidth * 0.60f,
                        y = kaabaTopLeft.y + kaabaHeight * 0.45f
                    ),
                    size = Size(doorWidth, doorHeight),
                    cornerRadius = CornerRadius(
                        doorWidth * 0.3f,
                        doorWidth * 0.3f
                    )
                )

                // 4) N / E / S / W
                val labels = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
                labels.forEach { (text, angle) ->
                    val angleRad = Math.toRadians(angle.toDouble() - 90).toFloat()
                    val lx = cx + cos(angleRad) * radius * 0.68f
                    val ly = cy + sin(angleRad) * radius * 0.68f

                    val layout = measurer.measure(
                        text = text,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (text == "N")
                                Color(0xFFFF6B6B)
                            else
                                Color.White.copy(alpha = 0.9f)
                        )
                    )

                    // keep letters upright
                    rotate(degrees = headingDeg, pivot = Offset(lx, ly)) {
                        drawText(
                            layout,
                            topLeft = Offset(
                                lx - layout.size.width / 2f,
                                ly - layout.size.height / 2f
                            )
                        )
                    }
                }
            }

            // 5) Center dot
            drawCircle(
                color = Color.White.copy(alpha = 0.95f),
                radius = radius * 0.028f,
                center = Offset(cx, cy)
            )
        }

        // 6) Optional refresh button
        if (onRefresh != null) {
            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color.White
                )
            }
        }
    }
}
