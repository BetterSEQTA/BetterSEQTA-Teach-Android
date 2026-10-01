package org.betterseqta.betterseqtateachandroid.ui.motion

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

object MessageHeroKeys {
    fun avatar(messageId: Int) = "message-avatar-$messageId"
    fun subject(messageId: Int) = "message-subject-$messageId"
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.messageHeroSharedElement(key: String): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalAnimatedVisibilityScope.current ?: return this
    return with(sharedScope) {
        val state = rememberSharedContentState(key = key)
        Modifier.sharedElement(
            sharedContentState = state,
            animatedVisibilityScope = visibilityScope,
        )
    }.then(this)
}
