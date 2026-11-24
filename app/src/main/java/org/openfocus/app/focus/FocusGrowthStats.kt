package org.openfocus.app.focus

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class DailyFocusStat(val date: LocalDate, val label: String, val focusMillis: Long, val barProgress: Float)
data class FocusGrowthSummary(
    val totalSessions: Int,
    val totalFocusMillis: Long,
    val totalFocusHours: Long,
    val currentStreak: Int,
    val longestStreak: Int,
    val activeDays: Int,
    val lastSevenDays: List<DailyFocusStat>
)

/** Derives streaks and weekly totals from completed local sessions only. */
object FocusGrowthStats {
    fun calculate(
        sessions: List<CompletedFocusSession>,
        nowEpochMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): FocusGrowthSummary {
        val today = Instant.ofEpochMilli(nowEpochMillis).atZone(zoneId).toLocalDate()
        val validSessions = sessions.filter { it.durationMillis > 0L && it.completedAtEpochMillis > 0L }
        val focusByDate = validSessions.groupBy {
            Instant.ofEpochMilli(it.completedAtEpochMillis).atZone(zoneId).toLocalDate()
        }.mapValues { (_, items) -> items.sumOf { it.durationMillis } }
        val activeDates = focusByDate.keys
        val currentAnchor = when {
            today in activeDates -> today
            today.minusDays(1) in activeDates -> today.minusDays(1)
            else -> null
        }
        var currentStreak = 0
        currentAnchor?.let { anchor ->
            var date: LocalDate = anchor
            while (date in activeDates) {
                currentStreak += 1
                date = date.minusDays(1)
            }
        }
        var longestStreak = 0
        var runningStreak = 0
        var previous: LocalDate? = null
        activeDates.sorted().forEach { date ->
            runningStreak = if (previous?.plusDays(1) == date) runningStreak + 1 else 1
            longestStreak = maxOf(longestStreak, runningStreak)
            previous = date
        }
        val weekDates = (6L downTo 0L).map { today.minusDays(it) }
        val maxDaily = weekDates.maxOfOrNull { focusByDate[it] ?: 0L } ?: 0L
        val daily = weekDates.map { date ->
            val millis = focusByDate[date] ?: 0L
            DailyFocusStat(
                date = date,
                label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                focusMillis = millis,
                barProgress = if (maxDaily > 0L) (millis.toFloat() / maxDaily).coerceIn(0f, 1f) else 0f
            )
        }
        val totalMillis = validSessions.sumOf { it.durationMillis }
        return FocusGrowthSummary(
            totalSessions = validSessions.size,
            totalFocusMillis = totalMillis,
            totalFocusHours = totalMillis / 3_600_000L,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            activeDays = activeDates.size,
            lastSevenDays = daily
        )
    }
}
