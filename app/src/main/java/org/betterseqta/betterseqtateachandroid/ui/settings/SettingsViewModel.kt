package org.betterseqta.betterseqtateachandroid.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.betterseqta.betterseqtateachandroid.data.local.AppPreferences
import org.betterseqta.betterseqtateachandroid.data.local.AttendanceViewMode
import org.betterseqta.betterseqtateachandroid.domain.model.SessionState
import org.betterseqta.betterseqtateachandroid.domain.repository.SessionRepository

data class SettingsUiState(
    val sessionState: SessionState = SessionState(),
    val biometricRequired: Boolean = false,
    val attendanceViewMode: AttendanceViewMode = AttendanceViewMode.List,
    val isSendingHeartbeat: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val appPreferences: AppPreferences,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        sessionRepository.state,
        appPreferences.biometricRequired,
        appPreferences.attendanceViewMode,
    ) { sessionState, biometric, attendanceApi ->
        SettingsUiState(
            sessionState = sessionState,
            biometricRequired = biometric,
            attendanceViewMode = AttendanceViewMode.fromApi(attendanceApi),
            isSendingHeartbeat = sessionState.heartbeatStatus is org.betterseqta.betterseqtateachandroid.domain.model.HeartbeatStatus.Loading,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setBiometricRequired(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setBiometricRequired(enabled) }
    }

    fun setAttendanceViewMode(mode: AttendanceViewMode) {
        viewModelScope.launch { appPreferences.setAttendanceViewMode(mode.apiValue) }
    }

    fun logout() {
        viewModelScope.launch { sessionRepository.logout() }
    }

    fun sendHeartbeat() {
        viewModelScope.launch { sessionRepository.sendHeartbeat() }
    }

    val accountDisplayName: String
        get() {
            val state = uiState.value.sessionState
            val name = state.displayName?.trim()
            if (!name.isNullOrEmpty()) return name
            val staffId = state.staffId
            if (staffId != null) return "Staff $staffId"
            return "User"
        }
}
