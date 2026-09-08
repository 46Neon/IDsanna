package com.idsanna.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OperationControlTest {
    @Test
    fun operationIdPreventsDuplicateStartAndSupportsCancel() {
        val control = OperationControl()
        assertTrue(control.start("op-1"))
        assertFalse(control.start("op-1"))
        assertTrue(control.cancel("op-1"))
        assertEquals(OperationState.CANCELLED, control.state("op-1"))
    }

    @Test
    fun timeoutIsBounded() {
        val control = OperationControl()
        assertTrue(control.isTimedOut(0L, 1L, 500L))
        assertFalse(control.isTimedOut(0L, 999_999_999L, 500L))
    }
}
