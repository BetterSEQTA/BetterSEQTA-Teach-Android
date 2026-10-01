package org.betterseqta.betterseqtateachandroid.ui.shell.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import org.betterseqta.betterseqtateachandroid.ui.motion.composableWithAppMotion
import androidx.navigation.compose.rememberNavController
import org.betterseqta.betterseqtateachandroid.navigation.SettingsRoutes
import org.betterseqta.betterseqtateachandroid.ui.settings.SettingsScreen

@Composable
fun SettingsNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = SettingsRoutes.Settings,
        modifier = modifier,
        route = SettingsRoutes.Graph,
    ) {
        composableWithAppMotion(SettingsRoutes.Settings) {
            SettingsScreen()
        }
    }
}

@Composable
fun rememberSettingsNavController(): NavHostController = rememberNavController()
