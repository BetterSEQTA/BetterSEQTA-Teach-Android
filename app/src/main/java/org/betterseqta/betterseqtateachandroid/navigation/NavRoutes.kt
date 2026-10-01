package org.betterseqta.betterseqtateachandroid.navigation

import org.betterseqta.betterseqtateachandroid.domain.model.TeachLesson

object HomeRoutes {
    const val Graph = "home_graph"
    const val Home = "home"
    const val Assessments = "home/assessments"
    const val Marksbook = "home/marksbook/{programId}/{metaClassId}/{assessmentId}"

    fun marksbook(programId: Int, metaClassId: Int, assessmentId: Int): String =
        "home/marksbook/$programId/$metaClassId/$assessmentId"
}

object TimetableRoutes {
    const val Graph = "timetable_graph"
    const val Timetable = "timetable"
}

object NoticesRoutes {
    const val Graph = "notices_graph"
    const val Notices = "notices"
    const val Detail = "notices/detail/{noticeId}"

    fun detail(noticeId: Int): String = "notices/detail/$noticeId"
}

object MessagesRoutes {
    const val Graph = "messages_graph"
    const val List = "messages/list"
    const val Detail = "messages/detail/{messageId}"
    const val Compose = "messages/compose/{mode}/{messageId}"

    fun detail(messageId: Int): String = "messages/detail/$messageId"

    fun compose(mode: String, messageId: Int = -1): String = "messages/compose/$mode/$messageId"
}

object SettingsRoutes {
    const val Graph = "settings_graph"
    const val Settings = "settings"
}

object AttendanceRoutes {
    const val Lesson = "lesson_attendance/{payload}"
    const val Stats = "lesson_attendance_stats/{payload}"
    const val PayloadArg = "payload"

    fun lesson(lesson: TeachLesson, date: String): String {
        val encoded = LessonAttendanceNavCodec.encode(lesson, date)
        return "lesson_attendance/$encoded"
    }

    fun statsRoute(encodedPayload: String): String = "lesson_attendance_stats/$encodedPayload"

    fun lessonRouteFromPayload(encodedPayload: String): String = "lesson_attendance/$encodedPayload"
}
