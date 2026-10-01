package org.betterseqta.betterseqtateachandroid.domain.model

import java.time.Instant

data class TeachMessageListParticipant(
    val name: String,
    val type: String,
)

data class TeachMessage(
    val id: String,
    val messageId: Int?,
    val sender: String?,
    val subject: String?,
    val body: String?,
    val date: Instant?,
    val read: Boolean,
    val starred: Boolean,
    val attachments: Boolean,
    val participants: List<TeachMessageListParticipant> = emptyList(),
)
