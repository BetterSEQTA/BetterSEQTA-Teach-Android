package org.betterseqta.betterseqtateachandroid.ui.assessments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.betterseqta.betterseqtateachandroid.R
import org.betterseqta.betterseqtateachandroid.domain.model.TeachAssessmentItem
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSubjectAssessmentGroup
import org.betterseqta.betterseqtateachandroid.ui.components.TeachWideContentPill
import org.betterseqta.betterseqtateachandroid.util.accentColorForSeed
import java.time.format.DateTimeFormatter

@Composable
fun CourseAssessmentsPillList(
    groups: List<TeachSubjectAssessmentGroup>,
    onAssessmentClick: (TeachAssessmentItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        groups.forEach { group ->
            CourseSubjectAssessmentBlock(
                group = group,
                onAssessmentClick = onAssessmentClick,
            )
        }
    }
}

@Composable
private fun CourseSubjectAssessmentBlock(
    group: TeachSubjectAssessmentGroup,
    onAssessmentClick: (TeachAssessmentItem) -> Unit,
) {
    val accent = accentColorForSeed(group.subjectLabel)
    val summary = stringResource(
        R.string.home_assessments_subject_summary,
        group.upcoming.size,
        group.past.size,
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = group.subjectLabel,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = accent,
        )
        Text(
            text = summary,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (group.upcoming.isNotEmpty()) {
            Text(
                text = stringResource(R.string.home_assessments_upcoming),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp),
            )
            group.upcoming.forEach { item ->
                AssessmentItemPill(
                    item = item,
                    accentColor = accent,
                    past = false,
                    onClick = { onAssessmentClick(item) },
                )
            }
        }

        if (group.past.isNotEmpty()) {
            Text(
                text = stringResource(R.string.home_assessments_past),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp),
            )
            group.past.forEach { item ->
                AssessmentItemPill(
                    item = item,
                    accentColor = accent.copy(alpha = 0.75f),
                    past = true,
                    onClick = { onAssessmentClick(item) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssessmentItemPill(
    item: TeachAssessmentItem,
    accentColor: Color,
    past: Boolean,
    onClick: () -> Unit,
) {
    val dueLabel = item.dueDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"))

    TeachWideContentPill(
        accentColor = accentColor,
        onClick = onClick,
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(R.string.home_assessments_due, dueLabel),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            PillStatChip(
                label = stringResource(
                    R.string.home_assessments_submitted,
                    item.submittedCount,
                    item.totalStudents,
                ),
                accentColor = accentColor,
            )
            PillStatChip(
                label = stringResource(
                    R.string.home_assessments_marked,
                    item.markedCount,
                    item.totalStudents,
                ),
                accentColor = accentColor,
            )
            if (past) {
                PillStatChip(
                    label = stringResource(R.string.home_assessments_past_due),
                    accentColor = MaterialTheme.colorScheme.error,
                )
            }
        }
        CompactProgressPair(
            submitted = item.submittedPercent / 100f,
            marked = item.markedPercent / 100f,
            accentColor = accentColor,
        )
        Text(
            text = stringResource(R.string.home_assessments_open_marksbook),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = accentColor,
        )
    }
}

@Composable
private fun PillStatChip(label: String, accentColor: Color) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = accentColor.copy(alpha = 0.22f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = null,
    )
}

@Composable
private fun CompactProgressPair(
    submitted: Float,
    marked: Float,
    accentColor: Color,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LinearProgressIndicator(
            progress = { submitted.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = accentColor,
            trackColor = accentColor.copy(alpha = 0.2f),
        )
        LinearProgressIndicator(
            progress = { marked.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        )
    }
}
