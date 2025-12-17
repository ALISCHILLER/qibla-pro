package com.msa.qiblapro.ui.theme

import android.view.View
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColors = darkColorScheme(
    primary = Color(0xFF39FFB6),
    secondary = Color(0xFF00C2FF),
    background = Color(0xFF07121E),
    surface = Color(0xFF0B1B2D),
    onPrimary = Color(0xFF00110B),
    onBackground = Color(0xFFEAF2FF),
    onSurface = Color(0xFFEAF2FF)
)

@Composable
fun QiblaTheme(content: @Composable () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val isRtl = android.text.TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL
    val dir = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides dir) {
        MaterialTheme(
            colorScheme = DarkColors,
            content = content
        )
    }
}
