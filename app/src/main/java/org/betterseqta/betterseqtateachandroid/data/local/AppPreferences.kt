package org.betterseqta.betterseqtateachandroid.data.local

import kotlinx.coroutines.flow.Flow

interface AppPreferences {
    val biometricRequired: Flow<Boolean>
    val attendanceViewMode: Flow<String>

    suspend fun setBiometricRequired(enabled: Boolean)
    suspend fun setAttendanceViewMode(mode: String)
}
