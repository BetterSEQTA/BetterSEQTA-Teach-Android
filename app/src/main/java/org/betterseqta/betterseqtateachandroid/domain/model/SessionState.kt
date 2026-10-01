package org.betterseqta.betterseqtateachandroid.domain.model

data class SessionState(
    val session: TeachSession? = null,
    val staffId: Int? = null,
    val displayName: String? = null,
    val userCode: String? = null,
    val loginStatus: LoginStatus = LoginStatus.LoggedOut,
    val heartbeatStatus: HeartbeatStatus = HeartbeatStatus.Idle,
)
