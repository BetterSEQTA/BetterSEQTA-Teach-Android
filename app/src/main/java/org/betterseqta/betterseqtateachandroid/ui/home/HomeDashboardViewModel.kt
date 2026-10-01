package org.betterseqta.betterseqtateachandroid.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.betterseqta.betterseqtateachandroid.data.remote.TeachAssessmentsClient
import org.betterseqta.betterseqtateachandroid.data.remote.TeachMessagesClient
import org.betterseqta.betterseqtateachandroid.data.remote.TeachTimetableClient
import org.betterseqta.betterseqtateachandroid.domain.model.TeachLesson
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMessage
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSubjectAssessmentGroup
import org.betterseqta.betterseqtateachandroid.domain.repository.SessionRepository
import org.betterseqta.betterseqtateachandroid.util.AppDateFormatters
import org.betterseqta.betterseqtateachandroid.util.formatStaffGreetingName
import javax.inject.Inject

data class HomeDashboardUiState(
    val greetingName: String? = null,
    val todayLessons: List<TeachLesson> = emptyList(),
    val messages: List<TeachMessage> = emptyList(),
    val assessmentGroups: List<TeachSubjectAssessmentGroup> = emptyList(),
    val assessmentsToMarkCount: Int = 0,
    val lessonsLoading: Boolean = false,
    val messagesLoading: Boolean = false,
    val assessmentsLoading: Boolean = false,
    val lessonsError: String? = null,
    val messagesError: String? = null,
    val assessmentsError: String? = null,
)

@HiltViewModel
class HomeDashboardViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val timetableClient: TeachTimetableClient,
    private val messagesClient: TeachMessagesClient,
    private val assessmentsClient: TeachAssessmentsClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeDashboardUiState())
    val uiState: StateFlow<HomeDashboardUiState> = _uiState.asStateFlow()

    private var loadedSessionId: String? = null

    init {
        viewModelScope.launch {
            sessionRepository.state
                .map { it.session?.jsessionId }
                .distinctUntilChanged()
                .collect { loadIfNeeded() }
        }
    }

    fun loadIfNeeded() {
        viewModelScope.launch {
            val session = sessionRepository.state.value.session
            if (session == null) {
                reset()
                return@launch
            }
            if (loadedSessionId == session.jsessionId) return@launch
            load()
            loadedSessionId = session.jsessionId
        }
    }

    fun refresh() {
        viewModelScope.launch {
            if (sessionRepository.state.value.session == null) {
                reset()
                return@launch
            }
            load()
            loadedSessionId = sessionRepository.state.value.session?.jsessionId
        }
    }

    private suspend fun load() {
        val session = sessionRepository.state.value.session ?: return
        val today = AppDateFormatters.todayIso()

        _uiState.update {
            it.copy(lessonsLoading = true, lessonsError = null)
        }

        sessionRepository.fetchStaffIdAndUserInfoIfNeeded()
        val staffId = sessionRepository.state.value.staffId
        val displayName = sessionRepository.state.value.displayName
        val greetingName = formatStaffGreetingName(displayName)

        _uiState.update { it.copy(greetingName = greetingName) }

        if (staffId != null) {
            runCatching {
                timetableClient.fetchLessons(session, staffId, today, today)
            }.onSuccess { lessons ->
                _uiState.update { it.copy(todayLessons = lessons) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(todayLessons = emptyList(), lessonsError = error.message ?: "Failed to load lessons")
                }
            }
        } else {
            _uiState.update {
                it.copy(todayLessons = emptyList(), lessonsError = "Could not load staff ID")
            }
        }
        _uiState.update { it.copy(lessonsLoading = false) }

        _uiState.update { it.copy(assessmentsLoading = true, assessmentsError = null) }
        if (staffId != null) {
            runCatching {
                assessmentsClient.fetchSubjectAssessmentGroups(session, staffId)
            }.onSuccess { groups ->
                val toMark = groups.sumOf { group ->
                    (group.upcoming + group.past).count { item ->
                        item.totalStudents > 0 && item.markedCount < item.totalStudents
                    }
                }
                _uiState.update {
                    it.copy(
                        assessmentGroups = groups,
                        assessmentsToMarkCount = toMark,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        assessmentGroups = emptyList(),
                        assessmentsToMarkCount = 0,
                        assessmentsError = error.message ?: "Failed to load assessments",
                    )
                }
            }
        } else {
            _uiState.update {
                it.copy(assessmentGroups = emptyList(), assessmentsError = "Could not load staff ID")
            }
        }
        _uiState.update { it.copy(assessmentsLoading = false) }

        _uiState.update { it.copy(messagesLoading = true, messagesError = null) }
        runCatching {
            messagesClient.fetchMessages(session, limit = 5)
        }.onSuccess { messages ->
            _uiState.update { it.copy(messages = messages) }
        }.onFailure { error ->
            _uiState.update {
                it.copy(messages = emptyList(), messagesError = error.message ?: "Failed to load messages")
            }
        }
        _uiState.update { it.copy(messagesLoading = false) }
    }

    private fun reset() {
        loadedSessionId = null
        _uiState.value = HomeDashboardUiState()
    }
}
