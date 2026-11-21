package org.openfocus.app.focus

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/**
 * Uses an inexact idle-capable alarm. Android may defer its delivery to protect
 * battery life; the app does not request exact-alarm special access.
 */
class FocusTimerAlarmScheduler(context: Context) {
    private val appContext = context.applicationContext

    fun schedule(endAtEpochMillis: Long) {
        val alarmManager = appContext.getSystemService(AlarmManager::class.java) ?: return
        val pendingIntent = pendingIntent() ?: return
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            endAtEpochMillis,
            pendingIntent
        )
    }

    fun cancel() {
        val alarmManager = appContext.getSystemService(AlarmManager::class.java) ?: return
        pendingIntent()?.let(alarmManager::cancel)
    }

    private fun pendingIntent(): PendingIntent? {
        val intent = Intent(appContext, FocusTimerAlarmReceiver::class.java).apply {
            action = ACTION_FOCUS_TIMER_COMPLETE
        }
        return PendingIntent.getBroadcast(
            appContext,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_FOCUS_TIMER_COMPLETE = "org.openfocus.app.action.FOCUS_TIMER_COMPLETE"
        private const val REQUEST_CODE = 731
    }
}