package org.betterseqta.betterseqtateachandroid.ui.attendance

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val RevealWidth = 88.dp

@Composable
fun AttendanceStudentSwipeRow(
    onMarkYes: () -> Unit,
    onMarkNo: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val revealPx = with(density) { RevealWidth.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val snapSpec = spring<Float>(stiffness = Spring.StiffnessMedium)

    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(RevealWidth)
                    .fillMaxHeight()
                    .background(Color(0xFF2E7D32))
                    .clickable {
                        onMarkYes()
                        scope.launch { offsetX.animateTo(0f, snapSpec) }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(RevealWidth)
                    .fillMaxHeight()
                    .background(Color(0xFFC62828))
                    .clickable {
                        onMarkNo()
                        scope.launch { offsetX.animateTo(0f, snapSpec) }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
            }
        }

        Surface(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offsetX.value > revealPx * 0.45f -> {
                                        onMarkYes()
                                        offsetX.animateTo(0f, snapSpec)
                                    }
                                    offsetX.value < -revealPx * 0.45f -> {
                                        onMarkNo()
                                        offsetX.animateTo(0f, snapSpec)
                                    }
                                    offsetX.value > revealPx * 0.25f -> offsetX.animateTo(revealPx, snapSpec)
                                    offsetX.value < -revealPx * 0.25f -> offsetX.animateTo(-revealPx, snapSpec)
                                    else -> offsetX.animateTo(0f, snapSpec)
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                offsetX.snapTo((offsetX.value + dragAmount).coerceIn(-revealPx, revealPx))
                            }
                        },
                    )
                },
            color = MaterialTheme.colorScheme.surface,
        ) {
            content()
        }
    }
}
