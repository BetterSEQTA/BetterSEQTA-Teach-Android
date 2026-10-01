package org.betterseqta.betterseqtateachandroid.ui.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.betterseqta.betterseqtateachandroid.R
import org.betterseqta.betterseqtateachandroid.domain.model.ComposeMode
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.ui.components.ComposeEditorDockedToolbar
import org.betterseqta.betterseqtateachandroid.ui.components.ComposeHtmlEditorWebView
import org.betterseqta.betterseqtateachandroid.ui.motion.PressStyle
import org.betterseqta.betterseqtateachandroid.ui.motion.expressivePressScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeMessageScreen(
    mode: ComposeMode,
    messageId: Int?,
    session: TeachSession?,
    selfStaffId: Int?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ComposeMessageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRecipientPicker by remember { mutableStateOf(false) }
    var formatCommand by remember { mutableStateOf<String?>(null) }
    val canSend = uiState.selectedRecipients.isNotEmpty() && !uiState.isSending

    LaunchedEffect(mode, messageId) {
        viewModel.initialize(mode, messageId)
        viewModel.loadRecipients(session, mode, messageId, selfStaffId)
    }

    LaunchedEffect(uiState.didSend) {
        if (uiState.didSend) onDismiss()
    }

    val title = when (mode) {
        ComposeMode.New -> stringResource(R.string.compose_title_new)
        ComposeMode.Reply -> stringResource(R.string.compose_title_reply)
        ComposeMode.ReplyAll -> stringResource(R.string.compose_title_reply_all)
        ComposeMode.Forward -> stringResource(R.string.compose_title_forward)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.expressivePressScale(PressStyle.Standard),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.compose_discard),
                        )
                    }
                },
                actions = {
                    if (uiState.isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .expressivePressScale(PressStyle.Standard),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        FilledTonalButton(
                            onClick = { viewModel.send(session) },
                            enabled = canSend,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .expressivePressScale(PressStyle.Playful),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Text(
                                text = stringResource(R.string.compose_send),
                                modifier = Modifier.padding(start = 6.dp),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            ComposeEditorDockedToolbar(
                onFormat = { formatCommand = it },
                modifier = Modifier.imePadding(),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            ComposeRecipientsSection(
                recipients = uiState.selectedRecipients,
                onAddRecipients = { showRecipientPicker = true },
                onRemoveRecipient = viewModel::removeRecipient,
            )
            ComposeSubjectField(
                subject = uiState.subject,
                onSubjectChange = viewModel::updateSubject,
            )
            ComposeBlindCopyRow(
                blind = uiState.blind,
                onBlindChange = viewModel::updateBlind,
            )
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 1.dp,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ComposeHtmlEditorWebView(
                        html = uiState.bodyHtml,
                        onHtmlChanged = viewModel::updateBody,
                        formatCommand = formatCommand,
                        onFormatCommandConsumed = { formatCommand = null },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }

    uiState.sendError?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::clearSendError,
            confirmButton = {
                TextButton(onClick = viewModel::clearSendError) {
                    Text(stringResource(R.string.compose_ok))
                }
            },
            title = { Text(stringResource(R.string.compose_error_title)) },
            text = { Text(error) },
        )
    }

    if (showRecipientPicker) {
        ComposeRecipientSheet(
            recipients = viewModel.filteredRecipients,
            selected = uiState.selectedRecipients,
            searchText = uiState.recipientSearchText,
            onSearchChange = viewModel::updateRecipientSearch,
            onToggleRecipient = viewModel::toggleRecipient,
            onDismiss = { showRecipientPicker = false },
        )
    }
}
