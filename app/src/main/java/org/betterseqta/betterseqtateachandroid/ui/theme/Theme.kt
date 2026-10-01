package org.betterseqta.betterseqtateachandroid.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import org.betterseqta.betterseqtateachandroid.ui.motion.LocalReduceMotion

/**
 * Material 3 theme with expressive typography/motion tokens and dynamic color (Android 12+).
 * Uses public MaterialTheme APIs; swap to MaterialExpressiveTheme when exposed in stable M3.
 */
@Composable
fun BetterSEQTATeachAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        darkTheme -> ExpressiveDarkFallback
        else -> ExpressiveLightFallback
    }
    val colorScheme = if (darkTheme) baseScheme.withDarkModeReadableText() else baseScheme

    CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
