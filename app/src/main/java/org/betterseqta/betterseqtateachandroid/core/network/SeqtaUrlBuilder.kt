package org.betterseqta.betterseqtateachandroid.core.network

object SeqtaUrlBuilder {
    fun build(baseUrl: String, path: String): String {
        val trimmed = baseUrl.trimEnd('/')
        val pathNormalized = if (path.startsWith("/")) path else "/$path"
        return trimmed + pathNormalized
    }

    fun buildWelcomeRedirectUrl(baseUrl: String): String {
        val trimmed = baseUrl.trimEnd('/')
        return if (trimmed.endsWith("/welcome")) trimmed else "$trimmed/welcome"
    }
}
