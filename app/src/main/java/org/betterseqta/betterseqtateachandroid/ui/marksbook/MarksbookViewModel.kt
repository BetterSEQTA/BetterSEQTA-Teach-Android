package org.betterseqta.betterseqtateachandroid.ui.marksbook

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.betterseqta.betterseqtateachandroid.data.remote.TeachMarksbookClient
import org.betterseqta.betterseqtateachandroid.domain.model.MarkCellKey
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMarksbookAssessment
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMarksbookCriterion
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMarksbookSnapshot
import org.betterseqta.betterseqtateachandroid.domain.repository.SessionRepository
import org.betterseqta.betterseqtateachandroid.util.AppDateFormatters
import org.betterseqta.betterseqtateachandroid.util.MarkScoreChange
import org.betterseqta.betterseqtateachandroid.util.averageNumericScore

data class MarksbookUiState(
    val snapshot: TeachMarksbookSnapshot? = null,
    val selectedAssessmentId: Int? = null,
    val selectedCriterionId: Int? = null,
    val pendingScores: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveError: String? = null,
    val saveSuccess: Boolean = false,
)

@HiltViewModel
class MarksbookViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val marksbookClient: TeachMarksbookClient,
) : ViewModel() {

    private val programId: Int = checkNotNull(savedStateHandle["programId"])
    private val metaClassId: Int = checkNotNull(savedStateHandle["metaClassId"])
    private val initialAssessmentId: Int = checkNotNull(savedStateHandle["assessmentId"])

    private val _uiState = MutableStateFlow(MarksbookUiState())
    val uiState: StateFlow<MarksbookUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    fun refresh() {
        viewModelScope.launch { load(force = true) }
    }

    fun selectAssessment(assessmentId: Int) {
        val assessment = _uiState.value.snapshot?.assessments?.find { it.id == assessmentId } ?: return
        _uiState.update {
            it.copy(
                selectedAssessmentId = assessmentId,
                selectedCriterionId = assessment.criteria.firstOrNull()?.id,
            )
        }
    }

    fun selectCriterion(criterionId: Int) {
        _uiState.update { it.copy(selectedCriterionId = criterionId) }
    }

    fun updateScore(studentId: Int, score: String) {
        val state = _uiState.value
        val assessmentId = state.selectedAssessmentId ?: return
        val criterionId = state.selectedCriterionId ?: return
        val key = MarkCellKey(assessmentId, criterionId)
        val baseline = state.snapshot?.students
            ?.find { it.id == studentId }
            ?.cells
            ?.get(key)
            ?.score
            ?: ""
        _uiState.update {
            val nextPending = it.pendingScores.toMutableMap()
            val cellKey = pendingScoreKey(studentId, key)
            if (score.trim() == baseline.trim()) {
                nextPending.remove(cellKey)
            } else {
                nextPending[cellKey] = score
            }
            it.copy(pendingScores = nextPending, saveSuccess = false)
        }
    }

    fun scoreFor(studentId: Int, key: MarkCellKey): String {
        val cellKey = pendingScoreKey(studentId, key)
        _uiState.value.pendingScores[cellKey]?.let { return it }
        return _uiState.value.snapshot?.students
            ?.find { it.id == studentId }
            ?.cells
            ?.get(key)
            ?.score
            ?: ""
    }

    fun hasUnsavedChanges(): Boolean = _uiState.value.pendingScores.isNotEmpty()

    fun clearSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun save() {
        viewModelScope.launch {
            val session = sessionRepository.state.value.session ?: return@launch
            val state = _uiState.value
            val snapshot = state.snapshot ?: return@launch
            val changes = buildChanges(state, snapshot)
            if (changes.isEmpty()) return@launch

            _uiState.update { it.copy(isSaving = true, saveError = null) }
            runCatching {
                marksbookClient.saveMarks(session, changes, snapshot.isNumeric)
            }.onSuccess {
                _uiState.update { current ->
                    val merged = mergeSavedScores(current, changes)
                    current.copy(
                        snapshot = merged,
                        pendingScores = emptyMap(),
                        isSaving = false,
                        saveSuccess = true,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = error.message ?: "Save failed",
                    )
                }
            }
        }
    }

    private suspend fun load(force: Boolean = false) {
        val session = sessionRepository.state.value.session ?: return
        if (!force && _uiState.value.snapshot != null) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        runCatching {
            marksbookClient.loadMarksbook(
                session = session,
                programId = programId,
                metaClassId = metaClassId,
                dateIso = AppDateFormatters.todayIso(),
            )
        }.onSuccess { snapshot ->
            val assessmentId = snapshot.assessments.find { it.id == initialAssessmentId }?.id
                ?: snapshot.assessments.firstOrNull()?.id
            val criterionId = snapshot.assessments
                .find { it.id == assessmentId }
                ?.criteria
                ?.firstOrNull()
                ?.id
            _uiState.update {
                it.copy(
                    snapshot = snapshot,
                    selectedAssessmentId = assessmentId,
                    selectedCriterionId = criterionId,
                    isLoading = false,
                    pendingScores = emptyMap(),
                )
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to load marksbook",
                )
            }
        }
    }

    private fun buildChanges(
        state: MarksbookUiState,
        snapshot: TeachMarksbookSnapshot,
    ): List<MarkScoreChange> {
        return state.pendingScores.mapNotNull { (cellKey, score) ->
            val parts = parsePendingScoreKey(cellKey) ?: return@mapNotNull null
            val markKey = MarkCellKey(parts.assessmentId, parts.criterionId)
            val template = snapshot.students
                .find { it.id == parts.studentId }
                ?.cells
                ?.get(markKey)
                ?.saveTemplate
                ?: return@mapNotNull null
            MarkScoreChange(
                studentId = parts.studentId,
                assessmentId = parts.assessmentId,
                criterionId = parts.criterionId,
                score = score,
                saveTemplate = template,
            )
        }
    }

    private fun mergeSavedScores(
        state: MarksbookUiState,
        changes: List<MarkScoreChange>,
    ): TeachMarksbookSnapshot? {
        val snapshot = state.snapshot ?: return null
        val changeMap = changes.associateBy { Triple(it.studentId, it.assessmentId, it.criterionId) }
        val students = snapshot.students.map { student ->
            val cells = student.cells.mapValues { (key, cell) ->
                val change = changeMap[Triple(student.id, key.assessmentId, key.criterionId)]
                if (change != null) {
                    cell.copy(score = change.score)
                } else {
                    cell
                }
            }
            student.copy(cells = cells)
        }
        return snapshot.copy(students = students)
    }
}

private fun pendingScoreKey(studentId: Int, key: MarkCellKey): String =
    "$studentId/${key.assessmentId}/${key.criterionId}"

private data class PendingScoreParts(
    val studentId: Int,
    val assessmentId: Int,
    val criterionId: Int,
)

private fun parsePendingScoreKey(raw: String): PendingScoreParts? {
    val parts = raw.split('/')
    if (parts.size != 3) return null
    val studentId = parts[0].toIntOrNull() ?: return null
    val assessmentId = parts[1].toIntOrNull() ?: return null
    val criterionId = parts[2].toIntOrNull() ?: return null
    return PendingScoreParts(studentId, assessmentId, criterionId)
}

fun MarksbookUiState.selectedAssessment(): TeachMarksbookAssessment? =
    snapshot?.assessments?.find { it.id == selectedAssessmentId }

fun MarksbookUiState.selectedCriterion(): TeachMarksbookCriterion? =
    selectedAssessment()?.criteria?.find { it.id == selectedCriterionId }

fun MarksbookUiState.markedCount(): Int {
    val assessmentId = selectedAssessmentId ?: return 0
    val criterionId = selectedCriterionId ?: return 0
    val key = MarkCellKey(assessmentId, criterionId)
    return snapshot?.students?.count { student ->
        val score = student.cells[key]?.score.orEmpty()
        score.isNotBlank()
    } ?: 0
}

fun MarksbookUiState.averageScoreLabel(isNumeric: Boolean): String? {
    if (!isNumeric) return null
    val assessmentId = selectedAssessmentId ?: return null
    val criterionId = selectedCriterionId ?: return null
    val key = MarkCellKey(assessmentId, criterionId)
    val scores = snapshot?.students?.mapNotNull { student ->
        student.cells[key]?.score?.takeIf { it.isNotBlank() }
    }.orEmpty()
    val avg = averageNumericScore(scores) ?: return null
    return if (avg == avg.toLong().toDouble()) {
        avg.toLong().toString()
    } else {
        "%.1f".format(avg)
    }
}
