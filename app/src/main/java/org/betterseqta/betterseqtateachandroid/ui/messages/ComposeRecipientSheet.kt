package org.betterseqta.betterseqtateachandroid.ui.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.betterseqta.betterseqtateachandroid.R
import org.betterseqta.betterseqtateachandroid.domain.model.Recipient
import org.betterseqta.betterseqtateachandroid.domain.model.RecipientType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ComposeRecipientSheet(
    recipients: List<Recipient>,
    selected: List<Recipient>,
    searchText: String,
    onSearchChange: (String) -> Unit,
    onToggleRecipient: (Recipient) -> Unit,
    onDismiss: () -> Unit,
) {
    var filter by remember { mutableStateOf<RecipientType?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filtered = recipients.filter { filter == null || it.type == filter }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = stringResource(R.string.compose_recipient_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filter == null,
                    onClick = { filter = null },
                    label = { Text(stringResource(R.string.course_assessments_filter_all)) },
                )
                RecipientType.entries.forEach { type ->
                    FilterChip(
                        selected = filter == type,
                        onClick = { filter = type },
                        label = { Text(type.displayName) },
                    )
                }
            }
            SearchBar(
                expanded = false,
                onExpandedChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = SearchBarDefaults.inputFieldShape,
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchText,
                        onQueryChange = onSearchChange,
                        onSearch = { },
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { Text(stringResource(R.string.compose_search_recipients)) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                    )
                },
            ) { }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
            ) {
                items(filtered.take(80), key = { "${it.type}-${it.id}" }) { recipient ->
                    val isSelected = selected.any { it.id == recipient.id && it.type == recipient.type }
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleRecipient(recipient) },
                        headlineContent = { Text(recipient.displayName) },
                        supportingContent = { Text(recipient.type.displayName) },
                        trailingContent = {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                    )
                }
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(stringResource(R.string.compose_done))
            }
        }
    }
}
