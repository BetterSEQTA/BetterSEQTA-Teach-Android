package org.betterseqta.betterseqtateachandroid.services

import org.betterseqta.betterseqtateachandroid.config.CloudflareConfig
import org.betterseqta.betterseqtateachandroid.util.SmartReplyCache
import javax.inject.Inject
import javax.inject.Singleton

/** Cloudflare-only smart replies (Apple Intelligence skipped on Android). */
@Singleton
class SmartReplyProvider @Inject constructor(
    private val cloudflareAIService: CloudflareAIService,
    private val smartReplyCache: SmartReplyCache,
) {
    val isAvailable: Boolean
        get() = CloudflareConfig.isAvailable

    suspend fun suggestReplies(
        messageContent: String,
        messageId: Int,
        forceRegenerate: Boolean = false,
    ): List<String>? {
        if (!forceRegenerate) {
            smartReplyCache.get(messageId)?.let { return it }
        }
        if (!CloudflareConfig.isAvailable) return null

        val replies = cloudflareAIService.suggestReplies(messageContent)
        if (!replies.isNullOrEmpty()) {
            smartReplyCache.set(messageId, replies)
        }
        return replies
    }
}
