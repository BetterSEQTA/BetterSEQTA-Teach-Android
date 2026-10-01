package org.betterseqta.betterseqtateachandroid.ui.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.betterseqta.betterseqtateachandroid.R
import org.betterseqta.betterseqtateachandroid.domain.model.Recipient

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComposeRecipientsSection(
    recipients: List<Recipient>,
    onAddRecipients: () -> Unit,
    onRemoveRecipient: (Recipient) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.compose_to_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            recipients.forEach { recipient ->
                InputChip(
                    selected = true,
                    onClick = { onRemoveRecipient(recipient) },
                    label = { Text(recipient.displayName, maxLines = 1) },
                    trailingIcon = {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.compose_remove_recipient))
                    },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            }
            OutlinedButton(onClick = onAddRecipients) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Text(
                    text = stringResource(R.string.compose_add_recipients),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        if (recipients.isEmpty()) {
            Text(
                text = stringResource(R.string.compose_recipients_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
fun ComposeSubjectField(
    subject: String,
    onSubjectChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = subject,
        onValueChange = onSubjectChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        label = { Text(stringResource(R.string.compose_subject_label)) },
        placeholder = { Text(stringResource(R.string.compose_subject_hint)) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}

@Composable
fun ComposeBlindCopyRow(
    blind: Boolean,
    onBlindChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier.fillMaxWidth(),
        headlineContent = { Text(stringResource(R.string.compose_bcc_title)) },
        supportingContent = { Text(stringResource(R.string.compose_bcc_subtitle)) },
        trailingContent = {
            Switch(checked = blind, onCheckedChange = onBlindChange)
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
    )
}
