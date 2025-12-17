package com.msa.qiblapro.ui.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun CompassRose(
    modifier: Modifier = Modifier,
    headingDeg: Float,
    qiblaDeg: Float,
    needleModifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = min(size.width, size.height) * 0.45f

            // حلقه بیرونی
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.20f),
                        Color.White.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = r * 1.15f
                ),
                radius = r * 1.10f,
                center = Offset(cx, cy)
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = r * 0.025f)
            )

            // tick marks
            for (d in 0 until 360 step 6) {
                val major = (d % 30 == 0)
                val len = if (major) r * 0.09f else r * 0.045f
                val stroke = if (major) r * 0.012f else r * 0.0065f
                val a = Math.toRadians(d.toDouble() - 90.0)
                val x1 = cx + cos(a).toFloat() * (r - len)
                val y1 = cy + sin(a).toFloat() * (r - len)
                val x2 = cx + cos(a).toFloat() * r
                val y2 = cy + sin(a).toFloat() * r
                drawLine(
                    color = Color.White.copy(alpha = if (major) 0.55f else 0.22f),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = stroke
                )
            }

            // خط قبله (نشانگر طلایی)
            rotate(degrees = qiblaDeg, pivot = Offset(cx, cy)) {
                drawLine(
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFFFFD36E).copy(alpha = 0.95f),
                            Color(0xFF39FFB6).copy(alpha = 0.55f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(cx, cy),
                    end = Offset(cx, cy - r * 0.92f),
                    strokeWidth = r * 0.018f
                )
            }

            // مرکز
            drawCircle(Color.White.copy(alpha = 0.18f), radius = r * 0.08f, center = Offset(cx, cy))
            drawCircle(Color(0xFF39FFB6).copy(alpha = 0.65f), radius = r * 0.03f, center = Offset(cx, cy))

            // حروف جهات
            drawCardinals(measurer, cx, cy, r)
        }

        CompassNeedle(
            modifier = Modifier.fillMaxSize(),
            headingDeg = headingDeg,
            needleModifier = needleModifier
        )
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawCardinals(
    measurer: TextMeasurer,
    centerX: Float,
    centerY: Float,
    r: Float
) {
    val style = TextStyle(
        fontSize = (r * 0.11f / density).sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White.copy(alpha = 0.80f)
    )

    fun drawLabel(text: String, angleDeg: Float) {
        val a = Math.toRadians(angleDeg.toDouble() - 90.0)
        val x = centerX + cos(a).toFloat() * (r * 0.78f)
        val y = centerY + sin(a).toFloat() * (r * 0.78f)
        val layout = measurer.measure(text, style)
        drawText(layout, topLeft = Offset(x - layout.size.width / 2f, y - layout.size.height / 2f))
    }

    drawLabel("N", 0f)
    drawLabel("E", 90f)
    drawLabel("S", 180f)
    drawLabel("W", 270f)
}

@Composable
fun CompassNeedle(
    modifier: Modifier = Modifier,
    headingDeg: Float,
    needleModifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.then(needleModifier)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = min(size.width, size.height) * 0.45f

        rotate(degrees = headingDeg, pivot = Offset(cx, cy)) {
            val top = Path().apply {
                moveTo(cx, cy - r * 0.92f)
                lineTo(cx - r * 0.06f, cy)
                lineTo(cx + r * 0.06f, cy)
                close()
            }
            drawPath(
                path = top,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF39FFB6).copy(alpha = 0.95f),
                        Color(0xFF00C2FF).copy(alpha = 0.55f)
                    ),
                    start = Offset(cx, cy - r * 0.92f),
                    end = Offset(cx, cy)
                )
            )

            val bottom = Path().apply {
                moveTo(cx, cy + r * 0.75f)
                lineTo(cx - r * 0.045f, cy)
                lineTo(cx + r * 0.045f, cy)
                close()
            }
            drawPath(bottom, color = Color.White.copy(alpha = 0.22f))
        }
    }
}
