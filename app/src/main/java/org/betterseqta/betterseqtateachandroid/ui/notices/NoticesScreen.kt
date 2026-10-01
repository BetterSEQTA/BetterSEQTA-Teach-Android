package org.betterseqta.betterseqtateachandroid.ui.notices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.betterseqta.betterseqtateachandroid.ui.components.TeachWideContentPill
import org.betterseqta.betterseqtateachandroid.util.plainTextFromHtml
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.betterseqta.betterseqtateachandroid.domain.model.TeachNotice
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.ui.components.FluidLoadingBarOrganic
import org.betterseqta.betterseqtateachandroid.ui.components.TeachLargeTopScaffold
import org.betterseqta.betterseqtateachandroid.ui.components.rememberTeachTabScrollBehavior
import org.betterseqta.betterseqtateachandroid.ui.components.teachContentPadding
import org.betterseqta.betterseqtateachandroid.ui.motion.PressStyle
import org.betterseqta.betterseqtateachandroid.ui.motion.expressivePressScale
import org.betterseqta.betterseqtateachandroid.ui.motion.premiumListItemModifier
import org.betterseqta.betterseqtateachandroid.util.FluidLoadingCoordinator
import org.betterseqta.betterseqtateachandroid.util.AppDateFormatters
import org.betterseqta.betterseqtateachandroid.util.colorFromHex
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticesScreen(
    session: TeachSession?,
    onNoticeClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoticesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dateKey = AppDateFormatters.formatIsoDate(uiState.selectedDate)
    val sessionKey = session?.jsessionId ?: ""

    LaunchedEffect(dateKey, sessionKey) {
        viewModel.load(session)
    }

    val scrollBehavior = rememberTeachTabScrollBehavior()

    TeachLargeTopScaffold(
        title = "Notices",
        modifier = modifier,
        scrollBehavior = scrollBehavior,
    ) { padding ->
        Column(modifier = Modifier.teachContentPadding(padding)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                onClick = { viewModel.shiftDate(-1) },
                modifier = Modifier.expressivePressScale(PressStyle.Standard),
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous day")
            }
            Text(
                text = uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(
                onClick = { viewModel.shiftDate(1) },
                modifier = Modifier.expressivePressScale(PressStyle.Standard),
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next day")
            }
        }

        when {
            uiState.isLoading && uiState.notices.isEmpty() -> {
                FluidLoadingBarOrganic(
                    phases = FluidLoadingCoordinator.Presets.notices,
                    active = true,
                    finishingText = "Notices ready",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 48.dp),
                )
            }
            uiState.errorMessage != null && uiState.notices.isEmpty() -> {
                EmptyNoticesMessage(
                    title = "Couldn't load notices",
                    subtitle = uiState.errorMessage ?: "",
                )
            }
            uiState.notices.isEmpty() -> {
                EmptyNoticesMessage(
                    title = "No notices",
                    subtitle = "No notices for this date.",
                )
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isLoading,
                    onRefresh = { viewModel.load(session) },
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp,
                        ),
                    ) {
                        items(uiState.notices, key = { it.id }) { notice ->
                            NoticePillRow(
                                notice = notice,
                                onClick = { onNoticeClick(notice.id) },
                                modifier = premiumListItemModifier(),
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun EmptyNoticesMessage(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoticePillRow(notice: TeachNotice, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = colorFromHex(notice.colour)
    val preview = notice.contents?.plainTextFromHtml()?.take(120)?.takeIf { it.isNotBlank() }

    TeachWideContentPill(
        accentColor = accent,
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(
            text = notice.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (preview != null) {
            Text(
                text = preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (!notice.labelTitle.isNullOrBlank()) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            text = notice.labelTitle,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor = accent.copy(alpha = 0.28f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    border = null,
                )
            }
            if (!notice.staff.isNullOrBlank()) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            text = notice.staff,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    border = null,
                )
            }
        }
    }
}
