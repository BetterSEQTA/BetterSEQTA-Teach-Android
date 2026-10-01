package org.betterseqta.betterseqtateachandroid.ui.shell

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import org.betterseqta.betterseqtateachandroid.ui.motion.PressStyle
import org.betterseqta.betterseqtateachandroid.ui.motion.expressivePressScale
import org.betterseqta.betterseqtateachandroid.ui.motion.tabVisibilityMotion
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.betterseqta.betterseqtateachandroid.navigation.AppTab
import org.betterseqta.betterseqtateachandroid.navigation.DeepLinkNavigator
import org.betterseqta.betterseqtateachandroid.navigation.HomeRoutes
import org.betterseqta.betterseqtateachandroid.navigation.MessagesRoutes
import org.betterseqta.betterseqtateachandroid.navigation.NoticesRoutes
import org.betterseqta.betterseqtateachandroid.navigation.SettingsRoutes
import org.betterseqta.betterseqtateachandroid.navigation.TimetableRoutes
import org.betterseqta.betterseqtateachandroid.ui.shell.nav.HomeNavHost
import org.betterseqta.betterseqtateachandroid.ui.shell.nav.MessagesNavHost
import org.betterseqta.betterseqtateachandroid.ui.shell.nav.NoticesNavHost
import org.betterseqta.betterseqtateachandroid.ui.shell.nav.SettingsNavHost
import org.betterseqta.betterseqtateachandroid.ui.shell.nav.TimetableNavHost
import org.betterseqta.betterseqtateachandroid.ui.shell.nav.rememberHomeNavController
import org.betterseqta.betterseqtateachandroid.ui.shell.nav.rememberMessagesNavController
import org.betterseqta.betterseqtateachandroid.ui.shell.nav.rememberNoticesNavController
import org.betterseqta.betterseqtateachandroid.ui.shell.nav.rememberSettingsNavController
import org.betterseqta.betterseqtateachandroid.ui.shell.nav.rememberTimetableNavController

/**
 * Android counterpart to iOS `TabRootView`.
 */
@Composable
fun TabRootScreen(
    deepLinkNavigator: DeepLinkNavigator,
    session: TeachSession?,
    staffId: Int?,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(AppTab.Home) }

    val homeNavController = rememberHomeNavController()
    val timetableNavController = rememberTimetableNavController()
    val noticesNavController = rememberNoticesNavController()
    val messagesNavController = rememberMessagesNavController()
    val settingsNavController = rememberSettingsNavController()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val drawerActions = remember(scope, drawerState) {
        TeachDrawerActions {
            scope.launch { drawerState.open() }
        }
    }

    val pendingMessageId = deepLinkNavigator.pendingMessageId
    LaunchedEffect(pendingMessageId) {
        val messageId = pendingMessageId ?: return@LaunchedEffect
        selectedTab = AppTab.Messages
        delay(100)
        messagesNavController.navigate(MessagesRoutes.detail(messageId)) {
            launchSingleTop = true
        }
        deepLinkNavigator.consumePendingMessageId()
    }

    BackHandler(enabled = selectedTab == AppTab.Messages) {
        if (!messagesNavController.popBackStack()) {
            selectedTab = AppTab.Home
        }
    }

    BackHandler(enabled = selectedTab == AppTab.Notices) {
        if (!noticesNavController.popBackStack()) {
            selectedTab = AppTab.Home
        }
    }

    BackHandler(enabled = selectedTab == AppTab.Home) {
        if (!homeNavController.popBackStack()) {
            // stay on home root
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            TeachAppDrawerSheet(
                selectedTab = selectedTab,
                drawerState = drawerState,
                scope = scope,
                onNavigateHome = {
                    selectedTab = AppTab.Home
                    homeNavController.popBackStack(HomeRoutes.Home, inclusive = false)
                },
                onNavigateAssessments = {
                    selectedTab = AppTab.Home
                    homeNavController.navigate(HomeRoutes.Assessments) {
                        launchSingleTop = true
                    }
                },
                onNavigateTab = { tab -> selectedTab = tab },
            )
        },
    ) {
        CompositionLocalProvider(LocalTeachDrawerActions provides drawerActions) {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar {
                        AppTab.entries.forEach { tab ->
                            val tabInteraction = remember(tab) { MutableInteractionSource() }
                            NavigationBarItem(
                                modifier = Modifier.expressivePressScale(
                                    PressStyle.Standard,
                                    tabInteraction,
                                ),
                                interactionSource = tabInteraction,
                                selected = selectedTab == tab,
                                onClick = {
                                    if (selectedTab == tab) {
                                        popTabToRoot(
                                            tab,
                                            homeNavController,
                                            timetableNavController,
                                            noticesNavController,
                                            messagesNavController,
                                            settingsNavController,
                                        )
                                    } else {
                                        selectedTab = tab
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = stringResource(tab.labelRes),
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                            )
                        }
                    }
                },
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    AppTab.entries
                        .sortedBy { if (it == selectedTab) 1 else 0 }
                        .forEach { tab ->
                            val visible = selectedTab == tab
                            when (tab) {
                                AppTab.Home -> HomeNavHost(
                                    navController = homeNavController,
                                    onNavigateToTab = { selectedTab = it },
                                    onMessageClick = { messageId ->
                                        selectedTab = AppTab.Messages
                                        messagesNavController.navigate(MessagesRoutes.detail(messageId)) {
                                            launchSingleTop = true
                                        }
                                    },
                                    modifier = tabContentModifier(visible),
                                )
                                AppTab.Timetable -> TimetableNavHost(
                                    navController = timetableNavController,
                                    modifier = tabContentModifier(visible),
                                )
                                AppTab.Notices -> NoticesNavHost(
                                    session = session,
                                    navController = noticesNavController,
                                    modifier = tabContentModifier(visible),
                                )
                                AppTab.Messages -> MessagesNavHost(
                                    session = session,
                                    staffId = staffId,
                                    navController = messagesNavController,
                                    modifier = tabContentModifier(visible),
                                )
                                AppTab.Settings -> SettingsNavHost(
                                    navController = settingsNavController,
                                    modifier = tabContentModifier(visible),
                                )
                            }
                        }
                }
            }
        }
    }
}

private fun tabContentModifier(visible: Boolean): Modifier =
    Modifier.tabVisibilityMotion(visible = visible)

private fun popTabToRoot(
    tab: AppTab,
    homeNavController: androidx.navigation.NavHostController,
    timetableNavController: androidx.navigation.NavHostController,
    noticesNavController: androidx.navigation.NavHostController,
    messagesNavController: androidx.navigation.NavHostController,
    settingsNavController: androidx.navigation.NavHostController,
) {
    when (tab) {
        AppTab.Home -> homeNavController.popBackStack(HomeRoutes.Home, inclusive = false)
        AppTab.Timetable -> timetableNavController.popBackStack(TimetableRoutes.Timetable, inclusive = false)
        AppTab.Notices -> noticesNavController.popBackStack(NoticesRoutes.Notices, inclusive = false)
        AppTab.Messages -> messagesNavController.popBackStack(MessagesRoutes.List, inclusive = false)
        AppTab.Settings -> settingsNavController.popBackStack(SettingsRoutes.Settings, inclusive = false)
    }
}
