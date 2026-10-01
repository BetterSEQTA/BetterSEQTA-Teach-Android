package org.betterseqta.betterseqtateachandroid.data.remote

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

internal object SeqtaDateParser {
    private val legacyFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd HH:mm:ss")
        .optionalStart()
        .appendPattern(".SSS")
        .optionalEnd()
        .appendOffset("+HH:mm", "XXXXX")
        .toFormatter(Locale.US)

    fun parse(dateStr: String?): Instant? {
        if (dateStr.isNullOrBlank()) return null
        return runCatching { Instant.parse(dateStr) }.getOrNull()
            ?: runCatching {
                LocalDateTime.parse(dateStr, legacyFormatter).toInstant(ZoneOffset.UTC)
            }.getOrNull()
    }
}
