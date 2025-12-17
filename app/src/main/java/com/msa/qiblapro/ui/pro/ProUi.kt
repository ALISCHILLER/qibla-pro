package com.msa.qiblapro.ui.pro

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val GlassShape = RoundedCornerShape(22.dp)

fun Modifier.proShadow(
    elevation: Dp = 22.dp,
    alpha: Float = 0.28f
): Modifier = this.graphicsLayer {
    shadowElevation = elevation.toPx()
    shape = GlassShape
    clip = false
    this.alpha = 1f
}.drawWithCache {
    val shadowBrush = Brush.radialGradient(
        colors = listOf(
            Color.Black.copy(alpha = alpha),
            Color.Transparent
        ),
        center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.75f),
        radius = size.maxDimension * 0.9f
    )
    onDrawBehind {
        drawRect(shadowBrush, blendMode = BlendMode.Multiply)
    }
}

fun Modifier.glassBorder(): Modifier = this.border(
    width = 1.dp,
    brush = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.55f),
            Color.White.copy(alpha = 0.10f),
            Color.White.copy(alpha = 0.25f)
        )
    ),
    shape = GlassShape
)

@Composable
fun ProBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val base = remember {
        Brush.linearGradient(
            listOf(
                Color(0xFF07121E),
                Color(0xFF0B1B2D),
                Color(0xFF061018)
            )
        )
    }

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .background(base)
            .drawWithCache {
                // vignette + noise-ish bands (بدون bitmap)
                val vignette = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.55f)
                    ),
                    radius = size.maxDimension * 0.85f
                )
                val glow = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF39FFB6).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.25f, size.height * 0.15f),
                    radius = size.maxDimension * 0.70f
                )
                onDrawBehind {
                    drawRect(glow, blendMode = BlendMode.Screen)
                    drawRect(vignette)
                }
            }
    ) { content() }
}

/**
 * GlassCard واقعی:
 * - روی API 31+ Blur واقعی (RenderEffect)
 * - روی API 28..30 با alpha + gradient (شیک و Premium)
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    blurRadius: Float = 18f,
    content: @Composable () -> Unit
) {
    val glassBrush = remember {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.14f),
                Color.White.copy(alpha = 0.06f),
                Color.White.copy(alpha = 0.10f)
            )
        )
    }

    Card(
        modifier = modifier
            .graphicsLayer {
                clip = true
                shape = GlassShape
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Blur واقعی فقط 31+
                    // (بدون نیاز به @RequiresApi چون داخل if هست)
                    renderEffect = android.graphics.RenderEffect
                        .createBlurEffect(blurRadius, blurRadius, android.graphics.Shader.TileMode.CLAMP)
                }
            }
            .background(glassBrush, GlassShape)
            .glassBorder(),
        shape = GlassShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(contentPadding)
        ) { content() }
    }
}
