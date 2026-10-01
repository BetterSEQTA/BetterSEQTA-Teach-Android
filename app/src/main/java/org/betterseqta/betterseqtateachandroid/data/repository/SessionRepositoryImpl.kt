package org.betterseqta.betterseqtateachandroid.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.betterseqta.betterseqtateachandroid.core.di.ApplicationScope
import org.betterseqta.betterseqtateachandroid.data.local.SessionStore
import org.betterseqta.betterseqtateachandroid.data.remote.HeartbeatClient
import org.betterseqta.betterseqtateachandroid.data.remote.HeartbeatResult
import org.betterseqta.betterseqtateachandroid.data.remote.TeachUserClient
import org.betterseqta.betterseqtateachandroid.domain.model.HeartbeatStatus
import org.betterseqta.betterseqtateachandroid.domain.model.LoginStatus
import org.betterseqta.betterseqtateachandroid.domain.model.SessionState
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.domain.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionStore: SessionStore,
    private val heartbeatClient: HeartbeatClient,
    private val teachUserClient: TeachUserClient,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : SessionRepository {

    private val _state = MutableStateFlow(SessionState())
    override val state: StateFlow<SessionState> = _state.asStateFlow()

    private val _logoutEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val logoutEvents: SharedFlow<Unit> = _logoutEvents.asSharedFlow()

    init {
        applicationScope.launch {
            restoreSession()
        }
    }

    override fun startLogin(baseUrl: String) {
        _state.update {
            it.copy(
                session = TeachSession(baseUrl = baseUrl, jsessionId = ""),
                loginStatus = LoginStatus.LoggingIn,
            )
        }
    }

    override suspend fun completeLogin(session: TeachSession) {
        _state.update {
            it.copy(
                session = session,
                loginStatus = LoginStatus.LoggedIn,
            )
        }
        sessionStore.saveSession(session)
        fetchStaffIdAndUserInfoIfNeeded()
    }

    override fun cancelLogin() {
        _state.update {
            it.copy(
                session = null,
                loginStatus = LoginStatus.LoggedOut,
            )
        }
    }

    override suspend fun logout() {
        _state.value = SessionState()
        _logoutEvents.emit(Unit)
        sessionStore.clearSession()
    }

    override fun setLoginError(message: String) {
        _state.update { it.copy(loginStatus = LoginStatus.Error(message)) }
    }

    override suspend fun sendHeartbeat() {
        val session = _state.value.session
        if (session == null) {
            _state.update { it.copy(heartbeatStatus = HeartbeatStatus.Error("No session")) }
            return
        }
        _state.update { it.copy(heartbeatStatus = HeartbeatStatus.Loading) }
        when (val result = heartbeatClient.sendHeartbeat(session)) {
            is HeartbeatResult.Success -> {
                val updated = session.copy(lastHeartbeatAt = result.at)
                _state.update {
                    it.copy(
                        session = updated,
                        heartbeatStatus = HeartbeatStatus.Success(result.at),
                    )
                }
            }
            HeartbeatResult.Unauthorized -> {
                _state.update { it.copy(heartbeatStatus = HeartbeatStatus.Unauthorized) }
                logout()
            }
            is HeartbeatResult.Failure -> {
                _state.update { it.copy(heartbeatStatus = HeartbeatStatus.Error(result.message)) }
            }
        }
    }

    override suspend fun fetchStaffIdAndUserInfoIfNeeded() {
        val session = _state.value.session ?: return
        val current = _state.value
        if (current.staffId != null && current.displayName != null && current.userCode != null) {
            return
        }
        if (current.staffId == null) {
            runCatching {
                val staffId = teachUserClient.getStaffId(session)
                _state.update { it.copy(staffId = staffId) }
            }
        }
        if (_state.value.displayName == null || _state.value.userCode == null) {
            runCatching {
                val (displayName, userCode) = teachUserClient.getUserInfo(session)
                _state.update { state ->
                    state.copy(
                        displayName = displayName?.takeIf { it.isNotEmpty() } ?: state.displayName,
                        userCode = userCode?.takeIf { it.isNotEmpty() } ?: state.userCode,
                    )
                }
            }
        }
    }

    private suspend fun restoreSession() {
        val restored = sessionStore.loadSession() ?: return
        _state.update {
            it.copy(
                session = restored,
                loginStatus = LoginStatus.LoggedIn,
            )
        }
        fetchStaffIdAndUserInfoIfNeeded()
    }
}
