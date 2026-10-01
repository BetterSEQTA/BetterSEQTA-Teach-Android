package org.betterseqta.betterseqtateachandroid.ui.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.betterseqta.betterseqtateachandroid.data.remote.TeachNoticesClient
import org.betterseqta.betterseqtateachandroid.domain.model.TeachNotice
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.util.AppDateFormatters

data class NoticesUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val notices: List<TeachNotice> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class NoticesViewModel @Inject constructor(
    private val noticesClient: TeachNoticesClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoticesUiState())
    val uiState: StateFlow<NoticesUiState> = _uiState.asStateFlow()

    fun noticeById(id: Int): TeachNotice? =
        _uiState.value.notices.firstOrNull { it.id == id }

    fun shiftDate(days: Int) {
        _uiState.update { state ->
            state.copy(selectedDate = state.selectedDate.plusDays(days.toLong()))
        }
    }

    fun setDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun load(session: TeachSession?) {
        viewModelScope.launch {
            if (session == null) {
                _uiState.value = NoticesUiState()
                return@launch
            }
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, notices = emptyList())
            }
            val dateString = AppDateFormatters.formatIsoDate(_uiState.value.selectedDate)
            runCatching {
                noticesClient.fetchNotices(session, dateString)
            }.onSuccess { (notices, _) ->
                _uiState.update { it.copy(notices = notices, isLoading = false) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Couldn't load notices",
                    )
                }
            }
        }
    }
}
