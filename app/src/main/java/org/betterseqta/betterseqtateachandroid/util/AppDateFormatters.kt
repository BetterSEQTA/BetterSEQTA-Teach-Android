package org.betterseqta.betterseqtateachandroid.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object AppDateFormatters {
    private val isoYmd: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    private val displayDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

    fun todayIso(): String = LocalDate.now().format(isoYmd)

    fun isoFromLocalDate(date: LocalDate): String = date.format(isoYmd)

    fun displayDate(date: LocalDate): String = date.format(displayDateFormatter)

    fun formatIsoDate(date: LocalDate): String = isoFromLocalDate(date)

    fun localDateFromIso(value: String): LocalDate? =
        runCatching { LocalDate.parse(value, isoYmd) }.getOrNull()

    fun lessonTitleDateTime(
        dateIso: String,
        lessonFrom: String,
        lessonUntil: String,
        subject: String?,
    ): String {
        val dateStr = localDateFromIso(dateIso)?.format(displayDateFormatter) ?: dateIso
        val dateTime = "$dateStr, $lessonFrom – $lessonUntil"
        return if (!subject.isNullOrBlank()) "$subject · $dateTime" else dateTime
    }

    fun formatMessageDate(instant: Instant?): String {
        if (instant == null) return ""
        val zoned = instant.atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.getDefault())
        return formatter.format(zoned)
    }

    fun relativeMessageDate(instant: Instant?): String {
        if (instant == null) return ""
        val now = Instant.now()
        val seconds = (now.epochSecond - instant.epochSecond).coerceAtLeast(0)
        return when {
            seconds < 60 -> "Just now"
            seconds < 3600 -> "${seconds / 60}m ago"
            seconds < 86_400 -> "${seconds / 3600}h ago"
            seconds < 604_800 -> "${seconds / 86_400}d ago"
            else -> formatMessageDate(instant)
        }
    }
}
