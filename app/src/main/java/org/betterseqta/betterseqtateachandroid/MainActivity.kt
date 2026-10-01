package org.betterseqta.betterseqtateachandroid

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.AndroidEntryPoint
import org.betterseqta.betterseqtateachandroid.navigation.DeepLinkIntentParser
import org.betterseqta.betterseqtateachandroid.navigation.DeepLinkNavigator
import org.betterseqta.betterseqtateachandroid.platform.polling.BackgroundPollManager
import org.betterseqta.betterseqtateachandroid.ui.TeachAppRoot
import org.betterseqta.betterseqtateachandroid.ui.theme.BetterSEQTATeachAndroidTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var backgroundPollManager: BackgroundPollManager

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            backgroundPollManager.scheduleAppRefresh()
        }
    }

    private val processLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            backgroundPollManager.startForegroundPolling()
        }

        override fun onStop(owner: LifecycleOwner) {
            backgroundPollManager.stopForegroundPolling()
            backgroundPollManager.scheduleAppRefresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        ProcessLifecycleOwner.get().lifecycle.addObserver(processLifecycleObserver)
        handleDeepLink(intent)

        enableEdgeToEdge()
        setContent {
            BetterSEQTATeachAndroidTheme {
                TeachAppRoot(
                    activity = this,
                    deepLinkNavigator = DeepLinkNavigator.instance,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun handleDeepLink(intent: Intent?) {
        DeepLinkIntentParser.parseMessageId(intent)?.let { messageId ->
            DeepLinkNavigator.instance.navigateToMessage(messageId)
        }
    }
}
