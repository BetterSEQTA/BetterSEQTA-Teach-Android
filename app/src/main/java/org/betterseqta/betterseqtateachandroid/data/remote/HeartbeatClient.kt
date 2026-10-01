package org.betterseqta.betterseqtateachandroid.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.betterseqta.betterseqtateachandroid.core.network.SeqtaUrlBuilder
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeartbeatClient @Inject constructor(
    private val httpClient: HttpClient,
) {
    private val timestampFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S", Locale.US)

    suspend fun sendHeartbeat(session: TeachSession): HeartbeatResult {
        val url = runCatching {
            SeqtaUrlBuilder.build(session.baseUrl, "/seqta/ta/heartbeat")
        }.getOrElse {
            return HeartbeatResult.Failure("Invalid heartbeat URL")
        }

        val timestamp = LocalDateTime.now().format(timestampFormatter)
        val body = buildJsonObject {
            put("hash", "")
            put("timestamp", timestamp)
        }

        val response = runCatching {
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                header("Cookie", "JSESSIONID=${session.jsessionId}")
                setBody(body.toString())
            }
        }.getOrElse {
            return HeartbeatResult.Failure(it.message ?: "Network error")
        }

        return when (response.status.value) {
            in 200..299 -> HeartbeatResult.Success(Instant.now())
            401, 403 -> HeartbeatResult.Unauthorized
            else -> HeartbeatResult.Failure("HTTP ${response.status.value}")
        }
    }
}
