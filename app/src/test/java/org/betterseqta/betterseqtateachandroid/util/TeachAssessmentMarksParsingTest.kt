package org.betterseqta.betterseqtateachandroid.util

import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.junit.Assert.assertEquals
import org.junit.Test

class TeachAssessmentMarksParsingTest {

    @Test
    fun countAssessmentProgress_countsSubmittedAndMarked() {
        val assessment = buildJsonObject {
            put("id", 10)
            put("criteria", buildJsonArray { })
        }
        val students = buildJsonArray {
            add(
                buildJsonObject {
                    put(
                        "marks",
                        buildJsonObject {
                            put(
                                "10",
                                buildJsonObject {
                                    put("submitted", true)
                                    put("score", "A")
                                },
                            )
                        },
                    )
                },
            )
            add(
                buildJsonObject {
                    put(
                        "marks",
                        buildJsonObject {
                            put(
                                "10",
                                buildJsonObject {
                                    put("submitted", false)
                                },
                            )
                        },
                    )
                },
            )
        }

        val progress = countAssessmentProgress(assessment, metaClassId = 1, students = students)
        assertEquals(2, progress.totalStudents)
        assertEquals(1, progress.submittedCount)
        assertEquals(1, progress.markedCount)
    }
}
