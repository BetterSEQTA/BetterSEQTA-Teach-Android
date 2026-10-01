package org.betterseqta.betterseqtateachandroid.domain.model

data class TeachAttendanceStudent(
    val id: Int,
    val firstname: String,
    val surname: String,
    val prefname: String? = null,
    val code: String,
    val email: String? = null,
    val year: String? = null,
    val rollgroupname: String? = null,
    val attendance: Map<String, Map<String, String>> = emptyMap(),
)
