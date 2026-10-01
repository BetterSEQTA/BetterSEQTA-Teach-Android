package org.betterseqta.betterseqtateachandroid.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.betterseqta.betterseqtateachandroid.util.FluidLoadingCoordinator

@Composable
fun FluidLoadingBar(
    phaseText: String,
    modifier: Modifier = Modifier,
    progress: Float = 0.35f,
) {
    val animatedProgress = FluidLoadingCoordinator.animatedProgress(progress)
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LinearProgressIndicator(
            progress = { animatedProgress.coerceIn(0.05f, 0.98f) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        Text(
            text = phaseText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

/** Organic milestone progress while [active] — mirrors iOS [FluidLoadingCoordinator]. */
@Composable
fun FluidLoadingBarOrganic(
    phases: List<FluidLoadingCoordinator.Phase>,
    active: Boolean,
    modifier: Modifier = Modifier,
    finishingText: String = "Ready",
) {
    var progress by remember { mutableFloatStateOf(0.06f) }
    var phaseText by remember { mutableStateOf(phases.firstOrNull()?.text ?: "") }
    var generation by remember { mutableIntStateOf(0) }

    LaunchedEffect(active, phases) {
        if (!active) {
            progress = 0f
            return@LaunchedEffect
        }
        generation += 1
        val gen = generation
        progress = 0.06f
        phaseText = phases.firstOrNull()?.text ?: ""
        for (phase in phases) {
            delay(phase.delayMillis)
            if (generation != gen) return@LaunchedEffect
            if (progress >= 0.9f) break
            progress = maxOf(progress, phase.progress)
            phaseText = phase.text
        }
        if (generation != gen) return@LaunchedEffect
        progress = 1f
        phaseText = finishingText
    }

    FluidLoadingBar(
        phaseText = phaseText,
        progress = progress,
        modifier = modifier,
    )
}
