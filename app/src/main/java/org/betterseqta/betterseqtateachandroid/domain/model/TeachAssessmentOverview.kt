package org.betterseqta.betterseqtateachandroid.domain.model

import java.time.LocalDate

data class TeachAssessmentItem(
    val id: Int,
    val title: String,
    val subjectLabel: String,
    val dueDate: LocalDate,
    val programId: Int,
    val metaClassId: Int,
    val submittedCount: Int,
    val totalStudents: Int,
    val markedCount: Int,
) {
    val isPastDue: Boolean
        get() = dueDate.isBefore(LocalDate.now())

    val submittedPercent: Int
        get() = if (totalStudents == 0) 0 else (submittedCount * 100) / totalStudents

    val markedPercent: Int
        get() = if (totalStudents == 0) 0 else (markedCount * 100) / totalStudents
}

data class TeachSubjectAssessmentGroup(
    val subjectLabel: String,
    val programTitle: String?,
    val upcoming: List<TeachAssessmentItem>,
    val past: List<TeachAssessmentItem>,
) {
    val totalCount: Int get() = upcoming.size + past.size
}
