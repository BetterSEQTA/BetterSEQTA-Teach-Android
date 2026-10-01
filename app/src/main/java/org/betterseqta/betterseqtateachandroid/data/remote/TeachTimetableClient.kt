package org.betterseqta.betterseqtateachandroid.data.remote

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.betterseqta.betterseqtateachandroid.domain.model.TeachLesson
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.util.intField
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeachTimetableClient @Inject constructor(
    private val seqtaApi: SeqtaApi,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchLessons(
        session: TeachSession,
        staffId: Int,
        dateFrom: String,
        dateTo: String,
    ): List<TeachLesson> = coroutineScope {
        fetchTerms(session)
        val timetableDeferred = async { fetchTimetabled(session, staffId, dateFrom, dateTo) }
        val adhocDeferred = async { fetchAdhoc(session, staffId, dateFrom, dateTo) }
        processTimetableData(
            timetablePayload = timetableDeferred.await(),
            adhocPayload = adhocDeferred.await(),
            dateFrom = dateFrom,
            dateTo = dateTo,
        )
    }

    private suspend fun fetchTerms(session: TeachSession) {
        runCatching {
            val body = buildJsonObject {
                put("request", "terms")
                put("asArray", true)
            }
            val response = seqtaApi.post(session, "/seqta/ta/json/get", body)
            if (response.statusCode !in 200..299) {
                throw SeqtaApiException.InvalidResponse()
            }
        }
    }

    private suspend fun fetchTimetabled(
        session: TeachSession,
        staffId: Int,
        dateFrom: String,
        dateTo: String,
    ): JsonObject {
        val body = buildJsonObject {
            put("timetabled", true)
            put("untimetabled", true)
            put("dateFrom", dateFrom)
            put("dateTo", dateTo)
            put("staff", staffId)
        }
        val response = seqtaApi.post(session, "/seqta/ta/json/timetable/get", body)
        if (response.statusCode !in 200..299) return buildJsonObject { }
        val root = json.parseToJsonElement(response.body).jsonObject
        return root["payload"]?.jsonObject ?: buildJsonObject { }
    }

    private suspend fun fetchAdhoc(
        session: TeachSession,
        staffId: Int,
        dateFrom: String,
        dateTo: String,
    ): JsonObject {
        val body = buildJsonObject {
            put("dateFrom", dateFrom)
            put("dateTo", dateTo)
            put("staff", staffId)
        }
        val response = seqtaApi.post(session, "/seqta/ta/json/timetable/adhoc/get", body)
        if (response.statusCode !in 200..299) return buildJsonObject { }
        val root = json.parseToJsonElement(response.body).jsonObject
        return root["payload"]?.jsonObject ?: buildJsonObject { }
    }

    private fun processTimetableData(
        timetablePayload: JsonObject,
        adhocPayload: JsonObject,
        dateFrom: String,
        dateTo: String,
    ): List<TeachLesson> {
        val lessons = mutableListOf<TeachLesson>()
        val adhocClassUnits = buildAdhocClassUnitLookup(adhocPayload)

        val timetabled = timetablePayload["timetabled"]?.jsonObject
        val periodsElement = timetabled?.get("periods")
        when (periodsElement) {
            is JsonArray -> {
                periodsElement.forEach { periodElement ->
                    collectLessonsForDateMap(
                        period = periodElement.jsonObject,
                        dateFrom = dateFrom,
                        dateTo = dateTo,
                        lessons = lessons,
                        isAdhoc = false,
                        adhocClassUnits = null,
                    )
                }
            }
            is JsonObject -> {
                collectLessonsForDateMap(
                    period = periodsElement,
                    dateFrom = dateFrom,
                    dateTo = dateTo,
                    lessons = lessons,
                    isAdhoc = false,
                    adhocClassUnits = null,
                )
            }
            else -> Unit
        }

        val adhocList = adhocPayload["adhoc"]?.jsonArray
        adhocList?.forEachIndexed { index, adhocElement ->
            val adhoc = adhocElement.jsonObject
            val dateStr = adhoc["date"]?.jsonPrimitive?.content ?: return@forEachIndexed
            if (dateStr < dateFrom || dateStr > dateTo) return@forEachIndexed
            parseLesson(
                raw = adhoc,
                index = index,
                isAdhoc = true,
                adhocClassUnits = adhocClassUnits,
            )?.let { lessons.add(it) }
        }

        return dedupeAndSort(lessons)
    }

    private data class AdhocClassUnitInfo(
        val description: String?,
        val code: String?,
        val staff: String?,
    )

    private fun buildAdhocClassUnitLookup(adhocPayload: JsonObject): Map<Int, AdhocClassUnitInfo> {
        val units = adhocPayload["adhoc_classunits"]?.jsonArray ?: return emptyMap()
        return units.mapNotNull { unitElement ->
            val unit = unitElement.jsonObject
            val id = unit["id"]?.jsonPrimitive?.int ?: return@mapNotNull null
            val description = unit["description"]?.jsonPrimitive?.content
            val code = unit["code"]?.jsonPrimitive?.content
                ?: unit["name"]?.jsonPrimitive?.content
            val staff = unit["staff"]?.jsonPrimitive?.content
            id to AdhocClassUnitInfo(description, code, staff)
        }.toMap()
    }

    private fun parseLesson(
        raw: JsonObject,
        index: Int,
        isAdhoc: Boolean,
        adhocClassUnits: Map<Int, AdhocClassUnitInfo>?,
    ): TeachLesson? {
        val from = raw["from"]?.jsonPrimitive?.content?.take(5) ?: ""
        val until = raw["until"]?.jsonPrimitive?.content?.take(5) ?: ""
        val id = raw["id"]?.jsonPrimitive?.int?.toString()
            ?: "$index-$from-$until"
        val programmeId = raw["programmeID"]?.jsonPrimitive?.int
            ?: raw["programme"]?.jsonPrimitive?.int
        val metaId = raw["metaID"]?.jsonPrimitive?.int
            ?: raw["metaclass"]?.jsonPrimitive?.int

        val classunitId = raw.intField("classunit", "classUnit", "classunit_id")
        var description: String? = null
        var code: String? = null
        var staff: String? = null

        if (isAdhoc && classunitId != null && adhocClassUnits != null) {
            adhocClassUnits[classunitId]?.let { info ->
                description = info.description
                code = info.code
                staff = info.staff
            }
        }
        description = description ?: raw["description"]?.jsonPrimitive?.content
        code = code ?: raw["code"]?.jsonPrimitive?.content
        if (staff == null) {
            staff = raw["staff"]?.jsonPrimitive?.contentOrNull
                ?: raw.intField("staff")?.toString()
        }
        val room = raw["room_code"]?.jsonPrimitive?.content
            ?: raw["room"]?.jsonPrimitive?.contentOrNull
        val period = raw["period"]?.jsonPrimitive?.content
        val classunit = classunitId?.toString() ?: raw["classunit"]?.jsonPrimitive?.contentOrNull

        return TeachLesson(
            id = id,
            from = from,
            until = until,
            period = period,
            description = description,
            code = code,
            staff = staff,
            room = room,
            classunit = classunit,
            classunitId = classunitId,
            programmeId = programmeId,
            metaId = metaId,
            isAdhoc = isAdhoc,
        )
    }

    private fun collectLessonsForDateMap(
        period: JsonObject,
        dateFrom: String,
        dateTo: String,
        lessons: MutableList<TeachLesson>,
        isAdhoc: Boolean,
        adhocClassUnits: Map<Int, AdhocClassUnitInfo>?,
    ) {
        for ((key, value) in period) {
            if (!key.matches(ISO_DATE_REGEX)) continue
            if (key < dateFrom || key > dateTo) continue
            val lessonArray = value.jsonArray
            lessonArray.forEachIndexed { index, lessonElement ->
                parseLesson(
                    raw = lessonElement.jsonObject,
                    index = index,
                    isAdhoc = isAdhoc,
                    adhocClassUnits = adhocClassUnits,
                )?.let { lessons.add(it) }
            }
        }
    }

    private fun dedupeAndSort(lessons: List<TeachLesson>): List<TeachLesson> {
        val seen = mutableSetOf<String>()
        val unique = mutableListOf<TeachLesson>()
        for (lesson in lessons) {
            val key = "${lesson.id}-${lesson.from}-${lesson.until}"
            if (seen.add(key)) {
                unique.add(lesson)
            }
        }
        return unique.sortedBy { it.from }
    }

    private val JsonElement.contentOrNull: String?
        get() = runCatching { jsonPrimitive.content }.getOrNull()

    companion object {
        private val ISO_DATE_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    }
}
