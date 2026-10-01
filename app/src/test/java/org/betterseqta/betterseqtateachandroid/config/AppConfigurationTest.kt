package org.betterseqta.betterseqtateachandroid.config

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class AppConfigurationTest {

    @Test
    fun isAvailable_falseWhenBuildConfigUsesPlaceholders() {
        if (AppConfiguration.cloudflareAccountId == null ||
            AppConfiguration.cloudflareAuthToken == null
        ) {
            assertFalse(AppConfiguration.isAvailable)
            assertNull(AppConfiguration.baseUrl())
            assertNull(AppConfiguration.authHeader())
        }
    }
}
