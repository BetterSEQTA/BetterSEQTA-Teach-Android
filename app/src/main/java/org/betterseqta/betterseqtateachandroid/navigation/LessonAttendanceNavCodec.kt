package org.betterseqta.betterseqtateachandroid.navigation

import android.net.Uri
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.betterseqta.betterseqtateachandroid.domain.model.TeachLesson

@Serializable
data class LessonAttendanceRouteArgs(
    val lesson: TeachLesson,
    val date: String,
)

object LessonAttendanceNavCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(lesson: TeachLesson, date: String): String =
        Uri.encode(json.encodeToString(LessonAttendanceRouteArgs.serializer(), LessonAttendanceRouteArgs(lesson, date)))

    fun decode(payload: String): LessonAttendanceRouteArgs? =
        runCatching {
            json.decodeFromString(
                LessonAttendanceRouteArgs.serializer(),
                Uri.decode(payload),
            )
        }.getOrNull()
}
