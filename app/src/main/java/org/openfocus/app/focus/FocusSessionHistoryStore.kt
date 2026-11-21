package org.openfocus.app.focus

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class CompletedFocusSession(
    val completedAtEpochMillis: Long,
    val durationMillis: Long
)

/** Stores local, completed focus sessions without any account or network dependency. */
class FocusSessionHistoryStore(context: Context) {
    private val preferences = context.getSharedPreferences("focus_session_history", Context.MODE_PRIVATE)

    fun load(): List<CompletedFocusSession> {
        val raw = preferences.getString(KEY_SESSIONS, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val completedAt = item.optLong("completed_at", 0L)
                val duration = item.optLong("duration", 0L)
                if (completedAt <= 0L || duration <= 0L) null
                else CompletedFocusSession(completedAt, duration)
            }.sortedBy { it.completedAtEpochMillis }
        }.getOrDefault(emptyList())
    }

    fun recordCompleted(durationMillis: Long, completedAtEpochMillis: Long) {
        if (durationMillis <= 0L || completedAtEpochMillis <= 0L) return
        val sessions = load().toMutableList()
        if (sessions.any { it.completedAtEpochMillis == completedAtEpochMillis }) return
        sessions.add(CompletedFocusSession(completedAtEpochMillis, durationMillis))
        persist(sessions.sortedBy { it.completedAtEpochMillis }.takeLast(MAX_SESSIONS))
    }

    fun totalDurationSince(startEpochMillis: Long): Long =
        load()
            .asSequence()
            .filter { it.completedAtEpochMillis >= startEpochMillis }
            .sumOf { it.durationMillis }

    private fun persist(sessions: List<CompletedFocusSession>) {
        val array = JSONArray()
        sessions.forEach { session ->
            array.put(
                JSONObject()
                    .put("completed_at", session.completedAtEpochMillis)
                    .put("duration", session.durationMillis)
            )
        }
        preferences.edit().putString(KEY_SESSIONS, array.toString()).apply()
    }

    companion object {
        private const val KEY_SESSIONS = "completed_sessions"
        private const val MAX_SESSIONS = 500
    }
}