package com.idsanna.android

import org.junit.Assert.assertEquals
import org.junit.Test

class OperationRecoveryTest {
    @Test
    fun terminalOperationRequiresNoRecovery() {
        val record = record(OperationState.COMPLETED, timeoutAt = 100L)
        val decision = OperationRecovery { 0L }.decide(record, resultKnown = false, safeToResume = true)
        assertEquals(RecoveryAction.NO_OP, decision.action)
    }

    @Test
    fun expiredRunningOperationIsNotResumed() {
        val record = record(OperationState.RUNNING, timeoutAt = 100L)
        val decision = OperationRecovery { 100L }.decide(record, resultKnown = false, safeToResume = true)
        assertEquals(RecoveryAction.MARK_TIMED_OUT, decision.action)
    }

    @Test
    fun knownResultWinsOverResume() {
        val record = record(OperationState.RUNNING, timeoutAt = 100L)
        val decision = OperationRecovery { 0L }.decide(record, resultKnown = true, safeToResume = true)
        assertEquals(RecoveryAction.MARK_COMPLETED_FROM_EVIDENCE, decision.action)
    }

    @Test
    fun sideEffectingOperationRequiresConfirmation() {
        val record = record(OperationState.RUNNING, timeoutAt = 100L)
        val decision = OperationRecovery { 0L }.decide(record, resultKnown = false, safeToResume = false)
        assertEquals(RecoveryAction.REQUIRE_CONFIRMATION, decision.action)
    }

    @Test
    fun idempotentOperationCanResume() {
        val record = record(OperationState.RUNNING, timeoutAt = 100L)
        val decision = OperationRecovery { 0L }.decide(record, resultKnown = false, safeToResume = true)
        assertEquals(RecoveryAction.RESUME, decision.action)
    }

    private fun record(state: OperationState, timeoutAt: Long): OperationRecord = OperationRecord(
        operationId = "op-1",
        taskId = "task-1",
        stepId = "step-1",
        toolName = "android.click",
        startedAt = 0L,
        timeoutAt = timeoutAt,
        state = state,
        evidence = null
    )
}
