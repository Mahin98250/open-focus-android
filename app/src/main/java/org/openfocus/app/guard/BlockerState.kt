package org.openfocus.app.guard

import android.content.Context

/**
 * Shared state between the timer and the blocker service.
 *
 * A session always has an expiry time, so if the app is killed mid-session
 * the blocking switches itself off instead of getting stuck on.
 */
object BlockerState {
    private const val FILE = "regain_blocker"

    /** Social apps blocked by default. YouTube is handled by the guard (see GuardPrefs). */
    val defaultBlocked: Set<String> = setOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically",   // TikTok
        "com.ss.android.ugc.trill",   // TikTok (some regions)
        "com.snapchat.android",
        "com.facebook.katana",
        "com.twitter.android",
        "com.reddit.frontpage"
    )

    private fun prefs(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    /** Start blocking for [seconds] (a safety expiry). Call again to extend. */
    fun startSession(c: Context, seconds: Int) {
        prefs(c).edit()
            .putLong("until", System.currentTimeMillis() + seconds * 1000L)
            .apply()
    }

    fun stopSession(c: Context) {
        prefs(c).edit().putLong("until", 0L).apply()
    }

    fun isSessionActive(c: Context): Boolean =
        System.currentTimeMillis() < prefs(c).getLong("until", 0L)

    fun blockedPackages(c: Context): Set<String> =
        prefs(c).getStringSet("blocked", null)?.toSet() ?: defaultBlocked

    fun setBlockedPackages(c: Context, packages: Set<String>) {
        prefs(c).edit().putStringSet("blocked", packages.toSet()).apply()
    }

    fun shouldBlock(c: Context, pkg: String): Boolean =
        isSessionActive(c) && pkg in blockedPackages(c)
}
