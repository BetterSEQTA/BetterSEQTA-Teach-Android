package org.betterseqta.betterseqtateachandroid.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.betterseqta.betterseqtateachandroid.core.network.SeqtaUrlBuilder
import org.betterseqta.betterseqtateachandroid.domain.model.Recipient
import org.betterseqta.betterseqtateachandroid.domain.model.RecipientType
import org.betterseqta.betterseqtateachandroid.domain.model.TeachLabel
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMessage
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMessageListParticipant
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMessageDetail
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMessageFile
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMessageParticipant
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.util.intField
import org.betterseqta.betterseqtateachandroid.util.seqtaTruthyField

@Singleton
class TeachMessagesClient @Inject constructor(
    private val seqtaApi: SeqtaApi,
    private val httpClient: HttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchLabels(session: TeachSession): List<TeachLabel> {
        val body = buildJsonObject { put("action", "labels") }
        val response = seqtaApi.post(session, "/seqta/ta/json/coneqtmessage/load", body)
        if (response.statusCode !in 200..299) return emptyList()
        val root = json.parseToJsonElement(response.body).jsonObject
        val payload = root["payload"]?.jsonArray ?: return emptyList()
        return payload.mapNotNull { element ->
            val dict = element.jsonObject
            val label = dict["label"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val unread = dict["unread"]?.jsonPrimitive?.intOrNull ?: 0
            TeachLabel(label = label, unread = unread)
        }
    }

    suspend fun fetchMessages(
        session: TeachSession,
        label: String = "inbox",
        searchValue: String = "",
        limit: Int = 100,
    ): List<TeachMessage> {
        val body = buildJsonObject {
            put("searchValue", searchValue)
            put("sortBy", "date")
            put("sortOrder", "desc")
            put("action", "list")
            put("label", label)
            put("offset", 0)
            put("limit", limit)
            put("datetimeUntil", JsonNull)
        }
        val response = seqtaApi.post(session, "/seqta/ta/json/coneqtmessage/load", body)
        if (response.statusCode !in 200..299) return emptyList()
        val root = json.parseToJsonElement(response.body).jsonObject
        val payload = root["payload"]?.jsonObject
        val messages = payload?.get("messages")?.jsonArray ?: return emptyList()
        return messages.mapNotNull { parseMessage(it.jsonObject) }
    }

    suspend fun markRead(session: TeachSession, ids: List<Int>, read: Boolean) {
        val body = buildJsonObject {
            put("mode", "x-read")
            put("read", read)
            putJsonArray("items") { ids.forEach { add(JsonPrimitive(it)) } }
        }
        val response = seqtaApi.post(session, "/seqta/ta/json/coneqtmessage/save", body)
        if (response.statusCode !in 200..299) throw SeqtaApiException.InvalidResponse()
    }

    suspend fun toggleStar(session: TeachSession, ids: List<Int>, starred: Boolean) {
        val body = buildJsonObject {
            put("mode", "x-star")
            put("starred", starred)
            putJsonArray("items") { ids.forEach { add(JsonPrimitive(it)) } }
        }
        val response = seqtaApi.post(session, "/seqta/ta/json/coneqtmessage/save", body)
        if (response.statusCode !in 200..299) throw SeqtaApiException.InvalidResponse()
    }

    suspend fun moveMessage(session: TeachSession, ids: List<Int>, label: String) {
        val body = buildJsonObject {
            put("mode", "x-label")
            put("label", label)
            putJsonArray("items") { ids.forEach { add(JsonPrimitive(it)) } }
        }
        val response = seqtaApi.post(session, "/seqta/ta/json/coneqtmessage/save", body)
        if (response.statusCode !in 200..299) throw SeqtaApiException.InvalidResponse()
    }

    suspend fun fetchMessageDetail(session: TeachSession, id: Int): TeachMessageDetail {
        val body = buildJsonObject {
            put("action", "message")
            put("id", id)
        }
        val response = seqtaApi.post(session, "/seqta/ta/json/coneqtmessage/load", body)
        if (response.statusCode !in 200..299) throw SeqtaApiException.InvalidResponse()
        val root = json.parseToJsonElement(response.body).jsonObject
        val payloadElement = root["payload"]
        val payload = when (payloadElement) {
            is JsonObject -> payloadElement
            else -> JsonObject(emptyMap())
        }
        return parseMessageDetail(payload)
    }

    suspend fun fetchContacts(session: TeachSession): List<Recipient> {
        val body = buildJsonObject { put("mode", "coneqt") }
        val response = seqtaApi.post(session, "/seqta/ta/json/contactIDs", body)
        if (response.statusCode !in 200..299) return emptyList()
        val root = json.parseToJsonElement(response.body).jsonObject
        val payload = root["payload"]?.jsonArray ?: return emptyList()
        return payload.mapNotNull { element ->
            val dict = element.jsonObject
            val id = dict["id"]?.jsonPrimitive?.intOrNull ?: return@mapNotNull null
            val name = dict["xx_display"]?.jsonPrimitive?.content
                ?: listOfNotNull(
                    dict["firstname"]?.jsonPrimitive?.content,
                    dict["surname"]?.jsonPrimitive?.content,
                ).joinToString(" ").trim()
            if (name.isEmpty()) return@mapNotNull null
            Recipient(id = id, displayName = name, type = RecipientType.Contact)
        }
    }

    suspend fun fetchStaff(session: TeachSession): List<Recipient> =
        fetchMsRecipients(session, "/seqta/ta/json/ms/staff", RecipientType.Staff)

    suspend fun fetchStudents(session: TeachSession): List<Recipient> =
        fetchMsRecipients(session, "/seqta/ta/json/ms/students", RecipientType.Student)

    suspend fun fetchTutors(session: TeachSession): List<Recipient> =
        fetchMsRecipients(session, "/seqta/ta/json/ms/tutors", RecipientType.Tutor)

    suspend fun sendMessage(
        session: TeachSession,
        subject: String,
        contents: String,
        participants: List<Recipient>,
        blind: Boolean = false,
        files: List<String> = emptyList(),
        inReplyTo: Int? = null,
    ): Int {
        val body = buildJsonObject {
            put("subject", subject)
            put("contents", contents)
            put("blind", blind)
            putJsonArray("participants") {
                participants.forEach { add(it.toParticipantJson()) }
            }
            putJsonArray("files") {
                files.forEach { add(JsonPrimitive(it)) }
            }
            inReplyTo?.let { put("inReplyTo", it) }
        }
        val response = seqtaApi.post(session, "/seqta/ta/json/coneqtmessage/save", body)
        if (response.statusCode !in 200..299) throw SeqtaApiException.InvalidResponse()
        val root = json.parseToJsonElement(response.body).jsonObject
        return root["payload"]?.jsonObject?.get("id")?.jsonPrimitive?.intOrNull ?: 0
    }

    suspend fun fetchReplyMeta(session: TeachSession, messageId: Int): JsonObject =
        fetchMeta(session, "replymeta", messageId)

    suspend fun fetchReplyAllMeta(session: TeachSession, messageId: Int): JsonObject =
        fetchMeta(session, "replyallmeta", messageId)

    suspend fun fetchForwardMeta(session: TeachSession, messageId: Int): JsonObject =
        fetchMeta(session, "forwardmeta", messageId)

    suspend fun downloadAttachment(session: TeachSession, file: TeachMessageFile): File {
        val url = SeqtaUrlBuilder.build(
            session.baseUrl,
            "/seqta/ta/file/get?type=message&file=${file.uuid}",
        )
        val safeName = file.filename.replace("/", "_")
        val destination = File.createTempFile("teach-msg-", "-$safeName")
        val response = httpClient.get(url) {
            header("Cookie", "JSESSIONID=${session.jsessionId}")
        }
        if (response.status.value !in 200..299) throw SeqtaApiException.InvalidResponse()
        response.bodyAsChannel().copyTo(destination.outputStream())
        return destination
    }

    private suspend fun fetchMeta(session: TeachSession, action: String, messageId: Int): JsonObject {
        val body = buildJsonObject {
            put("action", action)
            put("id", messageId)
        }
        val response = seqtaApi.post(session, "/seqta/ta/json/coneqtmessage/load", body)
        if (response.statusCode !in 200..299) return JsonObject(emptyMap())
        val root = json.parseToJsonElement(response.body).jsonObject
        return root["payload"]?.jsonObject ?: JsonObject(emptyMap())
    }

    private suspend fun fetchMsRecipients(
        session: TeachSession,
        path: String,
        type: RecipientType,
    ): List<Recipient> {
        val response = seqtaApi.post(session, path, buildJsonObject { })
        if (response.statusCode !in 200..299) return emptyList()
        val root = json.parseToJsonElement(response.body).jsonObject
        val elements = root["payload"]?.jsonObject?.get("elements")?.jsonArray ?: return emptyList()
        return elements.mapNotNull { element ->
            val dict = element.jsonObject
            val id = dict["xx_value"]?.jsonPrimitive?.intOrNull ?: return@mapNotNull null
            val name = dict["xx_display"]?.jsonPrimitive?.content ?: return@mapNotNull null
            Recipient(id = id, displayName = name, type = type)
        }
    }

    private fun parseMessage(raw: JsonObject): TeachMessage? {
        val messageId = raw.intField("id", "messageID", "message_id", "messageId")
        val id = messageId?.toString() ?: raw.hashCode().toString()
        val sender = raw["sender"]?.jsonPrimitive?.content
        val subject = raw["subject"]?.jsonPrimitive?.content
        val body = raw["body"]?.jsonPrimitive?.content
            ?: raw["content"]?.jsonPrimitive?.content
        val read = raw.seqtaTruthyField("read")
        val starred = raw.seqtaTruthyField("starred")
        val attachments = raw.seqtaTruthyField("attachments") ||
            (raw["attachmentCount"]?.jsonPrimitive?.intOrNull ?: 0) > 0
        val date = parseDate(raw["date"]?.jsonPrimitive?.content)
        val participants = raw["participants"]?.jsonArray?.mapNotNull { element ->
            val dict = element.jsonObject
            TeachMessageListParticipant(
                name = dict["name"]?.jsonPrimitive?.content ?: "Unknown",
                type = dict["type"]?.jsonPrimitive?.content ?: "",
            )
        } ?: emptyList()
        return TeachMessage(
            id = id,
            messageId = messageId,
            sender = sender,
            subject = subject,
            body = body,
            date = date,
            read = read,
            starred = starred,
            attachments = attachments,
            participants = participants,
        )
    }

    private fun parseMessageDetail(raw: JsonObject): TeachMessageDetail {
        val id = raw.intField("id") ?: 0
        val sender = raw["sender"]?.jsonPrimitive?.content
        val senderId = raw["sender_id"]?.jsonPrimitive?.intOrNull
        val senderType = raw["sender_type"]?.jsonPrimitive?.content
        val subject = raw["subject"]?.jsonPrimitive?.content
        val body = raw["contents"]?.jsonPrimitive?.content
            ?: raw["body"]?.jsonPrimitive?.content
            ?: raw["content"]?.jsonPrimitive?.content
        val read = raw.seqtaTruthyField("read")
        val starred = raw.seqtaTruthyField("starred")
        val date = parseDate(raw["date"]?.jsonPrimitive?.content)
        val participants = raw["participants"]?.jsonArray?.mapNotNull { element ->
            val dict = element.jsonObject
            val participantId = dict.intField("id") ?: return@mapNotNull null
            TeachMessageParticipant(
                id = participantId,
                name = dict["name"]?.jsonPrimitive?.content ?: "Unknown",
                type = dict["type"]?.jsonPrimitive?.content ?: "",
                read = dict.seqtaTruthyField("read"),
            )
        } ?: emptyList()
        val files = raw["files"]?.jsonArray?.mapNotNull { element ->
            val dict = element.jsonObject
            val fileId = dict.intField("id") ?: return@mapNotNull null
            val filename = dict["filename"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val uuid = dict["uuid"]?.jsonPrimitive?.content
                ?: dict["context_uuid"]?.jsonPrimitive?.content
                ?: return@mapNotNull null
            TeachMessageFile(
                id = fileId,
                filename = filename,
                mimetype = dict["mimetype"]?.jsonPrimitive?.content ?: "",
                size = dict["size"]?.jsonPrimitive?.content ?: "0",
                uuid = uuid,
            )
        } ?: emptyList()
        return TeachMessageDetail(
            id = id,
            sender = sender,
            senderId = senderId,
            senderType = senderType,
            subject = subject,
            body = body,
            date = date,
            read = read,
            starred = starred,
            participants = participants,
            files = files,
        )
    }

    private fun parseDate(dateStr: String?): Instant? {
        if (dateStr.isNullOrBlank()) return null
        runCatching { return Instant.parse(dateStr) }
        val patterns = listOf(
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSXXXXX"),
        )
        for (formatter in patterns) {
            val parsed = runCatching {
                LocalDateTime.parse(dateStr, formatter).atZone(ZoneId.systemDefault()).toInstant()
            }.getOrNull()
            if (parsed != null) return parsed
        }
        return null
    }
}
