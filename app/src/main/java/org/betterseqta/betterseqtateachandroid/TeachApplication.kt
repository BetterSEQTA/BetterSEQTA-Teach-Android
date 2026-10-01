package org.betterseqta.betterseqtateachandroid

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import org.betterseqta.betterseqtateachandroid.platform.polling.BackgroundPollManager
import org.betterseqta.betterseqtateachandroid.services.NotificationManager
import javax.inject.Inject

@HiltAndroidApp
class TeachApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var notificationManager: NotificationManager

    @Inject lateinit var backgroundPollManager: BackgroundPollManager

    override fun onCreate() {
        super.onCreate()
        notificationManager.setUp()
        backgroundPollManager.registerTask()
        backgroundPollManager.scheduleAppRefresh()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
