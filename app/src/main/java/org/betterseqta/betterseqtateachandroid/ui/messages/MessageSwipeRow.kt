package org.betterseqta.betterseqtateachandroid.ui.messages

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val LeadingReveal = 96.dp
private val TrailingReveal = 168.dp
private val FullSwipeTrash = 220.dp

@Composable
fun MessageSwipeRow(
    isTrashLabel: Boolean,
    read: Boolean,
    starred: Boolean,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    onTrashOrRestore: () -> Unit,
    onContentClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val viewConfiguration = LocalViewConfiguration.current
    val leadingPx = with(density) { LeadingReveal.toPx() }
    val trailingPx = with(density) { TrailingReveal.toPx() }
    val fullTrashPx = with(density) { FullSwipeTrash.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val snapSpec = spring<Float>(stiffness = Spring.StiffnessMedium)

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SwipeActionChip(
                label = if (read) "Unread" else "Read",
                containerColor = Color(0xFF1565C0),
                onClick = {
                    onToggleRead()
                    scope.launch { offsetX.animateTo(0f, snapSpec) }
                },
                modifier = Modifier
                    .width(LeadingReveal)
                    .fillMaxHeight(),
            ) {
                Icon(
                    if (read) Icons.Default.MarkEmailUnread else Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
            Box(modifier = Modifier.weight(1f))
            SwipeActionChip(
                label = if (starred) "Unflag" else "Flag",
                containerColor = Color(0xFFE65100),
                onClick = {
                    onToggleStar()
                    scope.launch { offsetX.animateTo(0f, snapSpec) }
                },
                modifier = Modifier
                    .width(84.dp)
                    .fillMaxHeight(),
            ) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = Color.White)
            }
            SwipeActionChip(
                label = if (isTrashLabel) "Restore" else "Trash",
                containerColor = if (isTrashLabel) Color(0xFF2E7D32) else Color(0xFFC62828),
                onClick = {
                    onTrashOrRestore()
                    scope.launch { offsetX.animateTo(0f, snapSpec) }
                },
                modifier = Modifier
                    .width(84.dp)
                    .fillMaxHeight(),
            ) {
                Icon(
                    if (isTrashLabel) Icons.AutoMirrored.Filled.Undo else Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }

        Surface(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(isTrashLabel, onContentClick) {
                    val slop = viewConfiguration.touchSlop
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var pointerId = down.id
                        var dragging = false
                        var totalX = 0f
                        var totalY = 0f

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId }
                                ?: event.changes.firstOrNull()
                                ?: break
                            pointerId = change.id

                            if (!change.pressed) {
                                if (!dragging) {
                                    onContentClick()
                                } else {
                                    scope.launch {
                                        when {
                                            offsetX.value <= -fullTrashPx && !isTrashLabel -> {
                                                onTrashOrRestore()
                                                offsetX.snapTo(0f)
                                            }
                                            offsetX.value > leadingPx * 0.55f -> {
                                                offsetX.animateTo(leadingPx, snapSpec)
                                            }
                                            offsetX.value < -trailingPx * 0.45f -> {
                                                offsetX.animateTo(-trailingPx, snapSpec)
                                            }
                                            else -> offsetX.animateTo(0f, snapSpec)
                                        }
                                    }
                                }
                                break
                            }

                            val delta = change.positionChange()
                            totalX += delta.x
                            totalY += delta.y

                            if (!dragging) {
                                if (abs(totalX) > slop && abs(totalX) > abs(totalY)) {
                                    dragging = true
                                } else if (abs(totalY) > slop) {
                                    break
                                }
                            }

                            if (dragging) {
                                change.consume()
                                val next = (offsetX.value + delta.x)
                                    .coerceIn(-fullTrashPx, leadingPx)
                                scope.launch { offsetX.snapTo(next) }
                            }
                        }
                    }
                },
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = if (abs(offsetX.value) > 4f) 2.dp else 0.dp,
        ) {
            content()
        }
    }
}

@Composable
private fun SwipeActionChip(
    label: String,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon()
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
