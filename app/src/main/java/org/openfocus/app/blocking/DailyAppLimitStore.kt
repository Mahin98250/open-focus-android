package org.openfocus.app.blocking

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import org.json.JSONObject
import java.util.Calendar

/**
 * Optional per-app daily foreground-time caps. Caps are enforced locally by the
 * accessibility service after the user grants Android Usage Access.
 */
class DailyAppLimitStore(private val context: Context) {
    private val preferences = context.getSharedPreferences("daily_app_limits", Context.MODE_PRIVATE)

    fun limitsMinutes(): Map<String, Int> {
        val raw = preferences.getString(KEY_LIMITS, "{}") ?: "{}"
        return runCatching {
            val json = JSONObject(raw)
            buildMap {
                val keys = json.keys()
                while (keys.hasNext()) {
                    val packageName = keys.next()
                    val minutes = json.optInt(packageName, 0)
                    if (packageName.isNotBlank() && minutes in 1..MAX_MINUTES) {
                        put(packageName, minutes)
                    }
                }
            }
        }.getOrDefault(emptyMap())
    }

    fun setLimit(packageName: String, minutes: Int?) {
        require(packageName.isNotBlank())
        require(minutes == null || minutes in 1..MAX_MINUTES)
        val updated = limitsMinutes().toMutableMap()
        if (minutes == null) updated.remove(packageName) else updated[packageName] = minutes
        val json = JSONObject()
        updated.forEach { (key, value) -> json.put(key, value) }
        preferences.edit().putString(KEY_LIMITS, json.toString()).apply()
    }

    fun limitMinutes(packageName: String): Int? = limitsMinutes()[packageName]

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        return appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        ) == AppOpsManager.MODE_ALLOWED
    }

    fun isLimitReached(packageName: String, nowEpochMillis: Long = System.currentTimeMillis()): Boolean {
        val limit = limitMinutes(packageName) ?: return false
        if (!hasUsageAccess()) return false
        val startOfDay = Calendar.getInstance().apply {
            timeInMillis = nowEpochMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val usageManager = context.getSystemService(UsageStatsManager::class.java) ?: return false
        val usedMillis = runCatching {
            usageManager.queryAndAggregateUsageStats(startOfDay, nowEpochMillis)
                .get(packageName)?.totalTimeInForeground ?: 0L
        }.getOrDefault(0L)
        return usedMillis >= limit * MILLIS_PER_MINUTE
    }

    companion object {
        private const val KEY_LIMITS = "limits"
        private const val MAX_MINUTES = 24 * 60
        private const val MILLIS_PER_MINUTE = 60_000L
    }
}
