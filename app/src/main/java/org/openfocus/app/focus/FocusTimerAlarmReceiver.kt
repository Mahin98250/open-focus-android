package org.openfocus.app.focus

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import org.openfocus.app.MainActivity

class FocusTimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val snapshot = FocusTimerStore(context).load()
        if (snapshot.isRunning) {
            snapshot.endAtEpochMillis?.let { FocusTimerAlarmScheduler(context).schedule(it) }
            return
        }
        if (snapshot.isCompleted) FocusCompletionNotification.show(context)
    }
}

private object FocusCompletionNotification {
    private const val CHANNEL_ID = "focus-session-complete"
    private const val NOTIFICATION_ID = 7310

    fun show(context: Context) {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Focus session reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "A reminder when a focus timer finishes."
                }
            )
        }

        val openApp = PendingIntent.getActivity(
            context,
            7311,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }

        val notification = builder
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Focus session complete")
            .setContentText("Your time is up. Take a short, refreshing break.")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        runCatching { manager.notify(NOTIFICATION_ID, notification) }
    }
}