package org.betterseqta.betterseqtateachandroid.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeqtaApiTest {

    @Test
    fun post_setsJsessionIdCookieHeader() = runTest {
        var capturedCookie: String? = null
        val engine = MockEngine { request ->
            capturedCookie = request.headers[HttpHeaders.Cookie]
            respond(
                content = """{"payload":{}}""",
                status = HttpStatusCode.OK,
            )
        }
        val client = HttpClient(engine) {
            expectSuccess = false
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val api = SeqtaApi(client)
        val session = TeachSession(
            baseUrl = "https://school.example.edu.au",
            jsessionId = "abc123session",
        )
        val body = buildJsonObject { put("mode", "normal") }

        val response = api.post(session, "/seqta/ta/login", body)

        assertEquals(200, response.statusCode)
        assertEquals("JSESSIONID=abc123session", capturedCookie)
        assertTrue(requestUrlEndsWith(engine, "/seqta/ta/login"))
        client.close()
    }

    private fun requestUrlEndsWith(engine: MockEngine, suffix: String): Boolean {
        val history = engine.requestHistory
        if (history.isEmpty()) return false
        return history.last().url.encodedPath.endsWith(suffix)
    }
}
