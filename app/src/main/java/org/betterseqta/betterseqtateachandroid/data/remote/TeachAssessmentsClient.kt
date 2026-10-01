package org.betterseqta.betterseqtateachandroid.data.remote

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.betterseqta.betterseqtateachandroid.domain.model.TeachAssessmentItem
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSubjectAssessmentGroup
import org.betterseqta.betterseqtateachandroid.util.AppDateFormatters
import org.betterseqta.betterseqtateachandroid.util.countAssessmentProgress
import org.betterseqta.betterseqtateachandroid.util.intField

@Singleton
class TeachAssessmentsClient @Inject constructor(
    private val seqtaApi: SeqtaApi,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val isoDate = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun fetchSubjectAssessmentGroups(
        session: TeachSession,
        staffId: Int,
        maxPrograms: Int = 16,
    ): List<TeachSubjectAssessmentGroup> {
        val termValue = fetchCurrentTermValue(session) ?: return emptyList()
        val programs = fetchPrograms(session, staffId, termValue).take(maxPrograms)
        if (programs.isEmpty()) return emptyList()

        val today = AppDateFormatters.todayIso()
        val todayDate = LocalDate.parse(today, isoDate)

        val items = coroutineScope {
            programs.map { program ->
                async {
                    loadAssessmentsForProgram(session, program, today, todayDate)
                }
            }.awaitAll().flatten()
        }

        return items
            .groupBy { it.subjectLabel.trim().ifEmpty { "Class" } }
            .map { (subject, subjectItems) ->
                val upcoming = subjectItems
                    .filter { !it.isPastDue }
                    .sortedBy { it.dueDate }
                val past = subjectItems
                    .filter { it.isPastDue }
                    .sortedByDescending { it.dueDate }
                TeachSubjectAssessmentGroup(
                    subjectLabel = subject,
                    programTitle = null,
                    upcoming = upcoming,
                    past = past,
                )
            }
            .filter { it.totalCount > 0 }
            .sortedBy { it.subjectLabel.lowercase() }
    }

    suspend fun countAssessmentsNeedingAttention(
        session: TeachSession,
        staffId: Int,
    ): Int {
        return fetchSubjectAssessmentGroups(session, staffId, maxPrograms = 8)
            .sumOf { group ->
                (group.upcoming + group.past).count { item ->
                    item.totalStudents > 0 && item.markedCount < item.totalStudents
                }
            }
    }

    private suspend fun fetchCurrentTermValue(session: TeachSession): Int? {
        val body = buildJsonObject {
            put("request", "terms")
            put("asArray", true)
        }
        val response = seqtaApi.post(session, "/seqta/ta/json/get", body)
        if (response.statusCode !in 200..299) return null
        val root = json.parseToJsonElement(response.body).jsonObject
        val terms = root["payload"]?.jsonArray ?: return null
        val today = LocalDate.now()
        for (element in terms) {
            val term = element.jsonObject
            val start = parseFlexibleDate(term["start"]?.jsonPrimitive?.contentOrNull) ?: continue
            val end = parseFlexibleDate(term["end"]?.jsonPrimitive?.contentOrNull) ?: continue
            if (!today.isBefore(start) && !today.isAfter(end)) {
                return term.intField("xx_value", "value", "id")
            }
        }
        return null
    }

    private suspend fun fetchPrograms(
        session: TeachSession,
        staffId: Int,
        termValue: Int,
    ): List<JsonObject> {
        val body = buildJsonObject {
            putJsonArray("search") { }
            putJsonArray("tags") { }
            putJsonArray("staff") { add(staffId) }
            putJsonArray("terms") { add(termValue) }
        }
        val response = seqtaApi.post(session, "/seqta/ta/json/program/list", body)
        if (response.statusCode !in 200..299) return emptyList()
        val root = json.parseToJsonElement(response.body).jsonObject
        return root["payload"]?.jsonArray?.map { it.jsonObject }.orEmpty()
    }

    private suspend fun loadAssessmentsForProgram(
        session: TeachSession,
        program: JsonObject,
        todayFormatted: String,
        todayDate: LocalDate,
    ): List<TeachAssessmentItem> {
        val programId = program.intField("id") ?: return emptyList()
        val metaId = program["meta"]?.jsonArray?.firstOrNull()?.jsonObject?.intField("id")
            ?: return emptyList()
        val subjectDesc = program["subjectDesc"]?.jsonPrimitive?.contentOrNull
            ?: program["title"]?.jsonPrimitive?.contentOrNull
            ?: "Class"

        val body = buildJsonObject {
            putJsonArray("classes") { add(metaId) }
            put("date", todayFormatted)
            put("program", programId)
        }
        val response = seqtaApi.post(session, "/seqta/ta/json/marksbook/load", body)
        if (response.statusCode !in 200..299) return emptyList()

        val payload = json.parseToJsonElement(response.body).jsonObject["payload"]?.jsonObject
            ?: return emptyList()
        val assessmentSets = payload["assessmentSets"]?.jsonArray ?: JsonArray(emptyList())
        val students = payload["students"]?.jsonArray ?: JsonArray(emptyList())
        val payloadSubject = payload["subjectDesc"]?.jsonPrimitive?.contentOrNull ?: subjectDesc

        val results = mutableListOf<TeachAssessmentItem>()
        for (setElement in assessmentSets) {
            val set = setElement.jsonObject
            val assessments = set["assessments"]?.jsonArray ?: continue
            for (assessmentElement in assessments) {
                val assessment = assessmentElement.jsonObject
                val assessmentId = assessment.intField("id") ?: continue
                val title = assessment["title"]?.jsonPrimitive?.contentOrNull ?: "Assessment"
                val classData = assessment["classes"]?.jsonObject?.get(metaId.toString())?.jsonObject
                    ?: assessment["classes"]?.jsonObject?.get(metaId.toString())?.jsonObject
                val dueRaw = classData?.get("d")?.jsonPrimitive?.contentOrNull ?: continue
                val dueDate = parseFlexibleDate(dueRaw) ?: continue

                val progress = countAssessmentProgress(assessment, metaId, students)
                results += TeachAssessmentItem(
                    id = assessmentId,
                    title = title,
                    subjectLabel = payloadSubject,
                    dueDate = dueDate,
                    programId = programId,
                    metaClassId = metaId,
                    submittedCount = progress.submittedCount,
                    totalStudents = progress.totalStudents,
                    markedCount = progress.markedCount,
                )
            }
        }
        return results
    }

    private fun parseFlexibleDate(raw: String?): LocalDate? {
        if (raw.isNullOrBlank()) return null
        runCatching { return LocalDate.parse(raw.take(10), isoDate) }
        runCatching {
            return LocalDate.parse(raw.substringBefore("T").take(10), isoDate)
        }
        return null
    }
}
