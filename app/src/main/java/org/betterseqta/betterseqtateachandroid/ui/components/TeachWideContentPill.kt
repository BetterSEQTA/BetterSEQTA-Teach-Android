package org.betterseqta.betterseqtateachandroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.betterseqta.betterseqtateachandroid.ui.motion.PressStyle
import org.betterseqta.betterseqtateachandroid.ui.motion.expressivePressScale

/**
 * Full-width M3 pill with a coloured leading stripe — used for notices, assessments, etc.
 */
@Composable
fun TeachWideContentPill(
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    val fill = accentColor.copy(alpha = 0.14f)
    val stripe = accentColor.copy(alpha = 0.92f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .expressivePressScale(PressStyle.Standard)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = fill,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(stripe),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                content = content,
            )
        }
    }
}
