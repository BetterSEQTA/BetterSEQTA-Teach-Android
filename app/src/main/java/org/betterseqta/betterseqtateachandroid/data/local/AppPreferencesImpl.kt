package org.betterseqta.betterseqtateachandroid.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_preferences",
)

@Singleton
class AppPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppPreferences {

    private val biometricKey = booleanPreferencesKey("biometric_required")
    private val attendanceModeKey = stringPreferencesKey("attendance_view_mode")

    override val biometricRequired: Flow<Boolean> =
        context.appPrefsDataStore.data.map { prefs -> prefs[biometricKey] ?: false }

    override val attendanceViewMode: Flow<String> =
        context.appPrefsDataStore.data.map { prefs ->
            prefs[attendanceModeKey] ?: AttendanceViewMode.List.apiValue
        }

    override suspend fun setBiometricRequired(enabled: Boolean) {
        context.appPrefsDataStore.edit { prefs ->
            prefs[biometricKey] = enabled
        }
    }

    override suspend fun setAttendanceViewMode(mode: String) {
        context.appPrefsDataStore.edit { prefs ->
            prefs[attendanceModeKey] = mode
        }
    }
}

enum class AttendanceViewMode(val apiValue: String, val label: String) {
    List("list", "List"),
    Grid("grid", "Grid"),
    ;

    companion object {
        fun fromApi(value: String): AttendanceViewMode =
            entries.firstOrNull { it.apiValue == value } ?: List
    }
}
