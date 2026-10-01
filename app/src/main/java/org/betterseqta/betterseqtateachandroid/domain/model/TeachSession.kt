package org.betterseqta.betterseqtateachandroid.domain.model

import java.net.URI
import java.time.Instant

data class TeachSession(
    val baseUrl: String,
    val jsessionId: String,
    val lastHeartbeatAt: Instant? = null,
) {
    val isAuthenticated: Boolean
        get() = jsessionId.isNotEmpty()

    val hostDisplay: String
        get() = runCatching { URI(baseUrl).host }.getOrNull() ?: baseUrl
}
