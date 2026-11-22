package org.openfocus.app.focus

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class FocusGrowthStatsTest {
    private val zone = ZoneId.of("UTC")
    private val today = LocalDate.of(2026, 11, 22)
    private val now = today.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()

    private fun session(day: LocalDate, minutes: Long = 25L) =
        CompletedFocusSession(day.atTime(10, 0).atZone(zone).toInstant().toEpochMilli(), minutes * 60_000L)

    @Test fun currentStreakIncludesTodayOrYesterday() {
        val result = FocusGrowthStats.calculate(
            listOf(session(today.minusDays(2)), session(today.minusDays(1))), now, zone
        )
        assertEquals(2, result.currentStreak)
        assertEquals(2, result.longestStreak)
    }

    @Test fun missingDayBreaksCurrentStreak() {
        val result = FocusGrowthStats.calculate(
            listOf(session(today.minusDays(3)), session(today.minusDays(1))), now, zone
        )
        assertEquals(1, result.currentStreak)
        assertEquals(1, result.longestStreak)
    }

    @Test fun weeklyBarsAreSevenDaysAndScaledToMaximum() {
        val result = FocusGrowthStats.calculate(
            listOf(session(today.minusDays(1), 20), session(today, 40)), now, zone
        )
        assertEquals(7, result.lastSevenDays.size)
        assertEquals(0.5f, result.lastSevenDays[5].barProgress)
        assertEquals(1f, result.lastSevenDays[6].barProgress)
    }

    @Test fun totalsIgnoreInvalidAndCountOnlySuppliedCompletedSessions() {
        val result = FocusGrowthStats.calculate(
            listOf(session(today, 60), CompletedFocusSession(0L, 900_000L), CompletedFocusSession(now, 0L)),
            now, zone
        )
        assertEquals(1, result.totalSessions)
        assertEquals(1L, result.totalFocusHours)
        assertEquals(1, result.activeDays)
    }
}