package org.betterseqta.betterseqtateachandroid.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import org.betterseqta.betterseqtateachandroid.ui.shell.TeachDrawerMenuIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachLargeTopScaffold(
    title: String,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showDrawerMenu: Boolean = true,
    actions: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val topBarState = rememberTopAppBarState()
    val behavior = scrollBehavior
        ?: TopAppBarDefaults.pinnedScrollBehavior(topBarState)

    Scaffold(
        modifier = modifier.nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    if (showDrawerMenu) {
                        TeachDrawerMenuIconButton()
                    }
                },
                scrollBehavior = behavior,
                actions = { actions() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
        floatingActionButton = floatingActionButton,
        containerColor = MaterialTheme.colorScheme.background,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachMediumTopScaffold(
    title: String,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val topBarState = rememberTopAppBarState()
    val behavior = scrollBehavior
        ?: TopAppBarDefaults.pinnedScrollBehavior(topBarState)

    Scaffold(
        modifier = modifier.nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = navigationIcon,
                scrollBehavior = behavior,
                actions = { actions() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        content = content,
    )
}

/** Shared pinned scroll behavior for tab list screens (pairs with [TeachLargeTopScaffold]). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberTeachTabScrollBehavior(): TopAppBarScrollBehavior {
    return TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
}

/** Applies scaffold insets for tab screens (compact top app bar + pinned scroll). */
@Composable
fun Modifier.teachContentPadding(padding: PaddingValues): Modifier =
    fillMaxSize().padding(padding)

/** @deprecated Use [teachContentPadding] */
@Composable
fun Modifier.screenContentPadding(padding: PaddingValues): Modifier =
    teachContentPadding(padding)
