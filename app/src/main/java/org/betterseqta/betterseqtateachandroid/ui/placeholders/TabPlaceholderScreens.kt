package org.betterseqta.betterseqtateachandroid.ui.placeholders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.betterseqta.betterseqtateachandroid.R
@Composable
fun HomePlaceholderScreen(
    modifier: Modifier = Modifier,
) {
    TabPlaceholderContent(
        title = stringResource(R.string.tab_home),
        subtitle = stringResource(R.string.placeholder_home_subtitle),
        modifier = modifier,
    )
}

@Composable
fun TimetablePlaceholderScreen(modifier: Modifier = Modifier) {
    TabPlaceholderContent(
        title = stringResource(R.string.tab_timetable),
        subtitle = stringResource(R.string.placeholder_coming_soon),
        modifier = modifier,
    )
}

@Composable
fun NoticesPlaceholderScreen(modifier: Modifier = Modifier) {
    TabPlaceholderContent(
        title = stringResource(R.string.tab_notices),
        subtitle = stringResource(R.string.placeholder_coming_soon),
        modifier = modifier,
    )
}

@Composable
fun MessagesListPlaceholderScreen(
    onMessageClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    TabPlaceholderContent(
        title = stringResource(R.string.tab_messages),
        subtitle = stringResource(R.string.placeholder_messages_list),
        modifier = modifier,
    )
}

@Composable
fun MessageDetailPlaceholderScreen(
    messageId: Int,
    modifier: Modifier = Modifier,
) {
    TabPlaceholderContent(
        title = stringResource(R.string.placeholder_message_detail_title, messageId),
        subtitle = stringResource(R.string.placeholder_coming_soon),
        modifier = modifier,
    )
}

@Composable
fun SettingsPlaceholderScreen(modifier: Modifier = Modifier) {
    TabPlaceholderContent(
        title = stringResource(R.string.tab_settings),
        subtitle = stringResource(R.string.placeholder_coming_soon),
        modifier = modifier,
    )
}

@Composable
private fun TabPlaceholderContent(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
