package org.betterseqta.betterseqtateachandroid.ui.motion

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import org.betterseqta.betterseqtateachandroid.ui.theme.AppMotionSpecs

/** Fade + subtle scale when switching bottom tabs (M3 fade-through feel). */
fun Modifier.tabVisibilityMotion(visible: Boolean, zIndex: Float = if (visible) 1f else 0f): Modifier = composed {
    val reduceMotion = LocalReduceMotion.current
    val targetAlpha = if (visible) 1f else 0f
    val targetScale = if (visible) 1f else 0.98f
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = if (reduceMotion) {
            androidx.compose.animation.core.snap()
        } else {
            AppMotionSpecs.SmoothFloat
        },
        label = "tabAlpha",
    )
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = if (reduceMotion) {
            androidx.compose.animation.core.snap()
        } else {
            AppMotionSpecs.SmoothFloat
        },
        label = "tabScale",
    )
    this
        .fillMaxSize()
        .zIndex(zIndex)
        .graphicsLayer {
            this.alpha = alpha
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (visible) {
                Modifier
            } else {
                Modifier.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            },
        )
}
