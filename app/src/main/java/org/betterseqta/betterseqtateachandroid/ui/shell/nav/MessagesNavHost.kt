package org.betterseqta.betterseqtateachandroid.ui.shell.nav

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import org.betterseqta.betterseqtateachandroid.ui.motion.LocalAnimatedVisibilityScope
import org.betterseqta.betterseqtateachandroid.ui.motion.LocalSharedTransitionScope
import org.betterseqta.betterseqtateachandroid.ui.motion.composableWithAppMotion
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import org.betterseqta.betterseqtateachandroid.domain.model.ComposeMode
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.navigation.MessagesRoutes
import org.betterseqta.betterseqtateachandroid.ui.messages.ComposeMessageScreen
import org.betterseqta.betterseqtateachandroid.ui.messages.DireqtMessagesScreen
import org.betterseqta.betterseqtateachandroid.ui.messages.MessageDetailScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MessagesNavHost(
    session: TeachSession?,
    staffId: Int?,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    SharedTransitionLayout(modifier = modifier) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this@SharedTransitionLayout) {
            NavHost(
                navController = navController,
                startDestination = MessagesRoutes.List,
                modifier = Modifier,
                route = MessagesRoutes.Graph,
            ) {
                composableWithAppMotion(MessagesRoutes.List) {
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        DireqtMessagesScreen(
                            session = session,
                            onMessageClick = { messageId ->
                                navController.navigate(MessagesRoutes.detail(messageId)) {
                                    launchSingleTop = true
                                }
                            },
                            onComposeClick = {
                                navController.navigate(MessagesRoutes.compose("new")) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                }
                composableWithAppMotion(
                    route = MessagesRoutes.Detail,
                    arguments = listOf(
                        navArgument("messageId") { type = NavType.IntType },
                    ),
                    deepLinks = listOf(
                        navDeepLink { uriPattern = "betterseqta://teach/messages/{messageId}" },
                    ),
                ) { backStackEntry ->
                    val messageId = backStackEntry.arguments?.getInt("messageId") ?: return@composableWithAppMotion
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        MessageDetailScreen(
                            messageId = messageId,
                            session = session,
                            onBack = { navController.popBackStack() },
                            onCompose = { mode, id ->
                                val modeKey = when (mode) {
                                    ComposeMode.New -> "new"
                                    ComposeMode.Reply -> "reply"
                                    ComposeMode.ReplyAll -> "reply_all"
                                    ComposeMode.Forward -> "forward"
                                }
                                navController.navigate(MessagesRoutes.compose(modeKey, id))
                            },
                            viewModel = hiltViewModel(backStackEntry),
                        )
                    }
                }
                composableWithAppMotion(
                    route = MessagesRoutes.Compose,
                    arguments = listOf(
                        navArgument("mode") { type = NavType.StringType },
                        navArgument("messageId") {
                            type = NavType.IntType
                            defaultValue = -1
                        },
                    ),
                ) { backStackEntry ->
                    val modeKey = backStackEntry.arguments?.getString("mode") ?: "new"
                    val rawMessageId = backStackEntry.arguments?.getInt("messageId") ?: -1
                    val messageId = if (rawMessageId >= 0) rawMessageId else null
                    val mode = when (modeKey) {
                        "reply" -> ComposeMode.Reply
                        "reply_all" -> ComposeMode.ReplyAll
                        "forward" -> ComposeMode.Forward
                        else -> ComposeMode.New
                    }
                    ComposeMessageScreen(
                        mode = mode,
                        messageId = messageId,
                        session = session,
                        selfStaffId = staffId,
                        onDismiss = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

@Composable
fun rememberMessagesNavController(): NavHostController = rememberNavController()
