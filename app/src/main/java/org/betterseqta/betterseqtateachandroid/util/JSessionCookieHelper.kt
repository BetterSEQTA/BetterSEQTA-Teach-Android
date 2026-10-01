package org.betterseqta.betterseqtateachandroid.util

import android.webkit.CookieManager
import java.net.URI

object JSessionCookieHelper {

    private const val COOKIE_NAME = "JSESSIONID"

    fun findJSessionId(baseUrl: String): String? {
        val host = SeqtaUrlHelper.hostFromUrl(baseUrl)
        val cookieManager = CookieManager.getInstance()
        cookieManager.flush()
        val candidates = listOf(
            baseUrl,
            SeqtaUrlHelper.buildTeachLoginUrl(baseUrl),
            "https://$host",
            "http://$host",
        )
        for (url in candidates) {
            val header = cookieManager.getCookie(url) ?: continue
            val value = cookieValue(header, COOKIE_NAME)
            if (!value.isNullOrEmpty()) return value
        }
        return null
    }

    private fun cookieValue(header: String, name: String): String? =
        header.split(';')
            .map { it.trim() }
            .firstOrNull { part -> part.startsWith("$name=", ignoreCase = true) }
            ?.substringAfter('=')
            ?.takeIf { it.isNotEmpty() }

    fun isWelcomeUrl(currentUrl: String?): Boolean {
        if (currentUrl.isNullOrBlank()) return false
        val uri = runCatching { URI(currentUrl) }.getOrNull() ?: return false
        val path = uri.path ?: ""
        val fragment = uri.fragment ?: ""
        return path.contains("welcome", ignoreCase = true) ||
            fragment.contains("welcome", ignoreCase = true)
    }

}
