package org.betterseqta.betterseqtateachandroid.platform.polling

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MessagePollWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val backgroundPollManager: BackgroundPollManager,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        backgroundPollManager.checkForNewMessages()
        backgroundPollManager.scheduleAppRefresh()
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "message_poll_refresh"
        const val WORK_TAG = "message_poll"
    }
}
