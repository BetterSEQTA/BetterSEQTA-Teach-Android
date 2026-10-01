package org.betterseqta.betterseqtateachandroid.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tag
import androidx.compose.ui.graphics.vector.ImageVector

object AttendanceIconHelper {
    private val codeToIcon: Map<String, ImageVector> = mapOf(
        "yes" to Icons.Filled.CheckCircle,
        "no" to Icons.Filled.Close,
        "present" to Icons.Filled.CheckCircle,
        "in_class" to Icons.Filled.CheckCircle,
        "absent" to Icons.Filled.Close,
        "alternate" to Icons.Filled.SwapHoriz,
        "absenceapproved" to Icons.Filled.Check,
        "camp" to Icons.Filled.Home,
        "educationalactivity" to Icons.Filled.Book,
        "educationaloffcampus" to Icons.Filled.Business,
        "excursion" to Icons.Filled.Business,
        "exempted" to Icons.Filled.Person,
        "kiosk" to Icons.Filled.Tag,
        "late" to Icons.Filled.Schedule,
        "latetoclass" to Icons.Filled.Schedule,
        "learningathome" to Icons.Filled.Home,
        "medical" to Icons.Filled.LocalHospital,
        "music" to Icons.Filled.MusicNote,
        "notapplicable" to Icons.Filled.Remove,
        "parentcontact" to Icons.Filled.Phone,
        "resolvedabsence" to Icons.Filled.CheckCircle,
        "reset" to Icons.Filled.ArrowBack,
        "sickbay" to Icons.Filled.LocalHospital,
        "staffadvice" to Icons.Filled.Info,
        "suspendedexternal" to Icons.Filled.Lock,
        "suspended" to Icons.Filled.Lock,
        "truant" to Icons.Filled.Close,
        "tutor" to Icons.Filled.Person,
        "absenceunapproved" to Icons.Filled.Close,
        "unresolvedabsence" to Icons.AutoMirrored.Filled.Help,
        "unresolvedlate" to Icons.Filled.Schedule,
        "withdrawn" to Icons.Filled.Person,
        "kiosk-zero" to Icons.Filled.Tag,
    )

    fun iconFor(code: String): ImageVector =
        codeToIcon[code.lowercase()] ?: Icons.AutoMirrored.Filled.Help
}
