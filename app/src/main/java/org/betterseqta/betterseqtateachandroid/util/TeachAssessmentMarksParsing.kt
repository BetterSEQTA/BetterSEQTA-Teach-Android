package org.betterseqta.betterseqtateachandroid.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class AssessmentProgressCounts(
    val submittedCount: Int,
    val markedCount: Int,
    val totalStudents: Int,
)

/**
 * Mirrors [BetterSEQTA+ teach home marksbook logic](https://github.com/AdenMGB/BetterSEQTA-Plus/blob/bs-teacj/src/seqta/utils/Loaders/LoadTeachHomePage.ts).
 */
fun countAssessmentProgress(
    assessment: JsonObject,
    metaClassId: Int,
    students: JsonArray,
): AssessmentProgressCounts {
    val assessmentId = assessment.intField("id") ?: return AssessmentProgressCounts(0, 0, 0)
    val criteria = assessment["criteria"]?.jsonArray ?: JsonArray(emptyList())
    var submittedCount = 0
    var markedCount = 0
    var totalStudents = 0

    for (element in students) {
        val student = element.jsonObject
        val marksRoot = student["marks"]?.jsonObject ?: continue
        val studentMarks = marksRoot[assessmentId.toString()]?.jsonObject ?: continue
        totalStudents++

        if (studentSubmitted(studentMarks, criteria)) {
            submittedCount++
        }
        if (studentMarked(studentMarks, criteria)) {
            markedCount++
        }
    }

    return AssessmentProgressCounts(
        submittedCount = submittedCount,
        markedCount = markedCount,
        totalStudents = totalStudents,
    )
}

private fun studentSubmitted(studentMarks: JsonObject, criteria: JsonArray): Boolean {
    if (studentMarks.seqtaTruthyField("submitted", "submit", "s")) return true
    val files = studentMarks["files"]?.jsonArray
    if (files != null && files.isNotEmpty()) return true
    val work = studentMarks["work"]?.jsonPrimitive?.contentOrNull
    if (!work.isNullOrBlank()) return true
    for (criterionElement in criteria) {
        val criterionId = criterionElement.jsonObject.intField("id") ?: continue
        val criterionData = studentMarks[criterionId.toString()]?.jsonObject ?: continue
        if (criterionData.seqtaTruthyField("submitted", "submit", "s")) return true
        val comment = criterionData["comment"]?.jsonPrimitive?.contentOrNull
        if (!comment.isNullOrBlank()) return true
    }
    return false
}

private fun studentMarked(studentMarks: JsonObject, criteria: JsonArray): Boolean {
    if (criteria.isEmpty()) {
        val directScore = studentMarks["score"]?.jsonPrimitive?.contentOrNull
        return !directScore.isNullOrBlank()
    }
    for (criterionElement in criteria) {
        val criterionId = criterionElement.jsonObject.intField("id") ?: continue
        val criterionData = studentMarks[criterionId.toString()]?.jsonObject ?: continue
        val score = criterionData["score"]?.jsonPrimitive?.contentOrNull
        if (!score.isNullOrBlank()) return true
        criterionData["score"]?.jsonPrimitive?.intOrNull?.let { return true }
    }
    return false
}
