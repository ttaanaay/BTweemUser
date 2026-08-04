package com.btween.app.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.btween.app.MainActivity
import com.btween.app.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CHANNEL_ID = "daily_quote"
private const val NOTIFICATION_ID = 1001

@AndroidEntryPoint
class BTweenMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var deviceTokenRepository: DeviceTokenRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Fire-and-forget: if this fails (e.g. offline right when the token rotates), it's
        // not catastrophic - the same token gets re-sent at next login/app-open via
        // DeviceTokenRegistrar, so it won't be silently lost for long.
        CoroutineScope(Dispatchers.IO).launch {
            deviceTokenRepository.registerToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Read from message.data, not message.notification - the server sends a data-only
        // payload specifically so this function always runs (foreground, background, or
        // killed), rather than Android silently auto-displaying a notification payload and
        // skipping this code while the app isn't in the foreground.
        val title = message.data["title"] ?: getString(R.string.app_name)
        val body = message.data["body"] ?: return
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily quote",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Today's featured quote"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
