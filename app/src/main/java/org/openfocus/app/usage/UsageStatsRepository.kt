package org.openfocus.app.usage

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process

data class AppUsageEntry(
    val packageName: String,
    val displayName: String,
    val foregroundMillis: Long,
    val lastUsedEpochMillis: Long
)

data class AppUsageSummary(
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val totalForegroundMillis: Long,
    val apps: List<AppUsageEntry>
)

/**
 * Reads Android's system-generated usage aggregates only after the user grants
 * Usage Access. Nothing is uploaded or retained by this repository.
 */
class UsageStatsRepository(private val context: Context) {
    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun loadLast24Hours(nowEpochMillis: Long = System.currentTimeMillis()): AppUsageSummary? {
        if (!hasUsageAccess()) return null

        val end = nowEpochMillis
        val start = end - DAY_MILLIS
        val manager = context.getSystemService(UsageStatsManager::class.java) ?: return null

        val aggregates = runCatching {
            manager.queryAndAggregateUsageStats(start, end)
        }.getOrNull() ?: return null

        val entries = aggregates.values
            .asSequence()
            .filter { it.packageName != context.packageName }
            .filter { it.totalTimeInForeground > 0L }
            .map { stats ->
                AppUsageEntry(
                    packageName = stats.packageName,
                    displayName = resolveDisplayName(stats.packageName),
                    foregroundMillis = stats.totalTimeInForeground,
                    lastUsedEpochMillis = stats.lastTimeUsed
                )
            }
            .sortedByDescending { it.foregroundMillis }
            .toList()

        return AppUsageSummary(
            startEpochMillis = start,
            endEpochMillis = end,
            totalForegroundMillis = entries.sumOf { it.foregroundMillis },
            apps = entries
        )
    }

    private fun resolveDisplayName(packageName: String): String {
        return runCatching {
            val info = context.packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            context.packageManager.getApplicationLabel(info).toString()
        }.recoverCatching {
            @Suppress("DEPRECATION")
            val info = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(packageName)
    }

    companion object {
        private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}