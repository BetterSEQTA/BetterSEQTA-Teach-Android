package org.betterseqta.betterseqtateachandroid.ui.attendance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.betterseqta.betterseqtateachandroid.R
import org.betterseqta.betterseqtateachandroid.domain.model.TeachAttendanceType
import org.betterseqta.betterseqtateachandroid.util.AttendanceIconHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceTypePickerSheet(
    types: List<TeachAttendanceType>,
    studentName: String,
    currentCode: String?,
    onSelect: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Text(
            text = studentName,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        TextButton(
            onClick = onClear,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Icon(Icons.Filled.Close, contentDescription = null)
            Text(
                text = stringResource(R.string.attendance_clear),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        LazyColumn {
            items(types) { type ->
                val selected = type.code == currentCode
                val color = attendanceStatusColor(type.label)
                ListItem(
                    headlineContent = {
                        Text(
                            text = type.label,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = AttendanceIconHelper.iconFor(type.code),
                            contentDescription = null,
                            tint = color,
                        )
                    },
                    trailingContent = {
                        if (selected) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = color)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(type.code) },
                )
            }
        }
    }
}
