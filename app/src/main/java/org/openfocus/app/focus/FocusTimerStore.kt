package org.openfocus.app.focus

import android.content.Context

data class FocusTimerSnapshot(
    val isRunning: Boolean,
    val endAtEpochMillis: Long?,
    val remainingMillis: Long,
    val totalDurationMillis: Long,
    val isCompleted: Boolean
)

/**
 * Persists a deadline rather than decrementing a counter. This survives process death
 * and lets the UI reconstruct the remaining time after the user returns to the app.
 */
class FocusTimerStore(context: Context) {
    private val preferences = context.getSharedPreferences("focus_timer", Context.MODE_PRIVATE)
    private val sessionHistory = FocusSessionHistoryStore(context)

    fun load(nowEpochMillis: Long = System.currentTimeMillis()): FocusTimerSnapshot {
        val running = preferences.getBoolean(KEY_RUNNING, false)
        val endAt = preferences.getLong(KEY_END_AT, 0L).takeIf { it > 0L }
        val storedRemaining = preferences.getLong(KEY_REMAINING, DEFAULT_DURATION_MILLIS)
            .coerceIn(0L, MAX_DURATION_MILLIS)
        val storedDuration = preferences.getLong(KEY_DURATION, DEFAULT_DURATION_MILLIS)
            .coerceIn(MIN_DURATION_MILLIS, MAX_DURATION_MILLIS)
        val completed = preferences.getBoolean(KEY_COMPLETED, false)

        if (running && endAt != null) {
            val remaining = remainingMillisUntil(endAt, nowEpochMillis)
            if (remaining == 0L) {
                finish(nowEpochMillis)
                return FocusTimerSnapshot(false, null, 0L, storedDuration, true)
            }
            return FocusTimerSnapshot(true, endAt, remaining, storedDuration, false)
        }

        // Recover safely from incomplete/corrupt persisted running state.
        if (running) {
            preferences.edit()
                .putBoolean(KEY_RUNNING, false)
                .remove(KEY_END_AT)
                .apply()
        }
        return FocusTimerSnapshot(false, null, storedRemaining, storedDuration, completed)
    }

    fun start(
        nowEpochMillis: Long = System.currentTimeMillis(),
        durationMillis: Long? = null
    ) {
        val current = load(nowEpochMillis)
        val selectedDuration = (durationMillis ?: current.remainingMillis)
            .takeIf { it > 0L }
            ?.coerceIn(MIN_DURATION_MILLIS, MAX_DURATION_MILLIS)
            ?: DEFAULT_DURATION_MILLIS

        preferences.edit()
            .putBoolean(KEY_RUNNING, true)
            .putLong(KEY_END_AT, nowEpochMillis + selectedDuration)
            .putLong(KEY_REMAINING, selectedDuration)
            .putLong(KEY_DURATION, selectedDuration)
            .putBoolean(KEY_COMPLETED, false)
            .apply()
    }

    fun pause(nowEpochMillis: Long = System.currentTimeMillis()) {
        val current = load(nowEpochMillis)
        if (!current.isRunning) return

        preferences.edit()
            .putBoolean(KEY_RUNNING, false)
            .remove(KEY_END_AT)
            .putLong(KEY_REMAINING, current.remainingMillis)
            .apply()
    }

    fun reset(durationMillis: Long = DEFAULT_DURATION_MILLIS) {
        val safeDuration = durationMillis.coerceIn(MIN_DURATION_MILLIS, MAX_DURATION_MILLIS)
        preferences.edit()
            .putBoolean(KEY_RUNNING, false)
            .remove(KEY_END_AT)
            .putLong(KEY_REMAINING, safeDuration)
            .putLong(KEY_DURATION, safeDuration)
            .putBoolean(KEY_COMPLETED, false)
            .apply()
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

        if (wasRunning && endAt > 0L && endAt <= nowEpochMillis) {
            sessionHistory.recordCompleted(duration, endAt)
        }

        preferences.edit()
            .putBoolean(KEY_RUNNING, false)
            .remove(KEY_END_AT)
            .putLong(KEY_REMAINING, 0L)
            .putBoolean(KEY_COMPLETED, true)
            .apply()
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
    }
}

internal fun remainingMillisUntil(endAtEpochMillis: Long, nowEpochMillis: Long): Long =
    (endAtEpochMillis - nowEpochMillis).coerceAtLeast(0L)
