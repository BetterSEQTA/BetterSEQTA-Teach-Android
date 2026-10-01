package org.betterseqta.betterseqtateachandroid.config

import org.betterseqta.betterseqtateachandroid.BuildConfig

/**
 * Cloudflare credentials from [BuildConfig], populated via `local.properties` / `secrets.properties`.
 * Mirrors iOS AppConfiguration (plist injection).
 */
object AppConfiguration {

    private const val PLACEHOLDER_ACCOUNT = "YOUR_CLOUDFLARE_ACCOUNT_ID"
    private const val PLACEHOLDER_TOKEN = "YOUR_CLOUDFLARE_AUTH_TOKEN"
    private const val PLACEHOLDER_API_KEY = "YOUR_API_KEY"

    val cloudflareAccountId: String?
        get() = BuildConfig.CF_ACCOUNT_ID
            .takeIf { it.isNotBlank() && it != PLACEHOLDER_ACCOUNT }

    val cloudflareAuthToken: String?
        get() = BuildConfig.CF_AUTH_TOKEN
            .takeIf { it.isNotBlank() && it != PLACEHOLDER_TOKEN }

    val cloudflareApiKey: String?
        get() = BuildConfig.CLOUDFLARE_API_KEY
            .takeIf { it.isNotBlank() && it != PLACEHOLDER_API_KEY }

    val isAvailable: Boolean
        get() = cloudflareAccountId != null && cloudflareAuthToken != null

    fun baseUrl(): String? {
        val accountId = cloudflareAccountId ?: return null
        return "https://api.cloudflare.com/client/v4/accounts/$accountId/ai/run"
    }

    fun authHeader(): String? {
        val token = cloudflareAuthToken ?: return null
        return "Bearer $token"
    }

    fun apiKeyHeader(): String? {
        val key = cloudflareApiKey ?: return null
        return "Bearer $key"
    }
}
