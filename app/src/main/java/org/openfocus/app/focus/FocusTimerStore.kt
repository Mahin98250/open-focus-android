package org.openfocus.app.focus

import android.content.Context

data class FocusTimerSnapshot(
    val isRunning: Boolean,
    val endAtEpochMillis: Long?,
    val remainingMillis: Long,
    val isCompleted: Boolean
)

class FocusTimerStore(context: Context) {
    private val preferences = context.getSharedPreferences("focus_timer", Context.MODE_PRIVATE)

    fun load(nowEpochMillis: Long = System.currentTimeMillis()): FocusTimerSnapshot {
        val running = preferences.getBoolean(KEY_RUNNING, false)
        val endAt = preferences.getLong(KEY_END_AT, 0L).takeIf { it > 0L }
        val storedRemaining = preferences.getLong(KEY_REMAINING, DEFAULT_DURATION_MILLIS)
        val completed = preferences.getBoolean(KEY_COMPLETED, false)

        if (running && endAt != null) {
            val remaining = (endAt - nowEpochMillis).coerceAtLeast(0L)
            if (remaining == 0L) {
                finish()
                return FocusTimerSnapshot(false, null, 0L, true)
            }
            return FocusTimerSnapshot(true, endAt, remaining, false)
        }

        return FocusTimerSnapshot(false, null, storedRemaining, completed)
    }

    fun start(nowEpochMillis: Long = System.currentTimeMillis()) {
        val current = load(nowEpochMillis)
        val duration = current.remainingMillis.takeIf { it > 0L } ?: DEFAULT_DURATION_MILLIS
        preferences.edit()
            .putBoolean(KEY_RUNNING, true)
            .putLong(KEY_END_AT, nowEpochMillis + duration)
            .putLong(KEY_REMAINING, duration)
            .putBoolean(KEY_COMPLETED, false)
            .apply()
    }

    fun pause(nowEpochMillis: Long = System.currentTimeMillis()) {
        val endAt = preferences.getLong(KEY_END_AT, 0L)
        val remaining = (endAt - nowEpochMillis).coerceAtLeast(0L)
        preferences.edit()
            .putBoolean(KEY_RUNNING, false)
            .remove(KEY_END_AT)
            .putLong(KEY_REMAINING, remaining)
            .apply()
    }

    fun reset() {
        preferences.edit()
            .putBoolean(KEY_RUNNING, false)
            .remove(KEY_END_AT)
            .putLong(KEY_REMAINING, DEFAULT_DURATION_MILLIS)
            .putBoolean(KEY_COMPLETED, false)
            .apply()
    }

    fun finish() {
        preferences.edit()
            .putBoolean(KEY_RUNNING, false)
            .remove(KEY_END_AT)
            .putLong(KEY_REMAINING, 0L)
            .putBoolean(KEY_COMPLETED, true)
            .apply()
    }

    companion object {
        const val DEFAULT_DURATION_MILLIS = 25 * 60 * 1000L
        private const val KEY_RUNNING = "running"
        private const val KEY_END_AT = "end_at"
        private const val KEY_REMAINING = "remaining"
        private const val KEY_COMPLETED = "completed"
    }
}
