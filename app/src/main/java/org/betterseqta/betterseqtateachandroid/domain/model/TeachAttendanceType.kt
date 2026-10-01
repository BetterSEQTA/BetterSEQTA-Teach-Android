package org.betterseqta.betterseqtateachandroid.domain.model

data class TeachAttendanceType(
    val id: Int,
    val code: String,
    val label: String,
    val icon: String? = null,
    val consideredPresent: Boolean? = null,
    val isReset: Boolean? = null,
    val kioskEnabled: Boolean? = null,
    val explanation: String? = null,
)
