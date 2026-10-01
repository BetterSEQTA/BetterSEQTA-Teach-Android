package org.betterseqta.betterseqtateachandroid.domain.model

enum class AttendanceViewMode(val storageValue: String) {
    List("List"),
    Card("Cards"),
    ;

    companion object {
        fun fromStorage(value: String?): AttendanceViewMode =
            entries.firstOrNull { it.storageValue == value } ?: List
    }
}

const val ATTENDANCE_VIEW_MODE_KEY = "attendanceViewMode"
