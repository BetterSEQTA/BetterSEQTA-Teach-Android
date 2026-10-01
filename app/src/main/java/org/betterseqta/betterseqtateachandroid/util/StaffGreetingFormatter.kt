package org.betterseqta.betterseqtateachandroid.util

private val STAFF_TITLES = listOf("Mr", "Ms", "Mrs", "Miss", "Dr", "Mx", "Prof")

/**
 * Builds a short greeting name from SEQTA `userDesc`, e.g. "Mr Teach Tester" → "Mr Teach".
 */
fun formatStaffGreetingName(rawDisplayName: String?): String? {
    if (rawDisplayName.isNullOrBlank()) return null
    val parts = rawDisplayName.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (parts.isEmpty()) return null

    val firstToken = parts.first().removeSuffix(".")
    val matchedTitle = STAFF_TITLES.firstOrNull { title ->
        title.equals(firstToken, ignoreCase = true)
    }

    if (matchedTitle != null) {
        val surname = parts.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return "$matchedTitle $surname"
    }

    return parts.first()
}
