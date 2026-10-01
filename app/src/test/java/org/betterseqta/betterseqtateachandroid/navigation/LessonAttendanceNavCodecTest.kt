package org.betterseqta.betterseqtateachandroid.navigation

import org.betterseqta.betterseqtateachandroid.domain.model.TeachLesson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LessonAttendanceNavCodecTest {

    @Test
    fun encodeDecode_roundTrip() {
        val lesson = TeachLesson(
            id = "42",
            from = "09:00",
            until = "10:00",
            description = "Maths",
            classunitId = 7,
            isAdhoc = false,
        )
        val encoded = LessonAttendanceNavCodec.encode(lesson, "2026-10-01")
        val decoded = LessonAttendanceNavCodec.decode(encoded)

        assertNotNull(decoded)
        assertEquals(lesson, decoded?.lesson)
        assertEquals("2026-10-01", decoded?.date)
    }
}
