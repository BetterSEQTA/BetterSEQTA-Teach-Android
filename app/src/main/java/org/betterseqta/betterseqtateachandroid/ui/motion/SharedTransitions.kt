package org.betterseqta.betterseqtateachandroid.ui.motion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import org.betterseqta.betterseqtateachandroid.ui.theme.AppMotionSpecs

object AppSharedTransitions {
    private val forwardSlideSpec = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
    private val fadeSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    fun forwardEnter(): EnterTransition =
        slideInHorizontally(
            animationSpec = forwardSlideSpec,
            initialOffsetX = { fullWidth -> (fullWidth * 0.18f).toInt() },
        ) + fadeIn(animationSpec = fadeSpec) +
        scaleIn(initialScale = 0.96f, animationSpec = AppMotionSpecs.SmoothFloat)

    fun forwardExit(): ExitTransition =
        slideOutHorizontally(
            animationSpec = forwardSlideSpec,
            targetOffsetX = { fullWidth -> -(fullWidth * 0.12f).toInt() },
        ) + fadeOut(animationSpec = fadeSpec, targetAlpha = 0.92f)

    fun popEnter(): EnterTransition =
        slideInHorizontally(
            animationSpec = forwardSlideSpec,
            initialOffsetX = { fullWidth -> -(fullWidth * 0.12f).toInt() },
        ) + fadeIn(animationSpec = fadeSpec)

    fun popExit(): ExitTransition =
        slideOutHorizontally(
            animationSpec = forwardSlideSpec,
            targetOffsetX = { fullWidth -> (fullWidth * 0.18f).toInt() },
        ) + fadeOut(animationSpec = fadeSpec) +
        scaleOut(targetScale = 0.96f, animationSpec = AppMotionSpecs.SmoothFloat)

    /** Root-level fade + scale (auth steps, shell reveal). */
    fun fadeScaleIn(): EnterTransition =
        fadeIn(animationSpec = AppMotionSpecs.SmoothFloat) +
            scaleIn(initialScale = 0.94f, animationSpec = AppMotionSpecs.BouncyFloat)

    fun fadeScaleOut(): ExitTransition =
        fadeOut(animationSpec = AppMotionSpecs.SmoothFloat) +
            scaleOut(targetScale = 0.96f, animationSpec = AppMotionSpecs.SnappyFloat)
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.appForwardEnter(): EnterTransition =
    AppSharedTransitions.forwardEnter()

fun AnimatedContentTransitionScope<NavBackStackEntry>.appForwardExit(): ExitTransition =
    AppSharedTransitions.forwardExit()

fun AnimatedContentTransitionScope<NavBackStackEntry>.appPopEnter(): EnterTransition =
    AppSharedTransitions.popEnter()

fun AnimatedContentTransitionScope<NavBackStackEntry>.appPopExit(): ExitTransition =
    AppSharedTransitions.popExit()
