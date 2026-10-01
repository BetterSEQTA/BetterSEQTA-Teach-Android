package org.betterseqta.betterseqtateachandroid.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import org.betterseqta.betterseqtateachandroid.R

enum class AppTab(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home(R.string.tab_home, Icons.Outlined.Home),
    Timetable(R.string.tab_timetable, Icons.Outlined.CalendarMonth),
    Notices(R.string.tab_notices, Icons.Outlined.Description),
    Messages(R.string.tab_messages, Icons.AutoMirrored.Outlined.Chat),
    Settings(R.string.tab_settings, Icons.Outlined.Settings),
}
