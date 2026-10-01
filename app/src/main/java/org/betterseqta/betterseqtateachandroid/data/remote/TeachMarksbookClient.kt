package org.betterseqta.betterseqtateachandroid.data.remote

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMarksbookSnapshot
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.util.MarkScoreChange
import org.betterseqta.betterseqtateachandroid.util.buildMarksbookSavePayload
import org.betterseqta.betterseqtateachandroid.util.parseMarksbookPayload

@Singleton
class TeachMarksbookClient @Inject constructor(
    private val seqtaApi: SeqtaApi,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadMarksbook(
        session: TeachSession,
        programId: Int,
        metaClassId: Int,
        dateIso: String,
    ): TeachMarksbookSnapshot {
        val body = buildJsonObject {
            putJsonArray("classes") { add(metaClassId) }
            put("date", dateIso)
            put("program", programId)
        }
        val response = seqtaApi.post(session, "/seqta/ta/json/marksbook/load", body)
        if (response.statusCode !in 200..299) {
            error("Marksbook load failed (${response.statusCode})")
        }
        val root = json.parseToJsonElement(response.body).jsonObject
        val payload = root["payload"]?.jsonObject
            ?: error("Marksbook response missing payload")
        return parseMarksbookPayload(payload, programId, metaClassId)
    }

    suspend fun saveMarks(
        session: TeachSession,
        changes: List<MarkScoreChange>,
        isNumeric: Int,
    ) {
        if (changes.isEmpty()) return
        val body = buildMarksbookSavePayload(changes, isNumeric)
        val response = seqtaApi.post(session, "/seqta/ta/json/marksbook/save", body)
        if (response.statusCode !in 200..299) {
            error("Marksbook save failed (${response.statusCode})")
        }
        val root = json.parseToJsonElement(response.body).jsonObject
        val status = root["status"]?.jsonPrimitive?.contentOrNull
        if (status != null && status != "200") {
            error("Marksbook save rejected")
        }
    }
}
