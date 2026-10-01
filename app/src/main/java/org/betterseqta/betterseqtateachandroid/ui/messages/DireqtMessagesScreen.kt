package org.betterseqta.betterseqtateachandroid.ui.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.betterseqta.betterseqtateachandroid.domain.model.TeachLabel
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMessage
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.ui.components.FluidLoadingBarOrganic
import org.betterseqta.betterseqtateachandroid.ui.components.TeachDockedSearchBar
import org.betterseqta.betterseqtateachandroid.ui.components.TeachLargeTopScaffold
import org.betterseqta.betterseqtateachandroid.ui.components.rememberTeachTabScrollBehavior
import org.betterseqta.betterseqtateachandroid.ui.components.teachContentPadding
import org.betterseqta.betterseqtateachandroid.ui.motion.PressStyle
import org.betterseqta.betterseqtateachandroid.ui.motion.expressivePressScale
import org.betterseqta.betterseqtateachandroid.ui.motion.MessageHeroKeys
import org.betterseqta.betterseqtateachandroid.ui.motion.messageHeroSharedElement
import org.betterseqta.betterseqtateachandroid.ui.motion.premiumListItemModifier
import org.betterseqta.betterseqtateachandroid.util.FluidLoadingCoordinator
import org.betterseqta.betterseqtateachandroid.util.AppDateFormatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DireqtMessagesScreen(
    session: TeachSession?,
    onMessageClick: (Int) -> Unit,
    onComposeClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DireqtMessagesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sessionKey = session?.jsessionId ?: ""
    val scrollBehavior = rememberTeachTabScrollBehavior()

    LaunchedEffect(sessionKey) {
        if (session != null) viewModel.loadIfNeeded(session)
    }

    LaunchedEffect(uiState.searchText) {
        viewModel.onSearchChanged(session)
    }

    Box(modifier = modifier.fillMaxSize()) {
        TeachLargeTopScaffold(
            title = "Messages",
            modifier = Modifier.fillMaxSize(),
            scrollBehavior = scrollBehavior,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onComposeClick,
                    modifier = Modifier.expressivePressScale(PressStyle.Playful),
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Compose")
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .teachContentPadding(padding)
                    .fillMaxSize(),
            ) {
                LabelPillsRow(
                    labels = uiState.labels,
                    selectedLabel = uiState.selectedLabel,
                    onLabelSelected = { label -> viewModel.switchLabel(label, session) },
                )

                when {
                    uiState.isLoading && uiState.messages.isEmpty() -> {
                        FluidLoadingBarOrganic(
                            phases = FluidLoadingCoordinator.Presets.messagesList,
                            active = true,
                            finishingText = "Inbox ready",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.dp, vertical = 48.dp),
                        )
                    }
                    uiState.errorMessage != null && uiState.messages.isEmpty() -> {
                        MessagesEmptyState("Messages unavailable", uiState.errorMessage ?: "")
                    }
                    viewModel.displayedMessages.isEmpty() -> {
                        MessagesEmptyState(
                            title = "No messages",
                            subtitle = if (uiState.searchText.isEmpty()) {
                                "You're all caught up."
                            } else {
                                "No results for \"${uiState.searchText}\"."
                            },
                        )
                    }
                    else -> {
                        PullToRefreshBox(
                            isRefreshing = uiState.isLoading,
                            onRefresh = { viewModel.refresh(session) },
                            modifier = Modifier
                                .weight(1f)
                                .nestedScroll(scrollBehavior.nestedScrollConnection),
                        ) {
                            LazyColumn {
                                items(viewModel.displayedMessages, key = { it.id }) { message ->
                                    val messageId = message.messageId
                                    if (messageId != null) {
                                        MessageSwipeRow(
                                            isTrashLabel = uiState.selectedLabel == "trash",
                                            read = message.read,
                                            starred = message.starred,
                                            onToggleRead = { viewModel.toggleRead(message, session) },
                                            onToggleStar = { viewModel.toggleStar(message, session) },
                                            onTrashOrRestore = {
                                                if (uiState.selectedLabel == "trash") {
                                                    viewModel.restore(message, session)
                                                } else {
                                                    viewModel.trash(message, session)
                                                }
                                            },
                                            onContentClick = { onMessageClick(messageId) },
                                            modifier = premiumListItemModifier(),
                                        ) {
                                            MessageRow(
                                                message = message,
                                                messageId = messageId,
                                            )
                                        }
                                    } else {
                                        MessageRow(message = message, messageId = 0)
                                    }
                                }
                            }
                        }
                    }
                }

                TeachDockedSearchBar(
                    query = uiState.searchText,
                    onQueryChange = viewModel::updateSearchText,
                    placeholder = "Search messages",
                )
            }
        }
    }
}

@Composable
private fun MessagesEmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun LabelPillsRow(
    labels: List<TeachLabel>,
    selectedLabel: String,
    onLabelSelected: (String) -> Unit,
) {
    val builtIn = listOf(
        LabelItem("inbox", "Inbox", Icons.Default.Inbox),
        LabelItem("outbox", "Sent", Icons.Default.Send),
        LabelItem("starred", "Starred", Icons.Default.Star),
        LabelItem("trash", "Trash", Icons.Default.Delete),
    )
    val builtInNames = builtIn.map { it.label }.toSet()
    val custom = labels
        .filter { it.label !in builtInNames }
        .map { LabelItem(it.label, it.label.replaceFirstChar { c -> c.titlecase() }, Icons.Default.Folder) }
    val all = builtIn + custom

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(all, key = { it.label }) { item ->
            val unread = labels.firstOrNull { it.label == item.label }?.unread ?: 0
            FilterChip(
                selected = selectedLabel == item.label,
                onClick = { onLabelSelected(item.label) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(item.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(item.displayName, modifier = Modifier.padding(start = 6.dp))
                        if (unread > 0 && selectedLabel != item.label) {
                            Text(
                                text = "$unread",
                                modifier = Modifier.padding(start = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                },
            )
        }
    }
}

private data class LabelItem(
    val label: String,
    val displayName: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

@Composable
private fun MessageRow(
    message: TeachMessage,
    messageId: Int,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.ListItem(
        modifier = modifier.fillMaxWidth(),
        headlineContent = {
            Text(
                text = message.subject ?: "No subject",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (message.read) FontWeight.Normal else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .messageHeroSharedElement(MessageHeroKeys.subject(messageId)),
            )
        },
        supportingContent = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "From: ${message.sender ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (message.attachments) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    message.date?.let { instant ->
                        Text(
                            text = AppDateFormatters.relativeMessageDate(instant),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
                if (message.participants.isNotEmpty()) {
                    Text(
                        text = "To: ${message.participants.map { it.name }.joinToString()}",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        leadingContent = {
            Box(
                modifier = Modifier.messageHeroSharedElement(MessageHeroKeys.avatar(messageId)),
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ) {
                    Text(
                        text = (message.sender ?: "U").take(1).uppercase(),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (!message.read) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(12.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                    ) {}
                }
            }
        },
    )
}
