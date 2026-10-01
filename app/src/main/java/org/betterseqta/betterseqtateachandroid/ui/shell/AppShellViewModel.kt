package org.betterseqta.betterseqtateachandroid.ui.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.betterseqta.betterseqtateachandroid.data.local.AppPreferences
import org.betterseqta.betterseqtateachandroid.domain.model.SessionState
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.domain.repository.SessionRepository

@HiltViewModel
class AppShellViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    appPreferences: AppPreferences,
) : ViewModel() {
    val sessionState: StateFlow<SessionState> = sessionRepository.state

    val biometricRequired: StateFlow<Boolean> = appPreferences.biometricRequired.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        false,
    )

    val logoutEvents = sessionRepository.logoutEvents

    fun startLogin(baseUrl: String) {
        sessionRepository.startLogin(baseUrl)
    }

    fun completeLogin(session: TeachSession) {
        viewModelScope.launch { sessionRepository.completeLogin(session) }
    }

    fun cancelLogin() {
        sessionRepository.cancelLogin()
    }

    fun setLoginError(message: String) {
        sessionRepository.setLoginError(message)
    }
}
