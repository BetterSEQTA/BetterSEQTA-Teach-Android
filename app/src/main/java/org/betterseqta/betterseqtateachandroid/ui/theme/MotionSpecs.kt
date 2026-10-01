package org.betterseqta.betterseqtateachandroid.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Semantic motion tokens aligned with iOS AppMotion (snappy / bouncy / smooth springs).
 */
object AppMotionSpecs {
    val SnappyFloat = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh,
    )

    val BouncyFloat = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    val SmoothFloat = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    val PhaseTextCrossfade = tween<Float>(durationMillis = 220)

    const val SharedTransitionDurationMillis = 320
}
