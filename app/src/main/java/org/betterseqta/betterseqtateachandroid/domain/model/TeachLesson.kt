package org.betterseqta.betterseqtateachandroid.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TeachLesson(
    val id: String,
    val from: String,
    val until: String,
    val period: String? = null,
    val description: String? = null,
    val code: String? = null,
    val staff: String? = null,
    val room: String? = null,
    val classunit: String? = null,
    val classunitId: Int? = null,
    val programmeId: Int? = null,
    val metaId: Int? = null,
    val isAdhoc: Boolean = false,
)
