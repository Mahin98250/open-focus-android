package org.openfocus.app.blocking

import android.content.Context

/**
 * The selected package names stay on-device. This store deliberately has no network
 * client and is shared by the app picker and the opt-in accessibility service.
 */
class AppBlockStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun blockedPackages(): Set<String> =
        preferences.getStringSet(KEY_BLOCKED_PACKAGES, emptySet())?.toSet().orEmpty()

    fun isBlocked(packageName: String): Boolean =
        packageName in blockedPackages()

    fun setBlocked(packageName: String, blocked: Boolean) {
        if (packageName.isBlank()) return
        val updated = blockedPackages().toMutableSet()
        if (blocked) updated.add(packageName) else updated.remove(packageName)
        preferences.edit().putStringSet(KEY_BLOCKED_PACKAGES, updated).apply()
    }

    fun replaceBlockedPackages(packageNames: Set<String>) {
        preferences.edit()
            .putStringSet(KEY_BLOCKED_PACKAGES, packageNames.filter(String::isNotBlank).toSet())
            .apply()
    }

    companion object {
        const val PREFERENCES_NAME = "app_block_rules"
        const val KEY_BLOCKED_PACKAGES = "blocked_packages"
    }
}