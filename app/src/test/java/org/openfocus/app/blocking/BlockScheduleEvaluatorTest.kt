package org.openfocus.app.blocking

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class BlockScheduleEvaluatorTest {
    private val mondayToFriday = setOf(
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY
    )

    @Test
    fun daytimeScheduleIncludesStartAndExcludesEnd() {
        assertTrue(BlockScheduleEvaluator.isActive(9 * 60, 17 * 60, mondayToFriday, Calendar.MONDAY, Calendar.SUNDAY, 9 * 60))
        assertTrue(BlockScheduleEvaluator.isActive(9 * 60, 17 * 60, mondayToFriday, Calendar.MONDAY, Calendar.SUNDAY, 12 * 60))
        assertFalse(BlockScheduleEvaluator.isActive(9 * 60, 17 * 60, mondayToFriday, Calendar.MONDAY, Calendar.SUNDAY, 17 * 60))
    }

    @Test
    fun scheduleDoesNotRunOnUnselectedDay() {
        assertFalse(BlockScheduleEvaluator.isActive(9 * 60, 17 * 60, mondayToFriday, Calendar.SATURDAY, Calendar.FRIDAY, 12 * 60))
    }

    @Test
    fun overnightScheduleContinuesIntoFollowingDay() {
        val weekdays = setOf(Calendar.MONDAY)
        assertTrue(BlockScheduleEvaluator.isActive(22 * 60, 6 * 60, weekdays, Calendar.MONDAY, Calendar.SUNDAY, 23 * 60))
        assertTrue(BlockScheduleEvaluator.isActive(22 * 60, 6 * 60, weekdays, Calendar.TUESDAY, Calendar.MONDAY, 5 * 60))
        assertFalse(BlockScheduleEvaluator.isActive(22 * 60, 6 * 60, weekdays, Calendar.TUESDAY, Calendar.MONDAY, 7 * 60))
    }

    @Test
    fun overnightScheduleDoesNotStartOnWrongPreviousDay() {
        val weekdays = setOf(Calendar.MONDAY)
        assertFalse(BlockScheduleEvaluator.isActive(22 * 60, 6 * 60, weekdays, Calendar.WEDNESDAY, Calendar.TUESDAY, 5 * 60))
    }

    @Test
    fun emptyDaysAndEqualEndpointsNeverActivate() {
        assertFalse(BlockScheduleEvaluator.isActive(22 * 60, 6 * 60, emptySet(), Calendar.MONDAY, Calendar.SUNDAY, 23 * 60))
        assertFalse(BlockScheduleEvaluator.isActive(9 * 60, 9 * 60, mondayToFriday, Calendar.MONDAY, Calendar.SUNDAY, 9 * 60))
    }
}