package org.betterseqta.betterseqtateachandroid.ui.timetable

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
import org.betterseqta.betterseqtateachandroid.data.remote.TeachTimetableClient
import org.betterseqta.betterseqtateachandroid.domain.model.TeachLesson
import org.betterseqta.betterseqtateachandroid.domain.repository.SessionRepository
import org.betterseqta.betterseqtateachandroid.util.AppDateFormatters
import java.time.LocalDate
import javax.inject.Inject

data class TimetableUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val lessons: List<TeachLesson> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val timetableClient: TeachTimetableClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimetableUiState())
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepository.state
                .map { it.session?.jsessionId }
                .distinctUntilChanged()
                .collect { load() }
        }
    }

    fun shiftDate(days: Int) {
        _uiState.update { it.copy(selectedDate = it.selectedDate.plusDays(days.toLong())) }
    }

    fun setSelectedDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun load() {
        viewModelScope.launch {
            val session = sessionRepository.state.value.session
            if (session == null) {
                reset()
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null, lessons = emptyList()) }

            sessionRepository.fetchStaffIdAndUserInfoIfNeeded()
            val staffId = sessionRepository.state.value.staffId
            if (staffId == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Could not load staff ID.")
                }
                return@launch
            }

            val dateString = AppDateFormatters.isoFromLocalDate(_uiState.value.selectedDate)
            runCatching {
                timetableClient.fetchLessons(session, staffId, dateString, dateString)
            }.onSuccess { lessons ->
                _uiState.update { it.copy(lessons = lessons) }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Failed to load timetable") }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun reset() {
        _uiState.value = TimetableUiState()
    }
}
