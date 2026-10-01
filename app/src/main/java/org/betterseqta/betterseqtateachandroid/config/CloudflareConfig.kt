package org.betterseqta.betterseqtateachandroid.config

/**
 * iOS CloudflareConfig parity — delegates to [AppConfiguration].
 */
object CloudflareConfig {
    val isAvailable: Boolean
        get() = AppConfiguration.isAvailable

    fun baseUrl(): String? = AppConfiguration.baseUrl()

    fun authHeader(): String? = AppConfiguration.authHeader()
}
