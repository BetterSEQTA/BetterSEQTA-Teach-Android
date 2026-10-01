package org.betterseqta.betterseqtateachandroid.data.remote

import io.mockk.any
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.junit.Assert.assertEquals
import org.junit.Test

class TeachNoticesClientTest {

    private val seqtaApi: SeqtaApi = mockk()
    private val client = TeachNoticesClient(seqtaApi)

    @Test
    fun parseNotice_mapsFields() {
        val notice = client.parseNotice(
            buildJsonObject {
                put("id", 42)
                put("title", "Assembly")
                put("label_title", "Whole School")
                put("staff", "Admin")
                put("colour", "#ff0000")
                put("contents", "<p>Hi</p>")
            },
        )
        assertEquals(42, notice?.id)
        assertEquals("Assembly", notice?.title)
        assertEquals("Whole School", notice?.labelTitle)
    }

    @Test
    fun fetchNotices_parsesPayload() = runTest {
        val session = TeachSession(baseUrl = "https://school.example", jsessionId = "abc")
        coEvery {
            seqtaApi.post(session, "/seqta/ta/json/notices/load", any())
        } returns SeqtaHttpResponse(
            statusCode = 200,
            body = """
                {
                  "payload": {
                    "notices": [
                      { "id": 1, "title": "Test" }
                    ],
                    "labels": [
                      { "id": 2, "title": "General" }
                    ]
                  }
                }
            """.trimIndent(),
        )

        val (notices, labels) = client.fetchNotices(session, "2026-10-01")
        assertEquals(1, notices.size)
        assertEquals("Test", notices.first().title)
        assertEquals(1, labels.size)
        assertEquals("General", labels.first().title)
    }
}
