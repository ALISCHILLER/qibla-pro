package com.msa.qiblapro.ui.pro

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawRoundRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val ProGlassShape = RoundedCornerShape(22.dp)

/**
 * بک‌گراند لوکس: گرادیان تیره + هاله‌های خیلی نرم
 */
@Composable
fun ProBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val bg = remember {
        Brush.linearGradient(
            listOf(
                Color(0xFF0B1220),
                Color(0xFF090F1B),
                Color(0xFF070B14)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .drawWithCache {
                val glow1 = Brush.radialGradient(
                    colors = listOf(Color(0xFF2D7DFF).copy(alpha = 0.22f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.22f),
                    radius = size.minDimension * 0.9f
                )
                val glow2 = Brush.radialGradient(
                    colors = listOf(Color(0xFF39FFB6).copy(alpha = 0.16f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.86f, size.height * 0.30f),
                    radius = size.minDimension * 0.8f
                )
                val glow3 = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFC14A).copy(alpha = 0.10f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.55f, size.height * 0.88f),
                    radius = size.minDimension * 0.9f
                )

                onDrawBehind {
                    drawRect(glow1)
                    drawRect(glow2)
                    drawRect(glow3)
                }
            },
        content = content
    )
}

/**
 * سایه‌ی Premium نرم (بدون نیاز به lib)
 */
fun Modifier.proShadow(
    elevation: Dp = 18.dp,
    shape: Shape = ProGlassShape
): Modifier = this.shadow(elevation = elevation, shape = shape, clip = false)

/**
 * کارت شیشه‌ای (Glass) سازگار با minSdk=28
 * - Blur واقعی فقط API31+ (اختیاری)
 * - روی 28 هم با gradient/alpha خیلی شیک می‌شه
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    shape: Shape = ProGlassShape,
    borderAlpha: Float = 0.22f,
    fillAlpha: Float = 0.10f,
    blurOn31Plus: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val fill = remember {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = fillAlpha),
                Color.White.copy(alpha = fillAlpha * 0.70f),
                Color.White.copy(alpha = fillAlpha * 0.85f),
            )
        )
    }
    val border = remember(borderAlpha) {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = borderAlpha),
                Color.White.copy(alpha = borderAlpha * 0.55f),
                Color.White.copy(alpha = borderAlpha * 0.75f)
            )
        )
    }

    val blurModifier = if (blurOn31Plus && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.graphicsLayer {
            // Blur روی کل لایه‌ی کارت (نرم)
            renderEffect = RenderEffect
                .createBlurEffect(22f, 22f, Shader.TileMode.CLAMP)
        }
    } else Modifier

    Column(
        modifier = modifier
            .clip(shape)
            .then(blurModifier)
            .background(fill, shape)
            .border(1.dp, border, shape)
            .drawWithCache {
                // هایلایت گوشه‌ها
                val highlight = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.10f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(0f, 0f),
                    radius = size.minDimension * 0.9f
                )
                onDrawBehind {
                    drawRoundRect(
                        brush = highlight,
                        cornerRadius = CornerRadius(36f, 36f)
                    )
                }
            }
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        content()
    }
}
