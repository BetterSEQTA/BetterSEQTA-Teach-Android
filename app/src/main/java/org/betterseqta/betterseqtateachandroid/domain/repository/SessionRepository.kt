package org.betterseqta.betterseqtateachandroid.domain.repository

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.betterseqta.betterseqtateachandroid.domain.model.SessionState
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession

/**
 * Session lifecycle for auth and shell agents. Observe [state]; react to [logoutEvents] when the
 * server invalidates the session (e.g. heartbeat 401/403).
 */
interface SessionRepository {
    val state: StateFlow<SessionState>
    val logoutEvents: SharedFlow<Unit>

    fun startLogin(baseUrl: String)

    suspend fun completeLogin(session: TeachSession)

    fun cancelLogin()

    suspend fun logout()

    fun setLoginError(message: String)

    suspend fun sendHeartbeat()

    suspend fun fetchStaffIdAndUserInfoIfNeeded()
}
