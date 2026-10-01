package org.betterseqta.betterseqtateachandroid.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.betterseqta.betterseqtateachandroid.R
import org.betterseqta.betterseqtateachandroid.domain.model.TeachAssessmentItem
import org.betterseqta.betterseqtateachandroid.domain.model.TeachLesson
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMessage
import org.betterseqta.betterseqtateachandroid.navigation.AppTab
import org.betterseqta.betterseqtateachandroid.ui.components.FluidLoadingBarOrganic
import org.betterseqta.betterseqtateachandroid.ui.components.LessonRow
import org.betterseqta.betterseqtateachandroid.ui.components.TeachLargeTopScaffold
import org.betterseqta.betterseqtateachandroid.ui.components.rememberTeachTabScrollBehavior
import org.betterseqta.betterseqtateachandroid.ui.components.teachContentPadding
import org.betterseqta.betterseqtateachandroid.ui.motion.premiumListItemModifier
import org.betterseqta.betterseqtateachandroid.util.AppDateFormatters
import org.betterseqta.betterseqtateachandroid.util.FluidLoadingCoordinator
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onNavigateToTab: (AppTab) -> Unit,
    onOpenAssessments: () -> Unit,
    onAssessmentClick: (TeachAssessmentItem) -> Unit = {},
    onLessonClick: (TeachLesson) -> Unit,
    onMessageClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = rememberTeachTabScrollBehavior()

    TeachLargeTopScaffold(
        title = stringResource(R.string.tab_home),
        modifier = modifier,
        scrollBehavior = scrollBehavior,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .teachContentPadding(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        ) {
            item {
                HomeGreeting(name = uiState.greetingName)
            }
            item {
                HomeStatsRow(
                    messageCount = uiState.messages.size,
                    assessmentsToMark = uiState.assessmentsToMarkCount,
                    lessonCount = uiState.todayLessons.size,
                    onMessages = { onNavigateToTab(AppTab.Messages) },
                    onLessons = { onNavigateToTab(AppTab.Timetable) },
                )
            }

            item {
                HomeWidgetCard(
                    title = stringResource(R.string.home_todays_lessons),
                    actionLabel = stringResource(R.string.home_see_all),
                    onAction = { onNavigateToTab(AppTab.Timetable) },
                ) {
                    when {
                        uiState.lessonsLoading && uiState.todayLessons.isEmpty() -> {
                            FluidLoadingBarOrganic(
                                phases = FluidLoadingCoordinator.Presets.homeLessons,
                                active = true,
                                finishingText = "Today's classes ready",
                                modifier = Modifier.padding(24.dp),
                            )
                        }
                        uiState.lessonsError != null -> {
                            HomeInlineError(
                                stringResource(R.string.error_prefix, uiState.lessonsError ?: ""),
                            )
                        }
                        uiState.todayLessons.isEmpty() -> {
                            HomeInlineEmpty(
                                title = stringResource(R.string.home_no_lessons_title),
                                subtitle = stringResource(R.string.home_no_lessons_subtitle),
                            )
                        }
                        else -> {
                            uiState.todayLessons.take(4).forEach { lesson ->
                                val clickable = lesson.classunitId != null
                                ListItem(
                                    modifier = premiumListItemModifier()
                                        .fillMaxWidth()
                                        .then(
                                            if (clickable) Modifier.clickable { onLessonClick(lesson) }
                                            else Modifier,
                                        ),
                                    headlineContent = { LessonRow(lesson = lesson) },
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }

            item {
                HomeWidgetCard(
                    title = stringResource(R.string.home_assessments_title),
                    actionLabel = stringResource(R.string.home_see_all),
                    onAction = onOpenAssessments,
                ) {
                    when {
                        uiState.assessmentsLoading && uiState.assessmentGroups.isEmpty() -> {
                            FluidLoadingBarOrganic(
                                phases = FluidLoadingCoordinator.Presets.homeLessons,
                                active = true,
                                finishingText = "Assessments ready",
                                modifier = Modifier.padding(24.dp),
                            )
                        }
                        uiState.assessmentsError != null -> {
                            HomeInlineError(
                                stringResource(R.string.error_prefix, uiState.assessmentsError ?: ""),
                            )
                        }
                        uiState.assessmentGroups.isEmpty() -> {
                            HomeInlineEmpty(
                                title = stringResource(R.string.home_assessments_empty_title),
                                subtitle = stringResource(R.string.home_assessments_empty_subtitle),
                            )
                        }
                        else -> {
                            HomeAssessmentsSection(
                                groups = uiState.assessmentGroups,
                                onAssessmentClick = onAssessmentClick,
                            )
                        }
                    }
                }
            }

            item {
                HomeWidgetCard(
                    title = stringResource(R.string.home_direqt_messages),
                    actionLabel = stringResource(R.string.home_see_all),
                    onAction = { onNavigateToTab(AppTab.Messages) },
                ) {
                    when {
                        uiState.messagesLoading && uiState.messages.isEmpty() -> {
                            FluidLoadingBarOrganic(
                                phases = FluidLoadingCoordinator.Presets.homeMessages,
                                active = true,
                                finishingText = "Direqt ready",
                                modifier = Modifier.padding(24.dp),
                            )
                        }
                        uiState.messagesError != null -> {
                            HomeInlineError(
                                stringResource(R.string.error_prefix, uiState.messagesError ?: ""),
                            )
                        }
                        uiState.messages.isEmpty() -> {
                            HomeInlineEmpty(
                                title = stringResource(R.string.home_no_messages_title),
                                subtitle = stringResource(R.string.home_no_messages_subtitle),
                            )
                        }
                        else -> {
                            uiState.messages.take(4).forEach { message ->
                                ListItem(
                                    modifier = premiumListItemModifier()
                                        .fillMaxWidth()
                                        .clickable { message.messageId?.let(onMessageClick) },
                                    headlineContent = { HomeMessageRow(message = message) },
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
            item { Box(Modifier.padding(bottom = 24.dp)) }
        }
    }
}

@Composable
private fun HomeGreeting(name: String?) {
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> stringResource(R.string.home_greeting_morning)
        in 12..16 -> stringResource(R.string.home_greeting_afternoon)
        else -> stringResource(R.string.home_greeting_evening)
    }
    val line = if (!name.isNullOrBlank()) "$greeting, $name!" else "$greeting!"
    Text(
        text = line,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

@Composable
private fun HomeStatsRow(
    messageCount: Int,
    assessmentsToMark: Int,
    lessonCount: Int,
    onMessages: () -> Unit,
    onLessons: () -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HomeStatPill(
            label = stringResource(R.string.home_stat_messages, messageCount),
            onClick = onMessages,
        )
        HomeStatPill(
            label = stringResource(R.string.home_stat_to_mark, assessmentsToMark),
            onClick = { },
        )
        HomeStatPill(
            label = stringResource(R.string.home_stat_lessons, lessonCount),
            onClick = onLessons,
        )
    }
}

@Composable
private fun HomeStatPill(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun HomeMessageRow(message: TeachMessage, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = (message.sender ?: "U").take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
            Row {
                Text(
                    text = message.sender ?: stringResource(R.string.unknown_sender),
                    fontWeight = if (message.read) FontWeight.Medium else FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                Text(
                    text = AppDateFormatters.formatMessageDate(message.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = message.subject ?: stringResource(R.string.no_subject),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
        if (!message.read) {
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}
