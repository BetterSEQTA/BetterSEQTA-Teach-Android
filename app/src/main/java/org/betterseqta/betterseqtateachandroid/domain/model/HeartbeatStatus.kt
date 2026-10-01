package org.betterseqta.betterseqtateachandroid.domain.model

import java.time.Instant

sealed interface HeartbeatStatus {
    data object Idle : HeartbeatStatus
    data object Loading : HeartbeatStatus
    data class Success(val at: Instant) : HeartbeatStatus
    data object Unauthorized : HeartbeatStatus
    data class Error(val message: String) : HeartbeatStatus
}
