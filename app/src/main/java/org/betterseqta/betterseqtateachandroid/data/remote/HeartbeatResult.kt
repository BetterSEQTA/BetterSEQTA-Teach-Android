package org.betterseqta.betterseqtateachandroid.data.remote

import java.time.Instant

sealed interface HeartbeatResult {
    data class Success(val at: Instant) : HeartbeatResult
    data object Unauthorized : HeartbeatResult
    data class Failure(val message: String) : HeartbeatResult
}
