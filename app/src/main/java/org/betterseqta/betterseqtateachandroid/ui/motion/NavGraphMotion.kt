package org.betterseqta.betterseqtateachandroid.ui.motion

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * M3-style forward/back transitions for nested navigation ([transition patterns](https://m3.material.io/styles/motion/transitions/transition-patterns)).
 */
fun NavGraphBuilder.composableWithAppMotion(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = { appForwardEnter() },
        exitTransition = { appForwardExit() },
        popEnterTransition = { appPopEnter() },
        popExitTransition = { appPopExit() },
    ) { entry ->
        content(entry)
    }
}
