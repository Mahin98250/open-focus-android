package org.openfocus.app.focus

import org.junit.Assert.assertEquals
import org.junit.Test

class FocusTimerMathTest {
    @Test
    fun remainingTimeUsesDeadlineMinusCurrentTime() {
        assertEquals(4_000L, remainingMillisUntil(10_000L, 6_000L))
    }

    @Test
    fun remainingTimeNeverBecomesNegative() {
        assertEquals(0L, remainingMillisUntil(5_000L, 8_000L))
    }

    @Test
    fun exactDeadlineIsComplete() {
        assertEquals(0L, remainingMillisUntil(8_000L, 8_000L))
    }
}