package org.betterseqta.betterseqtateachandroid.util

import java.net.URI

object SeqtaUrlHelper {

    fun normalizeSchoolUrl(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        var url = trimmed
        if (!url.startsWith("http://", ignoreCase = true) &&
            !url.startsWith("https://", ignoreCase = true)
        ) {
            url = "https://$url"
        }
        if (url.endsWith("/")) {
            url = url.dropLast(1)
        }
        return runCatching { URI(url).toString() }.getOrNull()
    }

    fun buildTeachLoginUrl(baseUrl: String): String {
        val trimmed = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) else baseUrl
        if (trimmed.contains("/seqta/ta", ignoreCase = true)) {
            return trimmed
        }
        return "$trimmed/seqta/ta"
    }

    fun hostFromUrl(url: String): String =
        runCatching { URI(url).host }.getOrNull() ?: url
}
