package org.betterseqta.betterseqtateachandroid.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.betterseqta.betterseqtateachandroid.domain.model.MarkCellKey
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMarkCell
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMarksbookAssessment
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMarksbookCriterion
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMarksbookSnapshot
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMarksbookStudentRow

data class MarkScoreChange(
    val studentId: Int,
    val assessmentId: Int,
    val criterionId: Int,
    val score: String,
    val saveTemplate: JsonObject,
)

fun parseMarksbookPayload(
    payload: JsonObject,
    programId: Int,
    metaClassId: Int,
): TeachMarksbookSnapshot {
    val title = payload["title"]?.jsonPrimitive?.contentOrNull ?: "Marksbook"
    val classLabel = payload["meta"]?.jsonArray?.firstOrNull()?.jsonObject?.let { meta ->
        meta["bridge_info"]?.jsonPrimitive?.contentOrNull
            ?: meta["name"]?.jsonPrimitive?.contentOrNull
    } ?: title
    val isNumeric = payload["isNumeric"]?.jsonPrimitive?.intOrNull ?: 1

    val assessments = mutableListOf<TeachMarksbookAssessment>()
    payload["assessmentSets"]?.jsonArray?.forEach { setElement ->
        val set = setElement.jsonObject
        val setDescription = set["description"]?.jsonPrimitive?.contentOrNull ?: "Assessments"
        val setColour = set["colour"]?.jsonPrimitive?.contentOrNull
        set["assessments"]?.jsonArray?.forEach { assessmentElement ->
            val assessment = assessmentElement.jsonObject
            val assessmentId = assessment.intField("id") ?: return@forEach
            val assessmentTitle = assessment["title"]?.jsonPrimitive?.contentOrNull ?: "Assessment"
            val weight = assessment.intField("weight")
            val criteria = assessment["criteria"]?.jsonArray?.mapNotNull { criterionElement ->
                val criterion = criterionElement.jsonObject
                val criterionId = criterion.intField("id") ?: return@mapNotNull null
                TeachMarksbookCriterion(
                    id = criterionId,
                    label = criterion["label"]?.jsonPrimitive?.contentOrNull ?: "Mark",
                    target = criterion.intField("target"),
                )
            }.orEmpty()
            if (criteria.isEmpty()) return@forEach
            assessments += TeachMarksbookAssessment(
                id = assessmentId,
                title = assessmentTitle,
                weight = weight,
                setDescription = setDescription,
                setColourHex = setColour,
                criteria = criteria,
            )
        }
    }

    val studentsArray = payload["students"]?.jsonArray ?: JsonArray(emptyList())
    val students = studentsArray.mapNotNull { element ->
        parseMarksbookStudent(element.jsonObject, assessments)
    }

    return TeachMarksbookSnapshot(
        title = title,
        classLabel = classLabel,
        programId = programId,
        metaClassId = metaClassId,
        isNumeric = isNumeric,
        assessments = assessments,
        students = students,
    )
}

private fun parseMarksbookStudent(
    student: JsonObject,
    assessments: List<TeachMarksbookAssessment>,
): TeachMarksbookStudentRow? {
    val studentId = student.intField("id") ?: return null
    val surname = student["surname"]?.jsonPrimitive?.contentOrNull.orEmpty()
    val pref = student["prefname"]?.jsonPrimitive?.contentOrNull
        ?: student["firstname"]?.jsonPrimitive?.contentOrNull.orEmpty()
    val displayName = when {
        surname.isNotBlank() && pref.isNotBlank() -> "$surname, $pref"
        surname.isNotBlank() -> surname
        else -> pref.ifBlank { student["code"]?.jsonPrimitive?.contentOrNull ?: "Student" }
    }
    val roll = student["roll"]?.jsonPrimitive?.contentOrNull
    val year = student["year"]?.jsonPrimitive?.contentOrNull
    val house = student["house"]?.jsonPrimitive?.contentOrNull
    val subtitle = listOfNotNull(house, roll, year).joinToString(" · ")

    val marksRoot = student["marks"]?.jsonObject ?: JsonObject(emptyMap())
    val cells = mutableMapOf<MarkCellKey, TeachMarkCell>()
    for (assessment in assessments) {
        val assessmentMarks = marksRoot[assessment.id.toString()]?.jsonObject
        for (criterion in assessment.criteria) {
            val criterionData = assessmentMarks?.get(criterion.id.toString())?.jsonObject
            if (criterionData != null) {
                val score = criterionData["score"]?.jsonPrimitive?.contentOrNull
                    ?: criterionData["score"]?.jsonPrimitive?.intOrNull?.toString()
                    ?: ""
                cells[MarkCellKey(assessment.id, criterion.id)] = TeachMarkCell(
                    score = score,
                    saveTemplate = criterionData,
                )
            } else {
                cells[MarkCellKey(assessment.id, criterion.id)] = TeachMarkCell(
                    score = "",
                    saveTemplate = buildEmptyCriterionTemplate(
                        assessmentId = assessment.id,
                        criterionId = criterion.id,
                        target = criterion.target,
                    ),
                )
            }
        }
    }

    return TeachMarksbookStudentRow(
        id = studentId,
        displayName = displayName,
        subtitle = subtitle,
        cells = cells,
    )
}

fun buildMarksbookSavePayload(
    changes: List<MarkScoreChange>,
    isNumeric: Int,
): JsonObject {
    return buildJsonObject {
        changes.groupBy { it.studentId }.forEach { (studentId, studentChanges) ->
            put(
                studentId.toString(),
                buildJsonObject {
                    put(
                        "marks",
                        buildJsonObject {
                            studentChanges.groupBy { it.assessmentId }.forEach { (assessmentId, assessmentChanges) ->
                                put(
                                    assessmentId.toString(),
                                    buildJsonObject {
                                        assessmentChanges.forEach { change ->
                                            put(
                                                change.criterionId.toString(),
                                                buildCriterionSaveObject(
                                                    template = change.saveTemplate,
                                                    assessmentId = change.assessmentId,
                                                    criterionId = change.criterionId,
                                                    score = change.score,
                                                    isNumeric = isNumeric,
                                                ),
                                            )
                                        }
                                    },
                                )
                            }
                        },
                    )
                },
            )
        }
        put("isNumeric", isNumeric)
    }
}

private fun buildCriterionSaveObject(
    template: JsonObject,
    assessmentId: Int,
    criterionId: Int,
    score: String,
    isNumeric: Int,
): JsonObject {
    return buildJsonObject {
        template.forEach { (key, value) ->
            if (key != "score") put(key, value)
        }
        put("assessmentID", assessmentId)
        put("criterionID", criterionId.toString())
        if (isNumeric == 1) {
            val numeric = score.trim().toDoubleOrNull()
            if (numeric != null) {
                if (numeric == numeric.toLong().toDouble()) {
                    put("score", numeric.toLong())
                } else {
                    put("score", numeric)
                }
            } else {
                put("score", score)
            }
        } else {
            put("score", score)
        }
        if (!template.containsKey("emblems")) put("emblems", buildJsonArray { })
        if (!template.containsKey("calculationType")) put("calculationType", "")
        if (!template.containsKey("override_external")) put("override_external", false)
    }
}

fun averageNumericScore(scores: List<String>): Double? {
    val values = scores.mapNotNull { it.trim().toDoubleOrNull() }
    if (values.isEmpty()) return null
    return values.average()
}

private fun buildEmptyCriterionTemplate(
    assessmentId: Int,
    criterionId: Int,
    target: Int?,
): JsonObject {
    return buildJsonObject {
        put("assessmentID", assessmentId)
        put("criterionID", criterionId.toString())
        put("score", "")
        put("emblems", buildJsonArray { })
        put("calculationType", "")
        put("override_external", false)
        if (target != null) put("target", target)
        put(
            "rubric",
            buildJsonObject { },
        )
    }
}
