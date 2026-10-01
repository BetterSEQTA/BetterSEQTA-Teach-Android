package org.betterseqta.betterseqtateachandroid.ui.assessments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.betterseqta.betterseqtateachandroid.data.remote.TeachAssessmentsClient
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSubjectAssessmentGroup
import org.betterseqta.betterseqtateachandroid.domain.repository.SessionRepository

enum class AssessmentTimeFilter {
    All,
    Upcoming,
    Past,
}

data class CourseAssessmentsUiState(
    val groups: List<TeachSubjectAssessmentGroup> = emptyList(),
    val filter: AssessmentTimeFilter = AssessmentTimeFilter.All,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class CourseAssessmentsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assessmentsClient: TeachAssessmentsClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseAssessmentsUiState())
    val uiState: StateFlow<CourseAssessmentsUiState> = _uiState.asStateFlow()

    private var loadedSessionId: String? = null

    init {
        viewModelScope.launch {
            sessionRepository.state
                .map { it.session?.jsessionId }
                .distinctUntilChanged()
                .collect { loadIfNeeded() }
        }
    }

    fun setFilter(filter: AssessmentTimeFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun refresh() {
        viewModelScope.launch { load(force = true) }
    }

    private fun loadIfNeeded() {
        viewModelScope.launch {
            val session = sessionRepository.state.value.session
            if (session == null) {
                loadedSessionId = null
                _uiState.value = CourseAssessmentsUiState()
                return@launch
            }
            if (loadedSessionId == session.jsessionId) return@launch
            load(force = false)
        }
    }

    private suspend fun load(force: Boolean) {
        val session = sessionRepository.state.value.session ?: return
        if (!force && loadedSessionId == session.jsessionId) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        sessionRepository.fetchStaffIdAndUserInfoIfNeeded()
        val staffId = sessionRepository.state.value.staffId
        if (staffId == null) {
            _uiState.update {
                it.copy(isLoading = false, errorMessage = "Could not load staff ID")
            }
            return
        }

        runCatching {
            assessmentsClient.fetchSubjectAssessmentGroups(session, staffId, maxPrograms = 32)
        }.onSuccess { groups ->
            loadedSessionId = session.jsessionId
            _uiState.update { it.copy(groups = groups, isLoading = false) }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to load assessments",
                    groups = emptyList(),
                )
            }
        }
    }
}

fun CourseAssessmentsUiState.filteredGroups(): List<TeachSubjectAssessmentGroup> {
    return groups.mapNotNull { group ->
        val upcoming = when (filter) {
            AssessmentTimeFilter.Past -> emptyList()
            else -> group.upcoming
        }
        val past = when (filter) {
            AssessmentTimeFilter.Upcoming -> emptyList()
            else -> group.past
        }
        if (upcoming.isEmpty() && past.isEmpty()) return@mapNotNull null
        group.copy(upcoming = upcoming, past = past)
    }
}
