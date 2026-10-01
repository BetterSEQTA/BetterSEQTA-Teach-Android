package org.betterseqta.betterseqtateachandroid.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.betterseqta.betterseqtateachandroid.domain.model.ATTENDANCE_VIEW_MODE_KEY
import org.betterseqta.betterseqtateachandroid.domain.model.AttendanceViewMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendancePreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getViewMode(): AttendanceViewMode =
        AttendanceViewMode.fromStorage(prefs.getString(ATTENDANCE_VIEW_MODE_KEY, null))

    fun setViewMode(mode: AttendanceViewMode) {
        prefs.edit().putString(ATTENDANCE_VIEW_MODE_KEY, mode.storageValue).apply()
    }

    companion object {
        private const val PREFS_NAME = "teach_prefs"
    }
}
