package org.betterseqta.betterseqtateachandroid.ui.timetable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.betterseqta.betterseqtateachandroid.R
import org.betterseqta.betterseqtateachandroid.domain.model.TeachLesson
import org.betterseqta.betterseqtateachandroid.ui.components.TeachLargeTopScaffold
import org.betterseqta.betterseqtateachandroid.ui.components.rememberTeachTabScrollBehavior
import org.betterseqta.betterseqtateachandroid.ui.components.teachContentPadding
import org.betterseqta.betterseqtateachandroid.ui.motion.PressStyle
import org.betterseqta.betterseqtateachandroid.ui.motion.expressivePressScale
import org.betterseqta.betterseqtateachandroid.ui.motion.premiumListItemModifier
import org.betterseqta.betterseqtateachandroid.ui.components.FluidLoadingBarOrganic
import org.betterseqta.betterseqtateachandroid.util.FluidLoadingCoordinator
import org.betterseqta.betterseqtateachandroid.ui.components.LessonRow
import org.betterseqta.betterseqtateachandroid.util.AppDateFormatters
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    onLessonClick: (TeachLesson, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TimetableViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val scrollBehavior = rememberTeachTabScrollBehavior()

    LaunchedEffect(uiState.selectedDate) {
        viewModel.load()
    }

    TeachLargeTopScaffold(
        title = stringResource(R.string.tab_timetable),
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        actions = {
            IconButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.expressivePressScale(PressStyle.Standard),
            ) {
                Icon(Icons.Filled.CalendarToday, contentDescription = stringResource(R.string.pick_date))
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.teachContentPadding(innerPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { viewModel.shiftDate(-1) },
                    modifier = Modifier.expressivePressScale(PressStyle.Standard),
                ) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.previous_day))
                }
                Text(
                    text = AppDateFormatters.displayDate(uiState.selectedDate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                )
                IconButton(
                    onClick = { viewModel.shiftDate(1) },
                    modifier = Modifier.expressivePressScale(PressStyle.Standard),
                ) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.next_day))
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                    FluidLoadingBarOrganic(
                        phases = FluidLoadingCoordinator.Presets.timetable,
                        active = true,
                        finishingText = "Timetable ready",
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                    }
                }
                uiState.errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.timetable_error_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(24.dp),
                            )
                        }
                    }
                }
                uiState.lessons.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.timetable_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(R.string.timetable_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp),
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection),
                    ) {
                        items(uiState.lessons, key = { "${it.id}-${it.from}-${it.until}" }) { lesson ->
                            val dateString = AppDateFormatters.isoFromLocalDate(uiState.selectedDate)
                            val clickable = lesson.classunitId != null
                            val baseModifier = premiumListItemModifier().fillMaxWidth()
                            if (clickable) {
                                ListItem(
                                    modifier = baseModifier.clickable { onLessonClick(lesson, dateString) },
                                    headlineContent = {
                                        LessonRow(lesson = lesson, showAdhocLabel = true)
                                    },
                                )
                            } else {
                                ListItem(
                                    modifier = baseModifier,
                                    headlineContent = {
                                        LessonRow(lesson = lesson, showAdhocLabel = true)
                                    },
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel.setSelectedDate(date)
                        }
                        showDatePicker = false
                    },
                ) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        ) {
            DatePicker(state = state)
        }
    }
}
