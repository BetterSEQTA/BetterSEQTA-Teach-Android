package org.betterseqta.betterseqtateachandroid.navigation

import android.content.Intent
import android.net.Uri

object DeepLinkIntentParser {
    const val EXTRA_MESSAGE_ID = "org.betterseqta.betterseqtateachandroid.extra.MESSAGE_ID"

    /**
     * Supports notification intents ([EXTRA_MESSAGE_ID]) and
     * `betterseqta://teach/messages/{messageId}` deep links.
     */
    fun parseMessageId(intent: Intent?): Int? {
        if (intent == null) return null

        val fromExtra = intent.getIntExtra(EXTRA_MESSAGE_ID, Int.MIN_VALUE)
        if (fromExtra != Int.MIN_VALUE) {
            return fromExtra
        }

        return parseMessageIdFromUri(intent.data)
    }

    fun parseMessageIdFromUri(uri: Uri?): Int? {
        if (uri == null) return null

        val host = uri.host?.lowercase()
        val scheme = uri.scheme?.lowercase()
        if (scheme != "betterseqta" || host != "teach") return null

        val segments = uri.pathSegments
        if (segments.size >= 2 && segments[0] == "messages") {
            return segments[1].toIntOrNull()
        }
        return null
    }
}
