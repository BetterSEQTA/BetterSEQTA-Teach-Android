package org.betterseqta.betterseqtateachandroid.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import org.betterseqta.betterseqtateachandroid.core.network.SeqtaUrlBuilder
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared SEQTA Teach API POST helper with JSESSIONID cookie (iOS SeqtaRequestHelper).
 */
@Singleton
class SeqtaApi @Inject constructor(
    private val httpClient: HttpClient,
) {
    suspend fun post(
        session: TeachSession,
        path: String,
        body: JsonObject,
    ): SeqtaHttpResponse {
        val url = runCatching { SeqtaUrlBuilder.build(session.baseUrl, path) }
            .getOrElse { throw SeqtaApiException.InvalidUrl() }

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("Cookie", "JSESSIONID=${session.jsessionId}")
            setBody(body.toString())
        }

        return SeqtaHttpResponse(
            body = response.bodyAsText(),
            statusCode = response.status.value,
        )
    }
}
