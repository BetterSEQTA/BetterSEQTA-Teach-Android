package org.betterseqta.betterseqtateachandroid.ui.motion

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

/** When true, skip non-essential motion (accessibility / battery). */
val LocalReduceMotion = compositionLocalOf { false }
