package com.idsanna.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OperationControlTest {
    private class FakeClock(var value: Long = 0L) : OperationClock {
        override fun nowMillis(): Long = value
    }

    @Test
    fun duplicateStartIsRejectedAndMetadataIsRetained() {
        val clock = FakeClock()
        val control = OperationControl(clock)

        assertTrue(control.start("op-1", "task-1", "step-1", "android.click"))
        assertFalse(control.start("op-1"))

        val record = control.record("op-1")
        assertNotNull(record)
        assertEquals("task-1", record?.taskId)
        assertEquals("android.click", record?.toolName)
        assertEquals(OperationState.RUNNING, control.state("op-1"))
    }

    @Test
    fun expiredOperationBecomesTimedOutAndCannotExecute() {
        val clock = FakeClock()
        val control = OperationControl(clock)

        assertTrue(control.start("op-1", timeoutMs = 1_000L))
        clock.value = 1_000L

        assertTrue(control.isExpired("op-1"))
        assertEquals(OperationState.TIMED_OUT, control.state("op-1"))
        assertFalse(control.canExecute("op-1"))
    }

    @Test
    fun cancellationPreventsExecutionAndCompletion() {
        val control = OperationControl(FakeClock())

        assertTrue(control.start("op-1"))
        assertTrue(control.cancel("op-1"))
        assertFalse(control.canExecute("op-1"))
        assertFalse(control.complete("op-1", "late-result"))
        assertEquals(OperationState.CANCELLED, control.state("op-1"))
    }

    @Test
    fun terminalOperationCannotBeCancelledOrRunAgain() {
        val control = OperationControl(FakeClock())

        assertTrue(control.start("op-1"))
        assertTrue(control.complete("op-1", "verified"))
        assertFalse(control.cancel("op-1"))
        assertFalse(control.start("op-1"))
        assertEquals("verified", control.record("op-1")?.evidence)
    }

    @Test
    fun timeoutIsBounded() {
        val clock = FakeClock()
        val control = OperationControl(clock)

        assertTrue(control.start("short", timeoutMs = 1L))
        assertEquals(100L, control.record("short")?.timeoutAt)

        assertTrue(control.start("long", timeoutMs = 999_999_999L))
        assertEquals(300_000L, control.record("long")?.timeoutAt)
    }
}
