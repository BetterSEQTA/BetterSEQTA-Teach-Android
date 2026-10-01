package org.betterseqta.betterseqtateachandroid.data.remote

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TeachMessagesClientTest {

    private val client = TeachMessagesClient(mockk(relaxed = true), mockk(relaxed = true))

    @Test
    fun parseMessage_mapsReadAndSubject() {
        val message = client.parseMessage(
            buildJsonObject {
                put("id", 10)
                put("subject", "Hello")
                put("sender", "Teacher")
                put("read", 0)
                put("starred", 0)
            },
        )
        assertEquals(10, message?.messageId)
        assertEquals("Hello", message?.subject)
        assertFalse(message?.read == true)
    }

    @Test
    fun parseMessageDetail_usesContentsField() {
        val detail = client.parseMessageDetail(
            buildJsonObject {
                put("id", 5)
                put("subject", "Details")
                put("contents", "<p>Body</p>")
            },
        )
        assertEquals(5, detail.id)
        assertEquals("<p>Body</p>", detail.body)
    }
}
