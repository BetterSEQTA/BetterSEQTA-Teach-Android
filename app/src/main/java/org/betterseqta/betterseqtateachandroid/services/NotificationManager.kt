package org.betterseqta.betterseqtateachandroid.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import org.betterseqta.betterseqtateachandroid.MainActivity
import org.betterseqta.betterseqtateachandroid.R
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMessage
import org.betterseqta.betterseqtateachandroid.navigation.DeepLinkIntentParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun setUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_messages),
                AndroidNotificationManager.IMPORTANCE_DEFAULT,
            )
            val systemService = context.getSystemService(Context.NOTIFICATION_SERVICE)
            (systemService as? AndroidNotificationManager)?.createNotificationChannel(channel)
        }
    }

    fun hasPostPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun postNewMessageNotifications(messages: List<TeachMessage>) {
        if (!hasPostPermission()) return

        val compat = NotificationManagerCompat.from(context)
        messages.take(5).forEach { message ->
            val title = message.sender ?: context.getString(R.string.notification_new_message_title)
            val body = message.subject ?: context.getString(R.string.notification_new_message_body)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                message.messageId?.let { id ->
                    putExtra(DeepLinkIntentParser.EXTRA_MESSAGE_ID, id)
                }
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                message.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            compat.notify(NOTIFICATION_TAG, message.id.hashCode(), notification)
        }
    }

    private companion object {
        const val CHANNEL_ID = "new_messages"
        const val NOTIFICATION_TAG = "teach_message"
    }
}
