package org.betterseqta.betterseqtateachandroid.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.betterseqta.betterseqtateachandroid.domain.model.AttendanceSummary
import org.betterseqta.betterseqtateachandroid.domain.model.TeachAttendanceStudent
import org.betterseqta.betterseqtateachandroid.domain.model.TeachAttendanceType
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeachAttendanceClient @Inject constructor(
    private val seqtaApi: SeqtaApi,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchAttendanceTypes(session: TeachSession): List<TeachAttendanceType> {
        val response = seqtaApi.post(session, "/seqta/ta/attendance/types", buildJsonObject { })
        if (response.statusCode !in 200..299) throw SeqtaApiException.InvalidResponse()
        val root = json.parseToJsonElement(response.body).jsonObject
        val payload = root["payload"]?.jsonArray ?: return emptyList()
        return payload.mapNotNull { parseAttendanceType(it.jsonObject) }
    }

    suspend fun fetchAttendanceLoad(
        session: TeachSession,
        date: String,
        classunitIds: List<Int>,
        isAdhoc: Boolean,
    ): JsonObject {
        val body = buildJsonObject {
            put("mode", "normal")
            put("date", date)
            put("classes", buildJsonArray { classunitIds.forEach { add(JsonPrimitive(it)) } })
            put("current", isAdhoc)
        }
        val response = seqtaApi.post(session, "/seqta/ta/attendance/load", body)
        if (response.statusCode !in 200..299) throw SeqtaApiException.InvalidResponse()
        val root = json.parseToJsonElement(response.body).jsonObject
        return root["payload"]?.jsonObject ?: buildJsonObject { }
    }

    suspend fun saveAttendance(
        session: TeachSession,
        attendance: Map<String, Map<String, String>>,
    ) {
        val attendanceJson = buildJsonObject {
            attendance.forEach { (classInstanceId, students) ->
                put(classInstanceId, buildJsonObject {
                    students.forEach { (studentId, code) -> put(studentId, code) }
                })
            }
        }
        val body = buildJsonObject { put("attendance", attendanceJson) }
        val response = seqtaApi.post(session, "/seqta/ta/attendance/save", body)
        if (response.statusCode !in 200..299) throw SeqtaApiException.InvalidResponse()
    }

    suspend fun fetchAttendanceSummary(
        session: TeachSession,
        date: String,
        studentIds: List<Int>,
        classunitIds: List<Int>,
        isAdhoc: Boolean,
    ): Map<Int, AttendanceSummary> {
        val body = buildJsonObject {
            put("date", date)
            put("students", buildJsonArray { studentIds.forEach { add(JsonPrimitive(it)) } })
            put("classes", buildJsonArray { classunitIds.forEach { add(JsonPrimitive(it)) } })
            put("mode", if (isAdhoc) "inclass" else "inclass-count")
        }
        val response = seqtaApi.post(session, "/seqta/ta/json/attendance/summary", body)
        if (response.statusCode !in 200..299) throw SeqtaApiException.InvalidResponse()
        val root = json.parseToJsonElement(response.body).jsonObject
        val payload = root["payload"]?.jsonObject ?: return emptyMap()
        val studentsRaw = payload["students"]?.jsonArray ?: return emptyMap()
        val result = mutableMapOf<Int, AttendanceSummary>()
        for (itemElement in studentsRaw) {
            val item = itemElement.jsonObject
            val studentId = item["student"]?.jsonPrimitive?.int ?: continue
            val att = item["attendance"]?.jsonObject
            val present = att?.get("present")?.jsonPrimitive?.int ?: 0
            val percent = att?.get("percent")?.jsonPrimitive?.int ?: 0
            result[studentId] = AttendanceSummary(present = present, percent = percent)
        }
        return result
    }

    fun parseStudents(payload: JsonObject): List<TeachAttendanceStudent> {
        val raw = payload["students"]?.jsonArray ?: return emptyList()
        return raw.mapNotNull { parseStudent(it.jsonObject) }
    }

    private fun parseAttendanceType(raw: JsonObject): TeachAttendanceType? {
        val id = raw["id"]?.jsonPrimitive?.int ?: return null
        val code = raw["code"]?.jsonPrimitive?.content ?: ""
        val label = raw["label"]?.jsonPrimitive?.content ?: ""
        val icon = raw["icon"]?.jsonPrimitive?.content
        val consideredPresent = raw["considered_present"]?.jsonPrimitive?.let { prim ->
            when {
                prim.isString -> prim.content == "true"
                else -> runCatching { prim.int == 1 }.getOrDefault(false)
            }
        }
        val isReset = when {
            raw["is_reset"]?.jsonPrimitive?.int == 1 -> true
            raw["is_reset"]?.jsonPrimitive?.content == "true" -> true
            else -> false
        }
        val kioskEnabled = when {
            raw["kioskEnabled"]?.jsonPrimitive?.int == 1 -> true
            raw["kioskEnabled"]?.jsonPrimitive?.content == "true" -> true
            else -> false
        }
        val explanation = raw["explanation"]?.jsonPrimitive?.content
        return TeachAttendanceType(
            id = id,
            code = code,
            label = label,
            icon = icon,
            consideredPresent = consideredPresent,
            isReset = if (isReset) true else null,
            kioskEnabled = if (kioskEnabled) true else null,
            explanation = explanation,
        )
    }

    private fun parseStudent(raw: JsonObject): TeachAttendanceStudent? {
        val id = raw["id"]?.jsonPrimitive?.int ?: return null
        val firstname = raw["firstname"]?.jsonPrimitive?.content ?: ""
        val surname = raw["surname"]?.jsonPrimitive?.content ?: ""
        val prefname = raw["prefname"]?.jsonPrimitive?.content
        val code = raw["code"]?.jsonPrimitive?.content ?: ""
        val email = raw["email"]?.jsonPrimitive?.content
        val year = raw["year"]?.jsonPrimitive?.content
        val rollgroupname = raw["rollgroupname"]?.jsonPrimitive?.content
        val attendance = parseAttendanceMap(raw["attendance"]?.jsonObject)
        return TeachAttendanceStudent(
            id = id,
            firstname = firstname,
            surname = surname,
            prefname = prefname,
            code = code,
            email = email,
            year = year,
            rollgroupname = rollgroupname,
            attendance = attendance,
        )
    }

    private fun parseAttendanceMap(raw: JsonObject?): Map<String, Map<String, String>> {
        if (raw == null) return emptyMap()
        return raw.mapValues { (_, value) ->
            value.jsonObject.mapValues { (_, inner) ->
                runCatching { inner.jsonPrimitive.content }.getOrDefault("")
            }
        }
    }
}
