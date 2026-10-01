package org.betterseqta.betterseqtateachandroid.platform.polling

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.betterseqta.betterseqtateachandroid.core.di.ApplicationScope
import org.betterseqta.betterseqtateachandroid.data.local.MessagePollStore
import org.betterseqta.betterseqtateachandroid.data.local.SessionStore
import org.betterseqta.betterseqtateachandroid.data.remote.TeachMessagesClient
import org.betterseqta.betterseqtateachandroid.services.NotificationManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundPollManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionStore: SessionStore,
    private val messagesClient: TeachMessagesClient,
    private val notificationManager: NotificationManager,
    private val messagePollStore: MessagePollStore,
    @ApplicationScope private val applicationScope: CoroutineScope,
) {
    private var foregroundJob: Job? = null

    fun registerTask() {
        // WorkManager worker is declared in manifest; scheduling happens via [scheduleAppRefresh].
    }

    fun scheduleAppRefresh() {
        val request = OneTimeWorkRequestBuilder<MessagePollWorker>()
            .setInitialDelay(15, TimeUnit.MINUTES)
            .addTag(MessagePollWorker.WORK_TAG)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            MessagePollWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun startForegroundPolling() {
        stopForegroundPolling()
        foregroundJob = applicationScope.launch {
            while (isActive) {
                checkForNewMessages()
                delay(FOREGROUND_INTERVAL_MS)
            }
        }
    }

    fun stopForegroundPolling() {
        foregroundJob?.cancel()
        foregroundJob = null
    }

    suspend fun checkForNewMessages() {
        val session = sessionStore.loadSession()
        if (session == null || !session.isAuthenticated) return

        runCatching {
            val messages = messagesClient.fetchMessages(
                session = session,
                label = "inbox",
                limit = 20,
            )
            val unread = messages.filter { !it.read }
            val lastSeen = messagePollStore.loadLastSeenIds()
            val newMessages = unread.filter { !lastSeen.contains(it.id) }

            if (newMessages.isNotEmpty()) {
                notificationManager.postNewMessageNotifications(newMessages)
            }

            messagePollStore.saveLastSeenIds(messages.map { it.id })
        }
    }

    private companion object {
        const val FOREGROUND_INTERVAL_MS = 60_000L
    }
}
