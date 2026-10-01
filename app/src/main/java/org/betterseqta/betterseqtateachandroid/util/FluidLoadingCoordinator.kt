package org.betterseqta.betterseqtateachandroid.util

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay

/**
 * Organic milestone progress + snap-to-complete for Compose (iOS FluidLoadingCoordinator).
 */
object FluidLoadingCoordinator {

    data class Phase(
        val delayMillis: Long,
        val progress: Float,
        val text: String,
    )

    object Presets {
        val smartReplies = listOf(
            Phase(450, 0.22f, "Understanding context…"),
            Phase(700, 0.46f, "Thinking…"),
            Phase(550, 0.68f, "Drafting replies…"),
            Phase(900, 0.82f, "Polishing…"),
        )

        val messageDetail = listOf(
            Phase(400, 0.22f, "Connecting to Direqt…"),
            Phase(550, 0.45f, "Fetching message…"),
            Phase(650, 0.68f, "Loading thread & labels…"),
            Phase(750, 0.84f, "Almost there…"),
        )

        val messagesList = listOf(
            Phase(380, 0.24f, "Opening your inbox…"),
            Phase(600, 0.48f, "Syncing messages…"),
            Phase(700, 0.72f, "Applying labels…"),
            Phase(800, 0.85f, "Finishing up…"),
        )

        val timetable = listOf(
            Phase(400, 0.24f, "Loading your timetable…"),
            Phase(600, 0.48f, "Fetching lessons…"),
            Phase(650, 0.70f, "Matching rooms & times…"),
            Phase(800, 0.86f, "Preparing your day…"),
        )

        val notices = listOf(
            Phase(400, 0.24f, "Loading notices…"),
            Phase(600, 0.50f, "Fetching school posts…"),
            Phase(700, 0.72f, "Organising by date…"),
            Phase(750, 0.85f, "Almost ready…"),
        )

        val homeLessons = listOf(
            Phase(350, 0.26f, "Fetching today’s classes…"),
            Phase(550, 0.52f, "Checking your timetable…"),
            Phase(650, 0.76f, "Sorting periods…"),
            Phase(700, 0.88f, "Wrapping up…"),
        )

        val homeMessages = listOf(
            Phase(350, 0.26f, "Opening Direqt…"),
            Phase(550, 0.52f, "Loading recent messages…"),
            Phase(650, 0.76f, "Checking read status…"),
            Phase(700, 0.88f, "Almost there…"),
        )

        val attendance = listOf(
            Phase(400, 0.22f, "Opening class roll…"),
            Phase(550, 0.46f, "Loading attendance codes…"),
            Phase(650, 0.68f, "Fetching students…"),
            Phase(700, 0.82f, "Loading summaries…"),
            Phase(750, 0.90f, "Finalising roll…"),
        )

        val composeRecipients = listOf(
            Phase(350, 0.26f, "Loading contacts…"),
            Phase(500, 0.48f, "Fetching staff & students…"),
            Phase(550, 0.68f, "Building recipient list…"),
            Phase(650, 0.84f, "Preparing your draft…"),
        )
    }

    suspend fun runOrganicMilestones(
        phases: List<Phase>,
        generation: Int,
        currentGeneration: () -> Int,
        progress: MutableState<Float>,
        phaseText: MutableState<String>,
    ) {
        for (phase in phases) {
            delay(phase.delayMillis)
            if (currentGeneration() != generation) return
            if (progress.value >= 0.9f) return
            progress.value = maxOf(progress.value, phase.progress)
            phaseText.value = phase.text
        }
    }

    suspend fun snapFinish(
        generation: Int,
        currentGeneration: () -> Int,
        progress: MutableState<Float>,
        phaseText: MutableState<String>,
        finishingText: String,
        resetText: String,
    ) {
        if (currentGeneration() != generation) return
        progress.value = 1f
        phaseText.value = finishingText
        delay(280)
        if (currentGeneration() != generation) return
        progress.value = 0f
        phaseText.value = resetText
    }

    /** Optional animated progress for [FluidLoadingBarView] composables. */
    @Composable
    fun animatedProgress(target: Float): Float {
        val animated by animateFloatAsState(
            targetValue = target,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "fluidLoadingProgress",
        )
        return animated
    }
}
