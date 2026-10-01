package org.betterseqta.betterseqtateachandroid.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.betterseqta.betterseqtateachandroid.R
import org.betterseqta.betterseqtateachandroid.domain.model.TeachAssessmentItem
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSubjectAssessmentGroup
import org.betterseqta.betterseqtateachandroid.ui.motion.PressStyle
import org.betterseqta.betterseqtateachandroid.ui.motion.expressivePressScale
import java.time.format.DateTimeFormatter

@Composable
fun HomeAssessmentsSection(
    groups: List<TeachSubjectAssessmentGroup>,
    modifier: Modifier = Modifier,
    defaultExpanded: Boolean = false,
    onAssessmentClick: (TeachAssessmentItem) -> Unit = {},
) {
    Column(modifier = modifier) {
        groups.forEach { group ->
            SubjectAssessmentCard(
                group = group,
                defaultExpanded = defaultExpanded,
                onAssessmentClick = onAssessmentClick,
            )
        }
    }
}

@Composable
private fun SubjectAssessmentCard(
    group: TeachSubjectAssessmentGroup,
    defaultExpanded: Boolean,
    onAssessmentClick: (TeachAssessmentItem) -> Unit,
) {
    var expanded by rememberSaveable(group.subjectLabel) { mutableStateOf(defaultExpanded) }
    val summary = stringResource(
        R.string.home_assessments_subject_summary,
        group.upcoming.size,
        group.past.size,
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .animateContentSize(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.subjectLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                )
            }

            if (expanded) {
                if (group.upcoming.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.home_assessments_upcoming),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    )
                    group.upcoming.forEach { item ->
                        AssessmentProgressRow(
                            item = item,
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
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    )
                    group.past.forEach { item ->
                        AssessmentProgressRow(
                            item = item,
                            past = true,
                            onClick = { onAssessmentClick(item) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssessmentProgressRow(
    item: TeachAssessmentItem,
    past: Boolean,
    onClick: () -> Unit,
) {
    val dueLabel = item.dueDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .expressivePressScale(PressStyle.Standard)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            if (past) {
                Text(
                    text = stringResource(R.string.home_assessments_past_due),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        Text(
            text = stringResource(R.string.home_assessments_due, dueLabel),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ProgressLine(
            label = stringResource(
                R.string.home_assessments_submitted,
                item.submittedCount,
                item.totalStudents,
            ),
            progress = item.submittedPercent / 100f,
            modifier = Modifier.padding(top = 8.dp),
        )
        ProgressLine(
            label = stringResource(
                R.string.home_assessments_marked,
                item.markedCount,
                item.totalStudents,
            ),
            progress = item.markedPercent / 100f,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = stringResource(R.string.home_assessments_open_marksbook),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 6.dp),
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun ProgressLine(
    label: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
    }
}
