package org.betterseqta.betterseqtateachandroid.domain.model

import java.time.Instant

data class TeachMessageParticipant(
    val id: Int,
    val name: String,
    val type: String,
    val read: Boolean,
)

data class TeachMessageFile(
    val id: Int,
    val filename: String,
    val mimetype: String,
    val size: String,
    val uuid: String,
)

data class TeachMessageDetail(
    val id: Int,
    val sender: String?,
    val senderId: Int?,
    val senderType: String?,
    val subject: String?,
    val body: String?,
    val date: Instant?,
    val read: Boolean,
    val starred: Boolean,
    val participants: List<TeachMessageParticipant>,
    val files: List<TeachMessageFile>,
)
