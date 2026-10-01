package org.betterseqta.betterseqtateachandroid.domain.model

import kotlinx.serialization.json.JsonObject

data class TeachMarksbookCriterion(
    val id: Int,
    val label: String,
    val target: Int?,
)

data class TeachMarksbookAssessment(
    val id: Int,
    val title: String,
    val weight: Int?,
    val setDescription: String,
    val setColourHex: String?,
    val criteria: List<TeachMarksbookCriterion>,
)

data class TeachMarksbookStudentRow(
    val id: Int,
    val displayName: String,
    val subtitle: String,
    val cells: Map<MarkCellKey, TeachMarkCell>,
)

data class MarkCellKey(
    val assessmentId: Int,
    val criterionId: Int,
)

data class TeachMarkCell(
    val score: String,
    /** Original criterion JSON from SEQTA — used to build save payloads. */
    val saveTemplate: JsonObject,
)

data class TeachMarksbookSnapshot(
    val title: String,
    val classLabel: String,
    val programId: Int,
    val metaClassId: Int,
    val isNumeric: Int,
    val assessments: List<TeachMarksbookAssessment>,
    val students: List<TeachMarksbookStudentRow>,
)
