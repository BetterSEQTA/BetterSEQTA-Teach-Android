package org.betterseqta.betterseqtateachandroid.ui.marksbook

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.betterseqta.betterseqtateachandroid.R
import org.betterseqta.betterseqtateachandroid.domain.model.MarkCellKey
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMarksbookStudentRow
import org.betterseqta.betterseqtateachandroid.ui.components.FluidLoadingBarOrganic
import org.betterseqta.betterseqtateachandroid.ui.motion.PressStyle
import org.betterseqta.betterseqtateachandroid.ui.motion.expressivePressScale
import org.betterseqta.betterseqtateachandroid.util.FluidLoadingCoordinator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksbookScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MarksbookViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.marksbook_save_success)
    val snapshot = uiState.snapshot

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(savedMessage)
            viewModel.consumeSaveSuccess()
        }
    }

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showSaveError by remember { mutableStateOf(false) }
    if (uiState.saveError != null) showSaveError = true

    if (showSaveError && uiState.saveError != null) {
        AlertDialog(
            onDismissRequest = {
                showSaveError = false
                viewModel.clearSaveError()
            },
            title = { Text(stringResource(R.string.marksbook_save_failed)) },
            text = { Text(uiState.saveError ?: "") },
            confirmButton = {
                TextButton(onClick = {
                    showSaveError = false
                    viewModel.clearSaveError()
                }) { Text(stringResource(R.string.compose_ok)) }
            },
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.marksbook_discard_title)) },
            text = { Text(stringResource(R.string.marksbook_discard_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onBack()
                }) { Text(stringResource(R.string.marksbook_discard_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.marksbook_keep_editing))
                }
            },
        )
    }

    val title = snapshot?.title ?: stringResource(R.string.marksbook_title)
    val subtitle = snapshot?.classLabel

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (viewModel.hasUnsavedChanges()) showDiscardDialog = true else onBack()
                        },
                        modifier = Modifier.expressivePressScale(PressStyle.Standard),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        IconButton(
                            onClick = viewModel::save,
                            enabled = viewModel.hasUnsavedChanges(),
                            modifier = Modifier.expressivePressScale(PressStyle.Standard),
                        ) {
                            Icon(Icons.Default.Save, contentDescription = stringResource(R.string.marksbook_save))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
    ) { padding ->
        when {
            uiState.isLoading && snapshot == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    FluidLoadingBarOrganic(
                        phases = FluidLoadingCoordinator.Presets.homeLessons,
                        active = true,
                        finishingText = stringResource(R.string.marksbook_loading),
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
            uiState.errorMessage != null && snapshot == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.error_prefix, uiState.errorMessage ?: ""),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            snapshot != null -> {
                MarksbookContent(
                    padding = padding,
                    uiState = uiState,
                    viewModel = viewModel,
                )
            }
        }
    }
}

@Composable
private fun MarksbookContent(
    padding: PaddingValues,
    uiState: MarksbookUiState,
    viewModel: MarksbookViewModel,
) {
    val snapshot = uiState.snapshot ?: return
    val assessment = uiState.selectedAssessment() ?: return
    val criterion = uiState.selectedCriterion() ?: return
    val markKey = MarkCellKey(assessment.id, criterion.id)
    val isNumeric = snapshot.isNumeric == 1
    val studentTotal = snapshot.students.size
    val marked = uiState.markedCount()
    val average = uiState.averageScoreLabel(isNumeric)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                snapshot.assessments.forEach { item ->
                    val chipColor = parseHexColor(item.setColourHex)
                    FilterChip(
                        selected = item.id == assessment.id,
                        onClick = { viewModel.selectAssessment(item.id) },
                        label = {
                            Text(
                                text = item.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        leadingIcon = if (chipColor != null) {
                            {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(chipColor),
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
            }
        }

        if (assessment.criteria.size > 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    assessment.criteria.forEach { item ->
                        FilterChip(
                            selected = item.id == criterion.id,
                            onClick = { viewModel.selectCriterion(item.id) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        }

        item {
            MarksbookStatsRow(
                studentCount = studentTotal,
                markedCount = marked,
                averageLabel = average,
                criterionLabel = criterion.label,
                target = criterion.target,
                weight = assessment.weight,
                setDescription = assessment.setDescription,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        items(snapshot.students, key = { it.id }) { student ->
            MarksbookStudentRow(
                student = student,
                markKey = markKey,
                score = viewModel.scoreFor(student.id, markKey),
                isNumeric = isNumeric,
                target = criterion.target,
                onScoreChange = { viewModel.updateScore(student.id, it) },
            )
        }
    }
}

@Composable
private fun MarksbookStatsRow(
    studentCount: Int,
    markedCount: Int,
    averageLabel: String?,
    criterionLabel: String,
    target: Int?,
    weight: Int?,
    setDescription: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = setDescription,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatAssistChip(
                label = stringResource(R.string.marksbook_stat_students, studentCount),
            )
            StatAssistChip(
                label = stringResource(R.string.marksbook_stat_marked, markedCount, studentCount),
            )
            if (averageLabel != null) {
                StatAssistChip(
                    label = stringResource(R.string.marksbook_stat_average, averageLabel),
                )
            }
            if (weight != null) {
                StatAssistChip(
                    label = stringResource(R.string.marksbook_stat_weight, weight),
                )
            }
        }
        val targetLine = when {
            target != null -> stringResource(R.string.marksbook_criterion_target, criterionLabel, target)
            else -> criterionLabel
        }
        Text(
            text = targetLine,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatAssistChip(label: String) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    )
}

@Composable
private fun MarksbookStudentRow(
    student: TeachMarksbookStudentRow,
    markKey: MarkCellKey,
    score: String,
    isNumeric: Boolean,
    target: Int?,
    onScoreChange: (String) -> Unit,
) {
    val initial = student.displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        ListItem(
            headlineContent = {
                Text(student.displayName, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = {
                if (student.subtitle.isNotBlank()) {
                    Text(
                        student.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            },
            trailingContent = {
                TextField(
                    value = score,
                    onValueChange = onScoreChange,
                    modifier = Modifier.widthIn(min = 72.dp, max = 96.dp),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = target?.toString() ?: "—",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    keyboardOptions = if (isNumeric) {
                        KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    } else {
                        KeyboardOptions.Default
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
                )
            },
        )
    }
}

private fun parseHexColor(raw: String?): Color? {
    if (raw.isNullOrBlank()) return null
    val hex = raw.removePrefix("#")
    return runCatching {
        when (hex.length) {
            6 -> Color(0xFF000000 or hex.toLong(16))
            8 -> Color(hex.toLong(16))
            else -> null
        }
    }.getOrNull()
}
