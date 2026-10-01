package org.betterseqta.betterseqtateachandroid.ui.attendance

import androidx.compose.ui.graphics.Color

fun attendanceStatusColor(label: String): Color {
    val lower = label.lowercase()
    if (lower.contains("absent") || lower.contains("no") || lower.contains("truant") ||
        lower.contains("unapproved") || lower.contains("unresolved")
    ) {
        return Color(0xFFC62828)
    }
    if (lower.contains("present") || lower.contains("in-class") || lower.contains("yes") ||
        lower.contains("approved") || lower.contains("resolved") || lower.contains("exempted")
    ) {
        return Color(0xFF2E7D32)
    }
    if (lower.contains("late")) return Color(0xFFEF6C00)
    if (lower.contains("learning at home") || lower.contains("medical")) return Color(0xFF1565C0)
    return Color(0xFF616161)
}
