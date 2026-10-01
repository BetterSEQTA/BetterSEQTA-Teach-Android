package org.betterseqta.betterseqtateachandroid.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Mirrors iOS [DeepLinkNavigator]: holds a pending message id until [TabRootScreen] consumes it.
 */
class DeepLinkNavigator {
    var pendingMessageId by mutableStateOf<Int?>(null)
        private set

    fun navigateToMessage(messageId: Int) {
        pendingMessageId = messageId
    }

    fun consumePendingMessageId(): Int? {
        val id = pendingMessageId
        pendingMessageId = null
        return id
    }

    companion object {
        val instance = DeepLinkNavigator()
    }
}

val LocalDeepLinkNavigator = staticCompositionLocalOf { DeepLinkNavigator.instance }
