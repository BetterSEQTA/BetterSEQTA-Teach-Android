package org.betterseqta.betterseqtateachandroid.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.betterseqta.betterseqtateachandroid.BuildConfig

enum class LegalSheet { About, Privacy }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalBottomSheet(
    sheet: LegalSheet,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        when (sheet) {
            LegalSheet.About -> AboutSheetContent(onDismiss = onDismiss)
            LegalSheet.Privacy -> PrivacySheetContent(onDismiss = onDismiss)
        }
    }
}

@Composable
private fun SheetHeader(title: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        TextButton(onClick = onDismiss) { Text("Done") }
    }
}

@Composable
private fun AboutSheetContent(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        SheetHeader(title = "About", onDismiss = onDismiss)
        Text(
            text = "BetterSEQTA Teach",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Version ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            textAlign = TextAlign.Center,
        )
        Text(
            text = "A cleaner, faster way to access SEQTA Teach — timetable, attendance, and Direqt messages.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 24.dp),
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Not affiliated with SEQTA or Education Horizons.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 24.dp, bottom = 32.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PrivacySheetContent(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        SheetHeader(title = "Privacy", onDismiss = onDismiss)
        Text(
            text = "Your data stays on your device",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
        )
        Text(
            text = "Your SEQTA session cookie (JSESSIONID) is stored only in encrypted storage on this device. " +
                "We do not send your login credentials to any server other than your school's SEQTA Teach site.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Text(
            text = "The app communicates directly with your school's SEQTA Teach instance. " +
                "No third-party analytics or tracking is included.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Text(
            text = "You can log out at any time from Settings to remove your session from this device.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp),
        )
    }
}
