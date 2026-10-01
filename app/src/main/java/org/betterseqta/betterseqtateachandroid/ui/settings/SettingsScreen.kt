package org.betterseqta.betterseqtateachandroid.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.betterseqta.betterseqtateachandroid.ui.components.TeachLargeTopScaffold
import org.betterseqta.betterseqtateachandroid.ui.components.rememberTeachTabScrollBehavior
import org.betterseqta.betterseqtateachandroid.ui.components.teachContentPadding
import androidx.compose.ui.input.nestedscroll.nestedScroll
import org.betterseqta.betterseqtateachandroid.ui.motion.PressStyle
import org.betterseqta.betterseqtateachandroid.ui.motion.expressivePressScale
import org.betterseqta.betterseqtateachandroid.data.local.AttendanceViewMode
import org.betterseqta.betterseqtateachandroid.domain.model.HeartbeatStatus
import org.betterseqta.betterseqtateachandroid.util.BiometricAuthHelper
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.sessionState.session
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricAvailable = activity?.let { BiometricAuthHelper.isAvailable(it) } == true
    val biometricName = activity?.let { BiometricAuthHelper.biometricTypeName(it) } ?: "Biometrics"
    var legalSheet by remember { mutableStateOf<LegalSheet?>(null) }
    val scrollBehavior = rememberTeachTabScrollBehavior()

    LaunchedEffect(session?.jsessionId) {
        if (session != null) {
            viewModel.sendHeartbeat()
        }
    }

    TeachLargeTopScaffold(
        title = "Settings",
        modifier = modifier,
        scrollBehavior = scrollBehavior,
    ) { padding ->
        Column(
            modifier = Modifier
                .teachContentPadding(padding)
                .verticalScroll(rememberScrollState())
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(vertical = 8.dp),
        ) {
        Text(
            text = "Account",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        if (session != null) {
            val displayName = uiState.sessionState.displayName?.trim().takeUnless { it.isNullOrEmpty() }
                ?: uiState.sessionState.staffId?.let { "Staff $it" }
                ?: "User"
            ListItem(
                headlineContent = { Text(displayName) },
                supportingContent = { Text(session.baseUrl) },
                leadingContent = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
            )
            HeartbeatRow(status = uiState.sessionState.heartbeatStatus)
            HorizontalDivider()
            Button(
                onClick = viewModel::logout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .expressivePressScale(PressStyle.Playful),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Text("Log out", modifier = Modifier.padding(start = 8.dp))
            }
        } else {
            ListItem(
                headlineContent = { Text("Not logged in") },
                supportingContent = { Text("Sign in to view account details.") },
            )
        }

        Text(
            text = "App",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        ListItem(
            headlineContent = { Text("About") },
            supportingContent = { Text("Version & disclaimer") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { legalSheet = LegalSheet.About },
        )
        ListItem(
            headlineContent = { Text("Privacy") },
            supportingContent = { Text("How your data is handled") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { legalSheet = LegalSheet.Privacy },
        )

        Text(
            text = "Attendance",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AttendanceViewMode.entries.forEach { mode ->
                androidx.compose.material3.FilterChip(
                    selected = uiState.attendanceViewMode == mode,
                    onClick = { viewModel.setAttendanceViewMode(mode) },
                    label = { Text(mode.label) },
                )
            }
        }

        if (session != null && biometricAvailable) {
            Text(
                text = "Security",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            ListItem(
                headlineContent = { Text("Require $biometricName to open") },
                supportingContent = {
                    Text("Require $biometricName every time you open the app.")
                },
                trailingContent = {
                    Switch(
                        checked = uiState.biometricRequired,
                        onCheckedChange = viewModel::setBiometricRequired,
                    )
                },
            )
        }
        }
    }

    legalSheet?.let { sheet ->
        LegalBottomSheet(
            sheet = sheet,
            onDismiss = { legalSheet = null },
        )
    }
}

@Composable
private fun HeartbeatRow(status: HeartbeatStatus) {
    val (iconText, message) = when (status) {
        HeartbeatStatus.Idle -> "○" to "Last heartbeat: —"
        HeartbeatStatus.Loading -> "○" to "Sending heartbeat…"
        is HeartbeatStatus.Success -> {
            val formatted = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a")
                .withZone(ZoneId.systemDefault())
                .format(status.at)
            "✓" to "Last heartbeat: $formatted"
        }
        HeartbeatStatus.Unauthorized -> "!" to "Session expired"
        is HeartbeatStatus.Error -> "!" to "Error: ${status.message}"
    }
    ListItem(
        headlineContent = { Text("$iconText $message", style = MaterialTheme.typography.bodySmall) },
    )
}
