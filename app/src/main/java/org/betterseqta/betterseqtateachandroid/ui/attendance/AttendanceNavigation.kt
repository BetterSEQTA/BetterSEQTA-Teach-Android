package org.betterseqta.betterseqtateachandroid.ui.attendance

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import org.betterseqta.betterseqtateachandroid.ui.motion.composableWithAppMotion
import androidx.navigation.navArgument
import org.betterseqta.betterseqtateachandroid.navigation.AttendanceRoutes
import org.betterseqta.betterseqtateachandroid.navigation.LessonAttendanceNavCodec

fun NavGraphBuilder.lessonAttendanceDestination(
    navController: NavHostController,
) {
    composableWithAppMotion(
        route = AttendanceRoutes.Lesson,
        arguments = listOf(
            navArgument(AttendanceRoutes.PayloadArg) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val payload = backStackEntry.arguments?.getString(AttendanceRoutes.PayloadArg)
        if (payload == null || LessonAttendanceNavCodec.decode(payload) == null) {
            navController.popBackStack()
            return@composableWithAppMotion
        }
        LessonAttendanceScreen(
            onBack = { navController.popBackStack() },
            onOpenStats = { navController.navigate(AttendanceRoutes.statsRoute(payload)) },
            viewModel = hiltViewModel(backStackEntry),
        )
    }
    composableWithAppMotion(
        route = AttendanceRoutes.Stats,
        arguments = listOf(
            navArgument(AttendanceRoutes.PayloadArg) { type = NavType.StringType },
        ),
    ) { statsEntry ->
        val payload = statsEntry.arguments?.getString(AttendanceRoutes.PayloadArg) ?: return@composableWithAppMotion
        val parentEntry = navController.getBackStackEntry(AttendanceRoutes.lessonRouteFromPayload(payload))
        AttendanceStatsScreen(
            onBack = { navController.popBackStack() },
            viewModel = hiltViewModel(parentEntry),
        )
    }
}
