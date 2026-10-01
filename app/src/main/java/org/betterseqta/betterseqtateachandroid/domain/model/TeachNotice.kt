package org.betterseqta.betterseqtateachandroid.domain.model

data class TeachNotice(
    val id: Int,
    val labelTitle: String?,
    val staff: String?,
    val title: String,
    val colour: String?,
    val contents: String?,
    val from: String?,
    val until: String?,
    val createdDate: String?,
)

data class TeachNoticeLabel(
    val id: Int,
    val title: String,
    val colour: String?,
)
