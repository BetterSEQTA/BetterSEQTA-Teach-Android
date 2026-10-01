package org.betterseqta.betterseqtateachandroid.ui.motion

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import org.betterseqta.betterseqtateachandroid.ui.theme.AppMotionSpecs

enum class PressStyle {
    /** iOS BouncyPressButtonStyle — scale 0.94, snappy release */
    Standard,

    /** iOS PlayfulPressButtonStyle — scale 0.88, bouncy release */
    Playful,
}

/**
 * Instant press-in (no animation), spring release — matches iOS AppMotion button styles.
 */
fun Modifier.expressivePressScale(
    style: PressStyle = PressStyle.Standard,
    interactionSource: MutableInteractionSource? = null,
): Modifier = composed {
    val reduceMotion = LocalReduceMotion.current
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val targetScale = when {
        reduceMotion -> 1f
        !pressed -> 1f
        style == PressStyle.Playful -> 0.88f
        else -> 0.94f
    }
    val releaseSpec = when (style) {
        PressStyle.Playful -> AppMotionSpecs.BouncyFloat
        PressStyle.Standard -> AppMotionSpecs.SnappyFloat
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = if (pressed) snap() else releaseSpec,
        label = "expressivePressScale",
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
}
