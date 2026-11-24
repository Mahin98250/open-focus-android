package org.openfocus.app.focus

import android.content.Context

data class FocusTimerSnapshot(
    val isRunning: Boolean,
    val endAtEpochMillis: Long?,
    val remainingMillis: Long,
    val totalDurationMillis: Long,
    val isCompleted: Boolean,
    val intention: String? = null
)

/**
 * Persists a deadline rather than decrementing a counter. This survives process death
 * and lets the UI reconstruct the remaining time after the user returns to the app.
 */
class FocusTimerStore(context: Context) {
    private val preferences = context.getSharedPreferences("focus_timer", Context.MODE_PRIVATE)
    private val sessionHistory = FocusSessionHistoryStore(context)
    private val alarmScheduler = FocusTimerAlarmScheduler(context)

    fun load(nowEpochMillis: Long = System.currentTimeMillis()): FocusTimerSnapshot {
        val running = preferences.getBoolean(KEY_RUNNING, false)
        val endAt = preferences.getLong(KEY_END_AT, 0L).takeIf { it > 0L }
        val storedRemaining = preferences.getLong(KEY_REMAINING, DEFAULT_DURATION_MILLIS)
            .coerceIn(0L, MAX_DURATION_MILLIS)
        val storedDuration = preferences.getLong(KEY_DURATION, DEFAULT_DURATION_MILLIS)
            .coerceIn(MIN_DURATION_MILLIS, MAX_DURATION_MILLIS)
        val completed = preferences.getBoolean(KEY_COMPLETED, false)
        val storedIntention = preferences.getString(KEY_INTENTION, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        if (running && endAt != null) {
            val remaining = remainingMillisUntil(endAt, nowEpochMillis)
            if (remaining == 0L) {
                finish(nowEpochMillis)
                return FocusTimerSnapshot(false, null, 0L, storedDuration, true, null)
            }
            return FocusTimerSnapshot(true, endAt, remaining, storedDuration, false, storedIntention)
        }

        // Recover safely from incomplete/corrupt persisted running state.
        if (running) {
            preferences.edit()
                .putBoolean(KEY_RUNNING, false)
                .remove(KEY_END_AT)
                .apply()
            alarmScheduler.cancel()
        }
        return FocusTimerSnapshot(false, null, storedRemaining, storedDuration, completed, storedIntention)
    }

    fun clearForDataDeletion() {
        alarmScheduler.cancel()
        preferences.edit().clear().commit()
    }

    fun isStrictModeEnabled(): Boolean = preferences.getBoolean(KEY_STRICT_MODE, false)

    fun setStrictModeEnabled(enabled: Boolean) {
        if (load().isRunning) return
        preferences.edit().putBoolean(KEY_STRICT_MODE, enabled).apply()
    }

    fun start(
        nowEpochMillis: Long = System.currentTimeMillis(),
        durationMillis: Long? = null,
        intention: String? = null
    ) {
        val current = load(nowEpochMillis)
        val totalDuration = (durationMillis ?: current.totalDurationMillis)
            .coerceIn(MIN_DURATION_MILLIS, MAX_DURATION_MILLIS)
        val remainingDuration = if (durationMillis != null) {
            totalDuration
        } else {
            current.remainingMillis.takeIf { it > 0L }
                ?.coerceAtMost(totalDuration)
                ?: totalDuration
        }

        val endAt = nowEpochMillis + remainingDuration
        preferences.edit()
            .putBoolean(KEY_RUNNING, true)
            .putLong(KEY_END_AT, endAt)
            .putLong(KEY_REMAINING, remainingDuration)
            .putLong(KEY_DURATION, totalDuration)
            .putBoolean(KEY_COMPLETED, false)
            .putBoolean(KEY_NOTIFICATION_PENDING, false)
            .apply {
                if (intention != null) {
                    putString(KEY_INTENTION, intention.trim().take(MAX_INTENTION_LENGTH))
                }
            }
        alarmScheduler.schedule(endAt)
    }

    fun pause(nowEpochMillis: Long = System.currentTimeMillis()) {
        if (isStrictModeEnabled()) return
        val current = load(nowEpochMillis)
        if (!current.isRunning) return

        preferences.edit()
            .putBoolean(KEY_RUNNING, false)
            .remove(KEY_END_AT)
            .putLong(KEY_REMAINING, current.remainingMillis)
            .apply()
        alarmScheduler.cancel()
    }

    fun reset(durationMillis: Long = DEFAULT_DURATION_MILLIS) {
        if (load().isRunning && isStrictModeEnabled()) return
        val safeDuration = durationMillis.coerceIn(MIN_DURATION_MILLIS, MAX_DURATION_MILLIS)
        preferences.edit()
            .putBoolean(KEY_RUNNING, false)
            .remove(KEY_END_AT)
            .putLong(KEY_REMAINING, safeDuration)
            .putLong(KEY_DURATION, safeDuration)
            .putBoolean(KEY_COMPLETED, false)
            .putBoolean(KEY_NOTIFICATION_PENDING, false)
            .remove(KEY_INTENTION)
            .apply()
        alarmScheduler.cancel()
    }

    /**
     * A session is recorded only when its deadline has actually elapsed. Calling
     * this method twice cannot create duplicate history entries.
     */
    fun finish(nowEpochMillis: Long = System.currentTimeMillis()) {
        val wasRunning = preferences.getBoolean(KEY_RUNNING, false)
        val endAt = preferences.getLong(KEY_END_AT, 0L)
        val duration = preferences.getLong(KEY_DURATION, DEFAULT_DURATION_MILLIS)
            .coerceIn(MIN_DURATION_MILLIS, MAX_DURATION_MILLIS)

        val completedOnTime = wasRunning && endAt > 0L && endAt <= nowEpochMillis
        val intention = preferences.getString(KEY_INTENTION, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        if (completedOnTime) {
            sessionHistory.recordCompleted(duration, endAt, intention)
        }

        preferences.edit()
            .putBoolean(KEY_RUNNING, false)
            .remove(KEY_END_AT)
            .putLong(KEY_REMAINING, 0L)
            .putBoolean(KEY_COMPLETED, true)
            .putBoolean(KEY_STRICT_MODE, false)
            .putBoolean(KEY_NOTIFICATION_PENDING, completedOnTime || preferences.getBoolean(KEY_NOTIFICATION_PENDING, false))
            .remove(KEY_INTENTION)
            .apply()
        alarmScheduler.cancel()
    }

    /**
     * Consume the one-shot completion notification flag. The receiver, foreground UI
     * and accessibility watchdog may race to finish a deadline; only one can consume it.
     */
    fun takeCompletionNotification(): Boolean {
        val pending = preferences.getBoolean(KEY_NOTIFICATION_PENDING, false)
        if (pending) {
            preferences.edit().putBoolean(KEY_NOTIFICATION_PENDING, false).commit()
        }
        return pending
    }

    companion object {
        const val DEFAULT_DURATION_MILLIS = 25 * 60 * 1000L
        const val MIN_DURATION_MILLIS = 60 * 1000L
        const val MAX_DURATION_MILLIS = 180 * 60 * 1000L

        private const val KEY_RUNNING = "running"
        private const val KEY_END_AT = "end_at"
        private const val KEY_REMAINING = "remaining"
        private const val KEY_DURATION = "duration"
        private const val KEY_COMPLETED = "completed"
        private const val KEY_NOTIFICATION_PENDING = "notification_pending"
        private const val KEY_INTENTION = "session_intention"
        private const val KEY_STRICT_MODE = "strict_mode_enabled"
        private const val MAX_INTENTION_LENGTH = 120
    }
}

internal fun remainingMillisUntil(endAtEpochMillis: Long, nowEpochMillis: Long): Long =
    (endAtEpochMillis - nowEpochMillis).coerceAtLeast(0L)
