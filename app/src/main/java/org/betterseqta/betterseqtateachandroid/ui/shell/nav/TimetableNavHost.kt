package org.betterseqta.betterseqtateachandroid.ui.shell.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import org.betterseqta.betterseqtateachandroid.navigation.AttendanceRoutes
import org.betterseqta.betterseqtateachandroid.navigation.TimetableRoutes
import org.betterseqta.betterseqtateachandroid.ui.attendance.lessonAttendanceDestination
import org.betterseqta.betterseqtateachandroid.ui.motion.composableWithAppMotion
import org.betterseqta.betterseqtateachandroid.ui.timetable.TimetableScreen

@Composable
fun TimetableNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TimetableRoutes.Timetable,
        modifier = modifier,
        route = TimetableRoutes.Graph,
    ) {
        composableWithAppMotion(TimetableRoutes.Timetable) {
            TimetableScreen(
                onLessonClick = { lesson, date ->
                    navController.navigate(AttendanceRoutes.lesson(lesson, date)) {
                        launchSingleTop = true
                    }
                },
            )
        }
        lessonAttendanceDestination(navController)
    }
}

@Composable
fun rememberTimetableNavController(): NavHostController = rememberNavController()
