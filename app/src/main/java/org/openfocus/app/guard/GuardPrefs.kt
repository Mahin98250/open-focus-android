package org.openfocus.app.guard

import android.content.Context

/** Small settings store for the YouTube guard. */
object GuardPrefs {
    private const val FILE = "regain_guard"

    private fun prefs(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isEnabled(c: Context) = prefs(c).getBoolean("youtube_guard", false)
    fun setEnabled(c: Context, on: Boolean) = prefs(c).edit().putBoolean("youtube_guard", on).apply()

    /** Block Shorts completely (default on). */
    fun blockShorts(c: Context) = prefs(c).getBoolean("block_shorts", true)
    fun setBlockShorts(c: Context, on: Boolean) = prefs(c).edit().putBoolean("block_shorts", on).apply()

    /** Also block videos that look neither educational nor distracting (default off). */
    fun strictUnknown(c: Context) = prefs(c).getBoolean("strict_unknown", false)
    fun setStrictUnknown(c: Context, on: Boolean) = prefs(c).edit().putBoolean("strict_unknown", on).apply()
}
