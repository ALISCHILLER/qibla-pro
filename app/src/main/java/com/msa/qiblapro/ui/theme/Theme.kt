package com.msa.qiblapro.ui.theme

import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.msa.qiblapro.data.settings.NeonAccent
import com.msa.qiblapro.data.settings.ThemeMode

private fun getNeonPrimary(accent: NeonAccent): Color = when (accent) {
    NeonAccent.GREEN -> Color(0xFF39FFB6)
    NeonAccent.BLUE -> Color(0xFF00C2FF)
    NeonAccent.PURPLE -> Color(0xFFBD00FF)
    NeonAccent.PINK -> Color(0xFFFF00E5)
}

private fun getNeonSecondary(accent: NeonAccent): Color = when (accent) {
    NeonAccent.GREEN -> Color(0xFF00C2FF)
    NeonAccent.BLUE -> Color(0xFF39FFB6)
    NeonAccent.PURPLE -> Color(0xFFFF00E5)
    NeonAccent.PINK -> Color(0xFFBD00FF)
}

private fun getDarkColorScheme(accent: NeonAccent) = darkColorScheme(
    primary = getNeonPrimary(accent),
    secondary = getNeonSecondary(accent),
    background = Color(0xFF07121E),
    surface = Color(0xFF0B1B2D),
    onPrimary = Color(0xFF00110B),
    onBackground = Color(0xFFEAF2FF),
    onSurface = Color(0xFFEAF2FF),
    primaryContainer = getNeonPrimary(accent).copy(alpha = 0.15f),
    onPrimaryContainer = getNeonPrimary(accent)
)

private fun getLightColorScheme(accent: NeonAccent) = lightColorScheme(
    primary = getNeonPrimary(accent),
    secondary = getNeonSecondary(accent),
    background = Color(0xFFF0F4F8),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF07121E),
    onSurface = Color(0xFF07121E),
    primaryContainer = getNeonPrimary(accent).copy(alpha = 0.1f),
    onPrimaryContainer = getNeonPrimary(accent)
)

@Composable
fun QiblaTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accent: NeonAccent = NeonAccent.GREEN,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colors = if (darkTheme) getDarkColorScheme(accent) else getLightColorScheme(accent)

    val locale = LocalConfiguration.current.locales[0]
    val isRtl = android.text.TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL
    val dir = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides dir) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            content = content
        )
    }
}
