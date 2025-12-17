package com.msa.qiblapro.ui.pro

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberInfiniteTransition
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private fun circularAbsDeg(deg: Float): Float {
    // فاصله زاویه‌ای از 0 در بازه [0..180]
    val x = ((deg + 180f) % 360f) - 180f
    return abs(x)
}

@Stable
fun proximity01(rotationErrorDeg: Float, toleranceDeg: Float): Float {
    // هرچی rotationErrorDeg به 0 نزدیک‌تر => نزدیک قبله
    val err = circularAbsDeg(rotationErrorDeg)
    val t = toleranceDeg.coerceAtLeast(1f)
    return (1f - (err / t)).coerceIn(0f, 1f)
}

/**
 * Glow/Status برای "Facing Qibla"
 */
@Composable
fun FacingGlowPill(
    text: String,
    isFacing: Boolean,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "pill")
    val pulse by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val glowAlpha = if (isFacing) pulse else 0.10f
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .drawWithCache {
                val glowBrush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF39FFB6).copy(alpha = glowAlpha),
                        Color.Transparent
                    ),
                    radius = size.minDimension * 0.95f
                )
                onDrawBehind { drawRect(glowBrush) }
            }
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0x26FFFFFF),
                        Color(0x14FFFFFF),
                        Color(0x1AFFFFFF)
                    )
                ),
                shape = shape
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.18f)
                    )
                ),
                shape
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.95f),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * افکت Premium روی عقربه: scale + shine نرم
 * proximity: 0..1
 */
@Composable
fun NeedlePremiumModifier(
    proximity: Float,
    isFacing: Boolean
): Modifier {
    val infinite = rememberInfiniteTransition(label = "needle_shine")

    val sweep by infinite.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isFacing) 900 else 1400,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    val scaleTarget = 1f + (0.04f * proximity) + (if (isFacing) 0.02f else 0f)
    val scale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    val shineAlphaTarget = (0.10f + 0.45f * proximity) * (if (isFacing) 1.0f else 0.75f)
    val shineAlpha by animateFloatAsState(
        targetValue = shineAlphaTarget,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "shineAlpha"
    )

    return Modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .drawWithCache {
            val w = size.width
            val h = size.height
            val x = w * sweep

            val shineBrush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = shineAlpha),
                    Color.Transparent
                ),
                start = androidx.compose.ui.geometry.Offset(x - w * 0.35f, 0f),
                end = androidx.compose.ui.geometry.Offset(x + w * 0.35f, h)
            )

            onDrawWithContent {
                drawContent()
                drawRect(brush = shineBrush, blendMode = BlendMode.Screen)
            }
        }
}
