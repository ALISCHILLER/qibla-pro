package com.msa.qiblapro.ui.pro

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val GlassShape = RoundedCornerShape(22.dp)

@Composable
fun ProBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val base = remember {
        Brush.linearGradient(
            listOf(Color(0xFF07121E), Color(0xFF0B1B2D), Color(0xFF061018))
        )
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(base)
            .drawWithCache {
                val vignette = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                    radius = size.maxDimension * 0.85f
                )
                val glow = Brush.radialGradient(
                    colors = listOf(Color(0xFF39FFB6).copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * 0.25f, size.height * 0.15f),
                    radius = size.maxDimension * 0.70f
                )
                onDrawBehind {
                    drawRect(glow, blendMode = BlendMode.Screen)
                    drawRect(vignette)
                }
            }
    ) { content() }
}

fun Modifier.proShadow(
    elevation: Dp = 22.dp,
    alpha: Float = 0.28f
): Modifier = this.graphicsLayer {
    shadowElevation = elevation.toPx()
    shape = GlassShape
    clip = false
}.drawWithCache {
    val shadowBrush = Brush.radialGradient(
        colors = listOf(Color.Black.copy(alpha = alpha), Color.Transparent),
        center = Offset(size.width * 0.5f, size.height * 0.75f),
        radius = size.maxDimension * 0.9f
    )
    onDrawBehind {
        drawRect(shadowBrush, blendMode = BlendMode.Multiply)
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    blurRadius: Float = 18f,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .proShadow()
            .graphicsLayer {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    renderEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP).asComposeRenderEffect()
                }
            }
            .background(Color.White.copy(alpha = 0.1f), GlassShape)
            .border(1.dp, Color.White.copy(alpha = 0.2f), GlassShape),
        shape = GlassShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}
