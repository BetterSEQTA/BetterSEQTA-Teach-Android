package org.betterseqta.betterseqtateachandroid.ui.shell.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import org.betterseqta.betterseqtateachandroid.ui.motion.composableWithAppMotion
import androidx.navigation.compose.rememberNavController
import org.betterseqta.betterseqtateachandroid.navigation.AppTab
import org.betterseqta.betterseqtateachandroid.navigation.AttendanceRoutes
import org.betterseqta.betterseqtateachandroid.navigation.HomeRoutes
import org.betterseqta.betterseqtateachandroid.ui.attendance.lessonAttendanceDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import org.betterseqta.betterseqtateachandroid.ui.assessments.CourseAssessmentsScreen
import org.betterseqta.betterseqtateachandroid.domain.model.TeachAssessmentItem
import org.betterseqta.betterseqtateachandroid.ui.home.HomeScreen
import org.betterseqta.betterseqtateachandroid.ui.marksbook.MarksbookScreen
import org.betterseqta.betterseqtateachandroid.util.AppDateFormatters

@Composable
fun HomeNavHost(
    navController: NavHostController,
    onNavigateToTab: (AppTab) -> Unit,
    onMessageClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoutes.Home,
        modifier = modifier,
        route = HomeRoutes.Graph,
    ) {
        val openMarksbook: (TeachAssessmentItem) -> Unit = { item ->
            navController.navigate(
                HomeRoutes.marksbook(item.programId, item.metaClassId, item.id),
            ) {
                launchSingleTop = true
            }
        }
        composableWithAppMotion(HomeRoutes.Home) {
            HomeScreen(
                onNavigateToTab = onNavigateToTab,
                onOpenAssessments = {
                    navController.navigate(HomeRoutes.Assessments) {
                        launchSingleTop = true
                    }
                },
                onAssessmentClick = openMarksbook,
                onLessonClick = { lesson ->
                    navController.navigate(
                        AttendanceRoutes.lesson(lesson, AppDateFormatters.todayIso()),
                    ) {
                        launchSingleTop = true
                    }
                },
                onMessageClick = onMessageClick,
            )
        }
        composableWithAppMotion(HomeRoutes.Assessments) {
            CourseAssessmentsScreen(
                onBack = { navController.popBackStack() },
                onAssessmentClick = openMarksbook,
            )
        }
        composableWithAppMotion(
            route = HomeRoutes.Marksbook,
            arguments = listOf(
                navArgument("programId") { type = NavType.IntType },
                navArgument("metaClassId") { type = NavType.IntType },
                navArgument("assessmentId") { type = NavType.IntType },
            ),
        ) {
            MarksbookScreen(onBack = { navController.popBackStack() })
        }
        lessonAttendanceDestination(navController)
    }
}

@Composable
fun rememberHomeNavController(): NavHostController = rememberNavController()
