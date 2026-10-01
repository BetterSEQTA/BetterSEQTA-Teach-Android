package org.betterseqta.betterseqtateachandroid.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Brand gradient stops (matches iOS FluidLoadingBar indigo → blue → cyan). */
object BrandGradient {
    val Indigo = Color(0xFF4F46E5)
    val Blue = Color(0xFF2563EB)
    val Cyan = Color(0xFF06B6D4)
}

/** Static fallbacks when dynamic color is off (API < 31). */
val ExpressiveLightFallback = lightColorScheme(
    primary = BrandGradient.Indigo,
    onPrimary = Color.White,
    secondary = BrandGradient.Blue,
    onSecondary = Color.White,
    tertiary = BrandGradient.Cyan,
    onTertiary = Color(0xFF003544),
)

val ExpressiveDarkFallback = darkColorScheme(
    primary = Color(0xFFB4C5FF),
    onPrimary = Color(0xFF00225B),
    secondary = Color(0xFFB8C8EA),
    onSecondary = Color(0xFF1A2F4D),
    tertiary = Color(0xFF7DD3FC),
    onTertiary = Color(0xFF003544),
    background = Color(0xFF0F1117),
    onBackground = Color(0xFFF5F5F7),
    surface = Color(0xFF161922),
    onSurface = Color(0xFFF5F5F7),
    surfaceVariant = Color(0xFF2A2F3D),
    onSurfaceVariant = Color(0xFFD1D5DB),
    outline = Color(0xFF8E94A3),
)

/** Ensures readable light text on dark surfaces (dynamic schemes may use muted on-surface). */
fun androidx.compose.material3.ColorScheme.withDarkModeReadableText(): androidx.compose.material3.ColorScheme {
    return copy(
        onBackground = Color(0xFFF5F5F7),
        onSurface = Color(0xFFF5F5F7),
        onSurfaceVariant = Color(0xFFD1D5DB),
    )
}
