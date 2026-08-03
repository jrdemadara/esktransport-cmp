package org.noztek.esktransport.core.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import org.koin.core.context.GlobalContext
import org.noztek.esktransport.core.notify.domain.lifecycle.PushNotificationRegistrationCoordinator

class FirebasePushMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val coordinator = GlobalContext.getOrNull()
            ?.getOrNull<PushNotificationRegistrationCoordinator>()
        if (coordinator == null) {
            Log.d(TAG, "FCM token refreshed before the app graph was ready.")
            return
        }

        CoroutineScope(Dispatchers.Default).launch {
            coordinator.registerToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(applicationInfo.labelRes)
        val body = message.notification?.body
            ?: message.data["body"]
            ?: message.data["message"]
            ?: return

        showNotification(
            title = title,
            body = body,
            notificationId = message.messageId?.hashCode()?.absoluteValue ?: body.hashCode().absoluteValue,
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        notificationId: Int,
    ) {
        if (!canPostNotifications()) return

        ensureDefaultChannel()

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
        } ?: return
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
            .setSmallIcon(applicationInfo.icon.takeIf { it != 0 } ?: android.R.drawable.stat_notify_more)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    private fun ensureDefaultChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(DEFAULT_CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            DEFAULT_CHANNEL_ID,
            "General notifications",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "General EskTransport updates"
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        private const val TAG = "EskPushMessaging"
        private const val DEFAULT_CHANNEL_ID = "esktransport_general"
    }
}
