package org.betterseqta.betterseqtateachandroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.betterseqta.betterseqtateachandroid.domain.model.TeachLesson

@Composable
fun LessonRow(
    lesson: TeachLesson,
    modifier: Modifier = Modifier,
    showAdhocLabel: Boolean = false,
) {
    Row(
        modifier = modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0x1A4CAF50)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.MenuBook,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
            )
        }
        Column(modifier = Modifier.padding(start = 14.dp)) {
            Text(
                text = lesson.description ?: lesson.code ?: "Lesson",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
            )
            val meta = buildList {
                if (!lesson.period.isNullOrBlank()) add(lesson.period)
                add("${lesson.from} – ${lesson.until}")
                if (!lesson.room.isNullOrBlank()) add(lesson.room)
                if (showAdhocLabel && lesson.isAdhoc) add("Untimetabled")
            }.joinToString(" · ")
            Text(
                text = meta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
