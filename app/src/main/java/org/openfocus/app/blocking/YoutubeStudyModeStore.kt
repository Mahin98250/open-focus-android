package org.openfocus.app.blocking

import android.content.Context

/** Offline-only preference for strict YouTube study filtering. */
class YoutubeStudyModeStore(context: Context) {
    private val preferences = context.getSharedPreferences("youtube_study_mode", Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = preferences.getBoolean(KEY_ENABLED, false)

    fun setEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    companion object {
        private const val KEY_ENABLED = "enabled"
    }
}
