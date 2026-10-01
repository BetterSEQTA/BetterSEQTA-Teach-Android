package org.betterseqta.betterseqtateachandroid.ui.assessments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.betterseqta.betterseqtateachandroid.R
import org.betterseqta.betterseqtateachandroid.ui.components.FluidLoadingBarOrganic
import org.betterseqta.betterseqtateachandroid.ui.components.TeachMediumTopScaffold
import org.betterseqta.betterseqtateachandroid.ui.components.rememberTeachTabScrollBehavior
import org.betterseqta.betterseqtateachandroid.ui.components.teachContentPadding
import org.betterseqta.betterseqtateachandroid.ui.home.HomeInlineEmpty
import org.betterseqta.betterseqtateachandroid.ui.home.HomeInlineError
import org.betterseqta.betterseqtateachandroid.ui.motion.PressStyle
import org.betterseqta.betterseqtateachandroid.ui.motion.expressivePressScale
import org.betterseqta.betterseqtateachandroid.util.FluidLoadingCoordinator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseAssessmentsScreen(
    onBack: () -> Unit,
    onAssessmentClick: (org.betterseqta.betterseqtateachandroid.domain.model.TeachAssessmentItem) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CourseAssessmentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = rememberTeachTabScrollBehavior()
    val filtered = uiState.filteredGroups()

    TeachMediumTopScaffold(
        title = stringResource(R.string.drawer_assessments),
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.expressivePressScale(PressStyle.Standard),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .teachContentPadding(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        ) {
            item {
                AssessmentFilterRow(
                    selected = uiState.filter,
                    onSelected = viewModel::setFilter,
                )
            }
            when {
                uiState.isLoading && uiState.groups.isEmpty() -> item {
                    FluidLoadingBarOrganic(
                        phases = FluidLoadingCoordinator.Presets.homeLessons,
                        active = true,
                        finishingText = "Assessments ready",
                        modifier = Modifier.padding(24.dp),
                    )
                }
                uiState.errorMessage != null -> item {
                    Text(
                        text = stringResource(R.string.error_prefix, uiState.errorMessage ?: ""),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                filtered.isEmpty() -> item {
                    HomeInlineEmpty(
                        title = stringResource(R.string.home_assessments_empty_title),
                        subtitle = stringResource(R.string.course_assessments_empty_filtered),
                    )
                }
                else -> item {
                    CourseAssessmentsPillList(
                        groups = filtered,
                        onAssessmentClick = onAssessmentClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun AssessmentFilterRow(
    selected: AssessmentTimeFilter,
    onSelected: (AssessmentTimeFilter) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            AssessmentTimeFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selected == filter,
                    onClick = { onSelected(filter) },
                    label = {
                        Text(
                            when (filter) {
                                AssessmentTimeFilter.All -> stringResource(R.string.course_assessments_filter_all)
                                AssessmentTimeFilter.Upcoming -> stringResource(R.string.home_assessments_upcoming)
                                AssessmentTimeFilter.Past -> stringResource(R.string.home_assessments_past)
                            },
                        )
                    },
                )
            }
        }
    }
}
