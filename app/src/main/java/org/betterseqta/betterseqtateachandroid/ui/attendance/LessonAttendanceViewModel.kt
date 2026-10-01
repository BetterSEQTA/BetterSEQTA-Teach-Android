package org.betterseqta.betterseqtateachandroid.ui.attendance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.betterseqta.betterseqtateachandroid.data.local.AttendancePreferences
import org.betterseqta.betterseqtateachandroid.data.remote.TeachAttendanceClient
import org.betterseqta.betterseqtateachandroid.domain.model.AttendanceSummary
import org.betterseqta.betterseqtateachandroid.domain.model.AttendanceViewMode
import org.betterseqta.betterseqtateachandroid.domain.model.TeachAttendanceStudent
import org.betterseqta.betterseqtateachandroid.domain.model.TeachAttendanceType
import org.betterseqta.betterseqtateachandroid.domain.model.TeachLesson
import org.betterseqta.betterseqtateachandroid.domain.repository.SessionRepository
import org.betterseqta.betterseqtateachandroid.navigation.LessonAttendanceNavCodec
import javax.inject.Inject

data class ResolvedAttendanceStatus(
    val label: String,
    val code: String,
)

data class LessonAttendanceUiState(
    val lesson: TeachLesson? = null,
    val date: String = "",
    val students: List<TeachAttendanceStudent> = emptyList(),
    val attendanceTypes: List<TeachAttendanceType> = emptyList(),
    val summaryByStudent: Map<Int, AttendanceSummary> = emptyMap(),
    val pendingChanges: Map<Int, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveError: String? = null,
    val viewMode: AttendanceViewMode = AttendanceViewMode.List,
    val cardIndex: Int = 0,
    val typePickerStudentId: Int? = null,
)

@HiltViewModel
class LessonAttendanceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val attendanceClient: TeachAttendanceClient,
    private val attendancePreferences: AttendancePreferences,
) : ViewModel() {

    private val routeArgs = savedStateHandle.get<String>("payload")?.let(LessonAttendanceNavCodec::decode)

    private val _uiState = MutableStateFlow(
        LessonAttendanceUiState(
            lesson = routeArgs?.lesson,
            date = routeArgs?.date ?: "",
            viewMode = attendancePreferences.getViewMode(),
        ),
    )
    val uiState: StateFlow<LessonAttendanceUiState> = _uiState.asStateFlow()

    private val cycleTypes = listOf("yes", "no")

    init {
        load()
    }

    fun reload() {
        load()
    }

    fun setViewMode(mode: AttendanceViewMode) {
        attendancePreferences.setViewMode(mode)
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun cycleAttendance(studentId: Int) {
        val student = _uiState.value.students.firstOrNull { it.id == studentId } ?: return
        val current = effectiveCode(student)
        val next = if (current != null) {
            val idx = cycleTypes.indexOf(current)
            if (idx >= 0) cycleTypes[(idx + 1) % cycleTypes.size] else cycleTypes.first()
        } else {
            cycleTypes.first()
        }
        _uiState.update { it.copy(pendingChanges = it.pendingChanges + (studentId to next)) }
    }

    fun setPending(studentId: Int, code: String) {
        _uiState.update { it.copy(pendingChanges = it.pendingChanges + (studentId to code)) }
        if (_uiState.value.viewMode == AttendanceViewMode.Card) {
            advanceCard()
        }
    }

    fun clearPending(studentId: Int) {
        _uiState.update { state ->
            state.copy(pendingChanges = state.pendingChanges - studentId)
        }
        if (_uiState.value.viewMode == AttendanceViewMode.Card) {
            advanceCard()
        }
    }

    fun showTypePicker(studentId: Int) {
        _uiState.update { it.copy(typePickerStudentId = studentId) }
    }

    fun dismissTypePicker() {
        _uiState.update { it.copy(typePickerStudentId = null) }
    }

    fun advanceCard() {
        _uiState.update { state ->
            val next = if (state.cardIndex < state.students.size - 1) {
                state.cardIndex + 1
            } else {
                state.students.size
            }
            state.copy(cardIndex = next)
        }
    }

    fun previousCard() {
        _uiState.update { state ->
            if (state.cardIndex > 0) state.copy(cardIndex = state.cardIndex - 1) else state
        }
    }

    fun markSwipe(studentId: Int, code: String) {
        setPending(studentId, code)
    }

    fun clearSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    fun effectiveCode(student: TeachAttendanceStudent): String? {
        val pending = _uiState.value.pendingChanges[student.id]
        if (pending != null) return pending
        val lessonId = _uiState.value.lesson?.id ?: return null
        return student.attendance[lessonId]?.get("detail")
    }

    fun resolveStatus(student: TeachAttendanceStudent): ResolvedAttendanceStatus? {
        val code = effectiveCode(student) ?: return null
        val label = _uiState.value.attendanceTypes.firstOrNull { it.code == code }?.label
            ?: code.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        return ResolvedAttendanceStatus(label = label, code = code)
    }

    fun selectableTypes(): List<TeachAttendanceType> =
        _uiState.value.attendanceTypes.filter { it.isReset != true && it.code != "kiosk-zero" }

    fun save() {
        val session = sessionRepository.state.value.session
        val lesson = _uiState.value.lesson
        val pending = _uiState.value.pendingChanges
        if (session == null || lesson == null || pending.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            runCatching {
                val studentDict = pending.mapKeys { it.key.toString() }.mapValues { it.value }
                attendanceClient.saveAttendance(
                    session = session,
                    attendance = mapOf(lesson.id to studentDict),
                )
            }.onSuccess {
                _uiState.update { it.copy(pendingChanges = emptyMap()) }
                load()
            }.onFailure { error ->
                _uiState.update { it.copy(saveError = error.message ?: "Save failed") }
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            val session = sessionRepository.state.value.session
            val lesson = _uiState.value.lesson
            val date = _uiState.value.date
            if (session == null) {
                _uiState.update { it.copy(errorMessage = "Not logged in.") }
                return@launch
            }
            val classunitId = lesson?.classunitId
            if (lesson == null || classunitId == null) {
                _uiState.update { it.copy(errorMessage = "No class unit for this lesson.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val types = attendanceClient.fetchAttendanceTypes(session)
                val classIds = if (lesson.isAdhoc) listOf(classunitId) else listOf(classunitId, classunitId)
                val payload = attendanceClient.fetchAttendanceLoad(
                    session = session,
                    date = date,
                    classunitIds = classIds,
                    isAdhoc = lesson.isAdhoc,
                )
                val students = attendanceClient.parseStudents(payload)
                val summary = if (students.isNotEmpty()) {
                    attendanceClient.fetchAttendanceSummary(
                        session = session,
                        date = date,
                        studentIds = students.map { it.id },
                        classunitIds = classIds,
                        isAdhoc = lesson.isAdhoc,
                    )
                } else {
                    emptyMap()
                }
                Triple(types, students, summary)
            }.onSuccess { (types, students, summary) ->
                _uiState.update {
                    it.copy(
                        attendanceTypes = types,
                        students = students,
                        summaryByStudent = summary,
                        cardIndex = 0,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        students = emptyList(),
                        errorMessage = error.message ?: "Failed to load attendance",
                    )
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
