package org.betterseqta.betterseqtateachandroid.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.betterseqta.betterseqtateachandroid.R
import org.betterseqta.betterseqtateachandroid.util.AppDateFormatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceStatsScreen(
    onBack: () -> Unit,
    viewModel: LessonAttendanceViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val lesson = uiState.lesson
    val title = if (lesson != null) {
        AppDateFormatters.lessonTitleDateTime(
            dateIso = uiState.date,
            lessonFrom = lesson.from,
            lessonUntil = lesson.until,
            subject = lesson.description,
        )
    } else {
        stringResource(R.string.attendance_stats_title)
    }

    val presentCount = uiState.summaryByStudent.values.count { it.present >= 1 }
    val total = uiState.students.size
    val percent = if (total > 0) (presentCount * 100) / total else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(20.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        StatCard(
                            value = presentCount.toString(),
                            label = stringResource(R.string.attendance_present_label),
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            value = "$percent%",
                            label = stringResource(R.string.attendance_rate_label),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Text(
                        text = stringResource(R.string.attendance_summary_line, presentCount, total),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }
            item {
                Text(
                    text = stringResource(R.string.attendance_by_student),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
            items(uiState.students) { student ->
                val summary = uiState.summaryByStudent[student.id] ?: return@items
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (summary.present >= 1) Color(0x334CAF50) else Color(0x1F000000),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (student.prefname ?: student.firstname).take(1),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = "${student.prefname ?: student.firstname} ${student.surname}",
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                    )
                    Text(
                        text = "${summary.percent}%",
                        fontWeight = FontWeight.SemiBold,
                        color = if (summary.present >= 1) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = value, style = MaterialTheme.typography.headlineLarge, color = color, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
