package org.betterseqta.betterseqtateachandroid.ui.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay
import org.betterseqta.betterseqtateachandroid.util.BiometricAuthHelper

private enum class LockOverlayPhase {
    Authenticating,
    Success,
    Failed,
}

@Composable
fun BiometricLockOverlay(
    activity: FragmentActivity,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var phase by remember { mutableStateOf(LockOverlayPhase.Authenticating) }
    var attemptNonce by remember { mutableIntStateOf(0) }
    val biometricName = remember(activity) { BiometricAuthHelper.biometricTypeName(activity) }
    val isDark = isSystemInDarkTheme()
    val backdrop = if (isDark) Color.Black.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.98f)
    val scale by animateFloatAsState(
        targetValue = if (phase == LockOverlayPhase.Success) 1.15f else 1f,
        label = "lockIconScale",
    )

    LaunchedEffect(attemptNonce) {
        phase = LockOverlayPhase.Authenticating
        val success = BiometricAuthHelper.authenticate(
            activity = activity,
            title = "Unlock BetterSEQTA Teach",
        )
        if (success) {
            phase = LockOverlayPhase.Success
            delay(350)
            onDismiss()
        } else {
            phase = LockOverlayPhase.Failed
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backdrop),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                tonalElevation = 4.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (phase == LockOverlayPhase.Success) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.Fingerprint
                        },
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .scale(scale),
                        tint = if (phase == LockOverlayPhase.Success) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = if (phase == LockOverlayPhase.Success) {
                    "Unlocked"
                } else {
                    "Unlock with $biometricName"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (phase == LockOverlayPhase.Failed) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { attemptNonce += 1 },
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text("Try again")
                }
            }
        }
    }
}
