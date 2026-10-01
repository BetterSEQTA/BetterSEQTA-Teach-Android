package org.betterseqta.betterseqtateachandroid.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.betterseqta.betterseqtateachandroid.R
import org.betterseqta.betterseqtateachandroid.ui.motion.PressStyle
import org.betterseqta.betterseqtateachandroid.ui.motion.expressivePressScale

/**
 * M3 [toolbar](https://m3.material.io/components/toolbars/overview) docked above the keyboard for rich-text actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeEditorDockedToolbar(
    onFormat: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.compose_format_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                FormatToolButton(
                    command = "bold",
                    icon = Icons.Default.FormatBold,
                    label = stringResource(R.string.compose_format_bold),
                    onFormat = onFormat,
                )
                FormatToolButton(
                    command = "italic",
                    icon = Icons.Default.FormatItalic,
                    label = stringResource(R.string.compose_format_italic),
                    onFormat = onFormat,
                )
                FormatToolButton(
                    command = "underline",
                    icon = Icons.Default.FormatUnderlined,
                    label = stringResource(R.string.compose_format_underline),
                    onFormat = onFormat,
                )
                FormatToolButton(
                    command = "h1",
                    icon = Icons.Default.Title,
                    label = stringResource(R.string.compose_format_heading1),
                    onFormat = onFormat,
                )
                FormatToolButton(
                    command = "h2",
                    icon = Icons.Default.Title,
                    label = stringResource(R.string.compose_format_heading2),
                    onFormat = onFormat,
                )
                FormatToolButton(
                    command = "h3",
                    icon = Icons.Default.Title,
                    label = stringResource(R.string.compose_format_heading3),
                    onFormat = onFormat,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormatToolButton(
    command: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onFormat: (String) -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { Text(label) },
        state = rememberTooltipState(),
    ) {
        IconButton(
            onClick = { onFormat(command) },
            modifier = Modifier.expressivePressScale(PressStyle.Standard),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        ) {
            Icon(icon, contentDescription = label)
        }
    }
}

/** @deprecated Use [ComposeEditorDockedToolbar] */
@Composable
fun ComposeFormattingToolbar(
    onFormat: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ComposeEditorDockedToolbar(onFormat = onFormat, modifier = modifier)
}
