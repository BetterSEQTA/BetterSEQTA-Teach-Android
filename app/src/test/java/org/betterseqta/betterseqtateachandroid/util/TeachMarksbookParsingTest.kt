package org.betterseqta.betterseqtateachandroid.util

import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class TeachMarksbookParsingTest {

    @Test
    fun buildMarksbookSavePayload_includesStudentMarksAndIsNumeric() {
        val template = buildJsonObject {
            put("target", 10)
            put("emblems", buildJsonArray { })
        }
        val payload = buildMarksbookSavePayload(
            changes = listOf(
                MarkScoreChange(
                    studentId = 3112,
                    assessmentId = 26523,
                    criterionId = 46461,
                    score = "10",
                    saveTemplate = template,
                ),
            ),
            isNumeric = 1,
        )
        val student = payload["3112"]!!.jsonObject
        val marks = student["marks"]!!.jsonObject
        val assessment = marks["26523"]!!.jsonObject
        val criterion = assessment["46461"]!!.jsonObject
        assertEquals("46461", criterion["criterionID"]!!.jsonPrimitive.content)
        assertEquals(10, criterion["score"]!!.jsonPrimitive.content.toInt())
        assertEquals(1, payload["isNumeric"]!!.jsonPrimitive.content.toInt())
    }
}
