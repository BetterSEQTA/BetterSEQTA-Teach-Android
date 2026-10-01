package org.betterseqta.betterseqtateachandroid.ui.shell.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import org.betterseqta.betterseqtateachandroid.ui.motion.composableWithAppMotion
import androidx.navigation.navArgument
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.navigation.NoticesRoutes
import org.betterseqta.betterseqtateachandroid.ui.notices.NoticeDetailScreen
import org.betterseqta.betterseqtateachandroid.ui.notices.NoticesScreen
import org.betterseqta.betterseqtateachandroid.ui.notices.NoticesViewModel

@Composable
fun NoticesNavHost(
    session: TeachSession?,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = NoticesRoutes.Notices,
        modifier = modifier,
        route = NoticesRoutes.Graph,
    ) {
        composableWithAppMotion(NoticesRoutes.Notices) { entry ->
            val listViewModel: NoticesViewModel = hiltViewModel(entry)
            NoticesScreen(
                session = session,
                onNoticeClick = { noticeId ->
                    navController.navigate(NoticesRoutes.detail(noticeId)) {
                        launchSingleTop = true
                    }
                },
                viewModel = listViewModel,
            )
        }
        composableWithAppMotion(
            route = NoticesRoutes.Detail,
            arguments = listOf(navArgument("noticeId") { type = NavType.IntType }),
        ) { entry ->
            val noticeId = entry.arguments?.getInt("noticeId") ?: return@composableWithAppMotion
            val listViewModel: NoticesViewModel = hiltViewModel(
                navController.getBackStackEntry(NoticesRoutes.Notices),
            )
            val notice = listViewModel.noticeById(noticeId)
            if (notice != null) {
                NoticeDetailScreen(
                    notice = notice,
                    onBack = { navController.popBackStack() },
                )
            } else {
                androidx.compose.material3.Text(
                    text = "Notice not found",
                    modifier = Modifier.padding(24.dp),
                )
            }
        }
    }
}

@Composable
fun rememberNoticesNavController(): NavHostController = rememberNavController()
