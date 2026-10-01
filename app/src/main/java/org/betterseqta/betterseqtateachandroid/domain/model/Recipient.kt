package org.betterseqta.betterseqtateachandroid.domain.model

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

enum class RecipientType(val apiKey: String, val displayName: String) {
    Staff("staff", "Staff"),
    Student("student", "Student"),
    Tutor("tutor", "Tutor"),
    Contact("contact", "Parents"),
}

data class Recipient(
    val id: Int,
    val displayName: String,
    val type: RecipientType,
) {
    fun toParticipantJson(): JsonObject = buildJsonObject {
        when (type) {
            RecipientType.Staff -> put("staff", true)
            RecipientType.Student -> put("student", true)
            RecipientType.Tutor -> put("tutor", true)
            RecipientType.Contact -> put("student", true)
        }
        put("id", id)
    }
}
