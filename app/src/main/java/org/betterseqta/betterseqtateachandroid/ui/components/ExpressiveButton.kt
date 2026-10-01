package org.betterseqta.betterseqtateachandroid.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import org.betterseqta.betterseqtateachandroid.ui.motion.PressStyle
import org.betterseqta.betterseqtateachandroid.ui.motion.expressivePressScale

@Composable
fun ExpressiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    playful: Boolean = false,
    hapticOnClick: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressStyle = if (playful) PressStyle.Playful else PressStyle.Standard
    Button(
        onClick = {
            if (hapticOnClick) {
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            }
            onClick()
        },
        modifier = modifier.expressivePressScale(pressStyle, interactionSource),
        enabled = enabled,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        content = content,
    )
}

@Composable
fun ExpressiveTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    FilledTonalButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier.expressivePressScale(PressStyle.Standard, interactionSource),
        enabled = enabled,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.filledTonalButtonColors(),
        content = content,
    )
}
