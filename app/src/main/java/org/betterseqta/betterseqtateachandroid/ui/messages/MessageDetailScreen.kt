package org.betterseqta.betterseqtateachandroid.ui.messages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.betterseqta.betterseqtateachandroid.domain.model.ComposeMode
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.ui.components.FluidLoadingBarOrganic
import org.betterseqta.betterseqtateachandroid.ui.components.MessageHtmlWebView
import org.betterseqta.betterseqtateachandroid.ui.components.TeachMediumTopScaffold
import org.betterseqta.betterseqtateachandroid.ui.components.teachContentPadding
import org.betterseqta.betterseqtateachandroid.ui.motion.PressStyle
import org.betterseqta.betterseqtateachandroid.ui.motion.MessageHeroKeys
import org.betterseqta.betterseqtateachandroid.ui.motion.expressivePressScale
import org.betterseqta.betterseqtateachandroid.ui.motion.messageHeroSharedElement
import org.betterseqta.betterseqtateachandroid.util.FluidLoadingCoordinator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailScreen(
    messageId: Int,
    session: TeachSession?,
    onBack: () -> Unit,
    onCompose: (ComposeMode, Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MessageDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sessionKey = session?.jsessionId ?: ""

    LaunchedEffect(messageId, sessionKey) {
        viewModel.loadIfNeeded(session, messageId)
    }

    LaunchedEffect(uiState.didTrash) {
        if (uiState.didTrash) onBack()
    }

    TeachMediumTopScaffold(
        title = "Message",
        modifier = modifier,
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.expressivePressScale(PressStyle.Standard),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            val detail = uiState.detail
            if (detail != null) {
                IconButton(
                    onClick = { viewModel.toggleStar(session) },
                    modifier = Modifier.expressivePressScale(PressStyle.Standard),
                ) {
                    Icon(
                        if (detail.starred) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Star",
                    )
                }
                IconButton(
                    onClick = { viewModel.trash(session) },
                    modifier = Modifier.expressivePressScale(PressStyle.Standard),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Trash")
                }
                IconButton(
                    onClick = { onCompose(ComposeMode.Reply, messageId) },
                    modifier = Modifier.expressivePressScale(PressStyle.Standard),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = "Reply")
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.teachContentPadding(padding)) {
        when {
            uiState.isLoading && uiState.detail == null -> {
                FluidLoadingBarOrganic(
                    phases = FluidLoadingCoordinator.Presets.messageDetail,
                    active = true,
                    finishingText = "Message loaded",
                    modifier = Modifier.padding(24.dp),
                )
            }
            uiState.errorMessage != null && uiState.detail == null -> {
                Text(
                    text = uiState.errorMessage ?: "Message unavailable",
                    modifier = Modifier.padding(24.dp),
                )
            }
            uiState.detail != null -> {
                val detail = uiState.detail!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    Text(
                        text = detail.subject ?: "No subject",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.messageHeroSharedElement(MessageHeroKeys.subject(messageId)),
                    )
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        detail.sender?.let { sender ->
                            Text(
                                text = sender,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        detail.date?.let { instant ->
                            val formatted = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a")
                                .withZone(ZoneId.systemDefault())
                                .format(instant)
                            Text(
                                text = " · $formatted",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (detail.participants.isNotEmpty()) {
                        Text(
                            text = "To: ${detail.participants.map { it.name }.joinToString()}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    val body = detail.body
                    if (!body.isNullOrEmpty()) {
                        MessageHtmlWebView(html = body, modifier = Modifier.fillMaxWidth())
                    } else {
                        Text("No message content.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (detail.files.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        Text("Attachments", style = MaterialTheme.typography.titleSmall)
                        detail.files.forEach { file ->
                            Text(
                                text = file.filename,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }
        }
        }
    }
}
