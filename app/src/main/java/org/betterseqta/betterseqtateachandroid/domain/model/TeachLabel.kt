package org.betterseqta.betterseqtateachandroid.domain.model

data class TeachLabel(
    val label: String,
    val unread: Int,
) {
    val id: String get() = label
}
