package org.betterseqta.betterseqtateachandroid.ui.shell

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.betterseqta.betterseqtateachandroid.R
import org.betterseqta.betterseqtateachandroid.navigation.AppTab

data class TeachDrawerActions(
    val open: () -> Unit,
)

val LocalTeachDrawerActions = staticCompositionLocalOf<TeachDrawerActions?> { null }

@Composable
fun TeachDrawerMenuIconButton(modifier: Modifier = Modifier) {
    val actions = LocalTeachDrawerActions.current ?: return
    IconButton(onClick = actions.open, modifier = modifier) {
        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.drawer_open))
    }
}

@Composable
fun TeachAppDrawerSheet(
    selectedTab: AppTab,
    onNavigateHome: () -> Unit,
    onNavigateAssessments: () -> Unit,
    onNavigateTab: (AppTab) -> Unit,
    drawerState: DrawerState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(modifier = modifier) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.tab_home)) },
            selected = selectedTab == AppTab.Home,
            onClick = {
                scope.launch { drawerState.close() }
                onNavigateHome()
            },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.drawer_assessments)) },
            selected = false,
            onClick = {
                scope.launch { drawerState.close() }
                onNavigateAssessments()
            },
            icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        AppTab.entries.filter { it != AppTab.Home }.forEach { tab ->
            NavigationDrawerItem(
                label = { Text(stringResource(tab.labelRes)) },
                selected = selectedTab == tab,
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigateTab(tab)
                },
                icon = { Icon(tab.icon, contentDescription = null) },
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
