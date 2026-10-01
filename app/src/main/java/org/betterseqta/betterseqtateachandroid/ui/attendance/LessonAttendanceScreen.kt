package org.betterseqta.betterseqtateachandroid.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import org.betterseqta.betterseqtateachandroid.R
import org.betterseqta.betterseqtateachandroid.domain.model.AttendanceViewMode
import org.betterseqta.betterseqtateachandroid.domain.model.TeachAttendanceStudent
import org.betterseqta.betterseqtateachandroid.ui.components.FluidLoadingBar
import org.betterseqta.betterseqtateachandroid.util.AppDateFormatters
import org.betterseqta.betterseqtateachandroid.util.AttendanceIconHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonAttendanceScreen(
    onBack: () -> Unit,
    onOpenStats: () -> Unit,
    viewModel: LessonAttendanceViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val lesson = uiState.lesson
    val title = if (lesson != null) {
        AppDateFormatters.lessonTitleDateTime(uiState.date, lesson.from, lesson.until, lesson.description)
    } else {
        stringResource(R.string.attendance_title)
    }

    var showSaveError by remember { mutableStateOf(false) }
    if (uiState.saveError != null) {
        showSaveError = true
    }

    if (showSaveError && uiState.saveError != null) {
        AlertDialog(
            onDismissRequest = {
                showSaveError = false
                viewModel.clearSaveError()
            },
            title = { Text(stringResource(R.string.attendance_save_failed)) },
            text = { Text(uiState.saveError ?: "") },
            confirmButton = {
                TextButton(onClick = {
                    showSaveError = false
                    viewModel.clearSaveError()
                }) { Text(stringResource(android.R.string.ok)) }
            },
        )
    }

    val pickerStudent = uiState.typePickerStudentId?.let { id ->
        uiState.students.firstOrNull { it.id == id }
    }
    if (pickerStudent != null) {
        AttendanceTypePickerSheet(
            types = viewModel.selectableTypes(),
            studentName = "${pickerStudent.prefname ?: pickerStudent.firstname} ${pickerStudent.surname}",
            currentCode = viewModel.effectiveCode(pickerStudent),
            onSelect = {
                viewModel.setPending(pickerStudent.id, it)
                viewModel.dismissTypePicker()
            },
            onClear = {
                viewModel.clearPending(pickerStudent.id)
                viewModel.dismissTypePicker()
            },
            onDismiss = viewModel::dismissTypePicker,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::save,
                        enabled = !uiState.isSaving && uiState.pendingChanges.isNotEmpty(),
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.save), fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading && uiState.students.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    FluidLoadingBar(
                        phaseText = stringResource(R.string.attendance_loading),
                        modifier = Modifier.padding(horizontal = 28.dp),
                    )
                }
            }
            uiState.errorMessage != null && uiState.students.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(uiState.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.students.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.attendance_no_students))
                }
            }
            uiState.viewMode == AttendanceViewMode.List -> {
                AttendanceListContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    onOpenStats = onOpenStats,
                    modifier = Modifier.padding(padding),
                )
            }
            else -> {
                AttendanceCardContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun AttendanceListContent(
    uiState: LessonAttendanceUiState,
    viewModel: LessonAttendanceViewModel,
    onOpenStats: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        if (uiState.summaryByStudent.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenStats)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(stringResource(R.string.attendance_view_stats), fontWeight = FontWeight.Medium)
                        Text(
                            stringResource(R.string.attendance_view_stats_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        items(uiState.students) { student ->
            AttendanceStudentSwipeRow(
                onMarkYes = { viewModel.setPending(student.id, "yes") },
                onMarkNo = { viewModel.setPending(student.id, "no") },
            ) {
                StudentRow(
                    student = student,
                    status = viewModel.resolveStatus(student),
                    onTap = { viewModel.cycleAttendance(student.id) },
                    onLongPress = { viewModel.showTypePicker(student.id) },
                    onMarkYes = { viewModel.setPending(student.id, "yes") },
                    onMarkNo = { viewModel.setPending(student.id, "no") },
                )
            }
        }
    }
}

@Composable
private fun StudentRow(
    student: TeachAttendanceStudent,
    status: ResolvedAttendanceStatus?,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onMarkYes: () -> Unit,
    onMarkNo: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(student.prefname ?: student.firstname, fontWeight = FontWeight.SemiBold)
            Text(student.surname, style = MaterialTheme.typography.bodySmall)
            student.rollgroupname?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        AttendanceBadge(status)
        Row {
            IconButton(onClick = onMarkYes) {
                Icon(Icons.Filled.CheckCircle, contentDescription = stringResource(R.string.attendance_yes), tint = Color(0xFF2E7D32))
            }
            IconButton(onClick = onMarkNo) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.attendance_no), tint = Color(0xFFC62828))
            }
            IconButton(onClick = onLongPress) {
                Text("···")
            }
        }
    }
}

@Composable
private fun AttendanceBadge(status: ResolvedAttendanceStatus?) {
    if (status == null) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0x1F000000))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.RemoveCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                stringResource(R.string.attendance_not_marked),
                modifier = Modifier.padding(start = 6.dp),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        return
    }
    val color = attendanceStatusColor(status.label)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AttendanceIconHelper.iconFor(status.code), contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(status.label, color = color, modifier = Modifier.padding(start = 6.dp), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AttendanceCardContent(
    uiState: LessonAttendanceUiState,
    viewModel: LessonAttendanceViewModel,
    modifier: Modifier = Modifier,
) {
    val cardStackSize = 3
    if (uiState.cardIndex >= uiState.students.size) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.attendance_cards_done))
        }
        return
    }

    val scope = rememberCoroutineScope()
    var dragX by remember(uiState.cardIndex) { mutableFloatStateOf(0f) }
    var isFlicking by remember(uiState.cardIndex) { mutableStateOf(false) }
    val dragAnim = remember(uiState.cardIndex) { Animatable(0f) }
    val flickSpec = spring<Float>(stiffness = Spring.StiffnessMediumLow)

    fun flickCard(direction: Int, studentId: Int, code: String) {
        if (isFlicking) return
        isFlicking = true
        viewModel.setPending(studentId, code)
        scope.launch {
            dragAnim.snapTo(dragX)
            dragAnim.animateTo(direction * 450f, flickSpec)
            delay(340)
            viewModel.advanceCard()
            dragAnim.snapTo(0f)
            dragX = 0f
            isFlicking = false
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.attendance_card_progress, uiState.cardIndex + 1, uiState.students.size),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            val end = minOf(uiState.students.size, uiState.cardIndex + cardStackSize)
            for (index in uiState.cardIndex until end) {
                val stackOffset = index - uiState.cardIndex
                val student = uiState.students[index]
                val isTop = stackOffset == 0
                val offsetX = if (isTop) dragAnim.value + dragX else 0f
                CardStudentPanel(
                    student = student,
                    status = viewModel.resolveStatus(student),
                    offsetX = offsetX,
                    stackOffset = stackOffset,
                    isTop = isTop,
                    showSwipeHints = isTop && !isFlicking,
                    modifier = Modifier.zIndex((cardStackSize - stackOffset).toFloat()),
                    onLongPress = { if (isTop) viewModel.showTypePicker(student.id) },
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(cardStackSize + 1f)
                    .pointerInput(uiState.cardIndex, isFlicking) {
                        if (isFlicking) return@pointerInput
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                val student = uiState.students.getOrNull(uiState.cardIndex) ?: return@detectHorizontalDragGestures
                                when {
                                    dragX > 80f -> flickCard(1, student.id, "yes")
                                    dragX < -80f -> flickCard(-1, student.id, "no")
                                    else -> {
                                        scope.launch {
                                            dragAnim.snapTo(dragX)
                                            dragAnim.animateTo(0f, flickSpec)
                                            dragX = 0f
                                        }
                                    }
                                }
                            },
                            onHorizontalDrag = { _, amount ->
                                if (!isFlicking) dragX += amount
                            },
                        )
                    },
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (uiState.cardIndex > 0) {
                TextButton(onClick = viewModel::previousCard) {
                    Text(stringResource(R.string.attendance_previous))
                }
            } else {
                Box(Modifier.size(1.dp))
            }
            TextButton(onClick = viewModel::advanceCard) {
                Text(stringResource(R.string.attendance_skip))
            }
        }
    }
}

@Composable
private fun CardStudentPanel(
    student: TeachAttendanceStudent,
    status: ResolvedAttendanceStatus?,
    offsetX: Float,
    stackOffset: Int,
    isTop: Boolean,
    showSwipeHints: Boolean,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale = 1f - 0.04f * stackOffset
    val yOffset = (4 * stackOffset).dp
    Box(
        modifier = modifier
            .fillMaxWidth(0.92f - stackOffset * 0.02f)
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = yOffset.toPx()
                rotationZ = if (isTop) offsetX / 24f else 0f
            }
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onLongPress)
            .padding(36.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (showSwipeHints && offsetX > 15f) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        alpha = minOf(1f, offsetX / 90f)
                        scaleX = 0.6f + 0.4f * minOf(1f, offsetX / 90f)
                        scaleY = scaleX
                    },
            )
        }
        if (showSwipeHints && offsetX < -15f) {
            Icon(
                Icons.Filled.Close,
                contentDescription = null,
                tint = Color(0xFFC62828),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .graphicsLayer {
                        alpha = minOf(1f, -offsetX / 90f)
                        scaleX = 0.6f + 0.4f * minOf(1f, -offsetX / 90f)
                        scaleY = scaleX
                    },
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color(0x1A1565C0)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (student.prefname ?: student.firstname).take(1),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1565C0),
                )
            }
            Text(
                text = "${student.prefname ?: student.firstname} ${student.surname}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 20.dp),
            )
            student.rollgroupname?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (status != null) {
                val color = attendanceStatusColor(status.label)
                Text(
                    text = status.label,
                    color = color,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp),
                )
            } else {
                Text(
                    text = stringResource(R.string.attendance_swipe_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}
