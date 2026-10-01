package org.betterseqta.betterseqtateachandroid.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.betterseqta.betterseqtateachandroid.domain.model.TeachNotice
import org.betterseqta.betterseqtateachandroid.domain.model.TeachNoticeLabel
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeachNoticesClient @Inject constructor(
    private val seqtaApi: SeqtaApi,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchNotices(
        session: TeachSession,
        date: String,
    ): Pair<List<TeachNotice>, List<TeachNoticeLabel>> {
        val body = buildJsonObject { put("date", date) }
        val response = seqtaApi.post(session, "/seqta/ta/json/notices/load", body)
        if (response.statusCode !in 200..299) {
            throw SeqtaApiException.InvalidResponse()
        }
        val root = json.parseToJsonElement(response.body).jsonObject
        val payload = root["payload"]?.jsonObject ?: JsonObject(emptyMap())
        val notices = payload["notices"]?.jsonArray
            ?.mapNotNull { element -> parseNotice(element.jsonObject) }
            ?: emptyList()
        val labels = payload["labels"]?.jsonArray
            ?.mapNotNull { element -> parseLabel(element.jsonObject) }
            ?: emptyList()
        return notices to labels
    }

    internal fun parseNotice(raw: JsonObject): TeachNotice? {
        val id = runCatching { raw["id"]?.jsonPrimitive?.int }.getOrNull() ?: return null
        return TeachNotice(
            id = id,
            labelTitle = raw["label_title"]?.jsonPrimitive?.content,
            staff = raw["staff"]?.jsonPrimitive?.content,
            title = raw["title"]?.jsonPrimitive?.content ?: "Untitled",
            colour = raw["colour"]?.jsonPrimitive?.content,
            contents = raw["contents"]?.jsonPrimitive?.content,
            from = raw["from"]?.jsonPrimitive?.content,
            until = raw["until"]?.jsonPrimitive?.content,
            createdDate = raw["created_date"]?.jsonPrimitive?.content,
        )
    }

    internal fun parseLabel(raw: JsonObject): TeachNoticeLabel? {
        val id = runCatching { raw["id"]?.jsonPrimitive?.int }.getOrNull() ?: return null
        return TeachNoticeLabel(
            id = id,
            title = raw["title"]?.jsonPrimitive?.content ?: "",
            colour = raw["colour"]?.jsonPrimitive?.content,
        )
    }
}
