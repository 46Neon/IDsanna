package com.idsanna.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryStateIntegratorTest {
    private class FakeState : RecoveryStatePersistence {
        val checkpoints = mutableListOf<Checkpoint>()
        val approvals = mutableListOf<ApprovalRequest>()
        override fun saveCheckpoint(checkpoint: Checkpoint) { checkpoints += checkpoint }
        override fun saveApproval(request: ApprovalRequest) { approvals += request }
    }

    @Test
    fun resumeCreatesDurableCheckpoint() {
        val state = FakeState()
        RecoveryStateIntegrator(state).apply(listOf(result(RecoveryAction.RESUME)))

        assertEquals("RECOVERY_RESUME_PENDING", state.checkpoints.single().status)
        assertTrue(state.approvals.isEmpty())
    }

    @Test
    fun confirmationCreatesApprovalAndCheckpoint() {
        val state = FakeState()
        RecoveryStateIntegrator(state).apply(listOf(result(RecoveryAction.REQUIRE_CONFIRMATION)))

        assertEquals(ApprovalState.WAITING, state.approvals.single().state)
        assertEquals("android.type_text", state.approvals.single().toolName)
        assertEquals("RECOVERY_WAITING_APPROVAL", state.checkpoints.single().status)
    }

    @Test
    fun terminalRecoveryCreatesOnlyCheckpoint() {
        val state = FakeState()
        RecoveryStateIntegrator(state).apply(listOf(result(RecoveryAction.MARK_TIMED_OUT)))

        assertEquals("RECOVERY_TIMED_OUT", state.checkpoints.single().status)
        assertTrue(state.approvals.isEmpty())
    }

    private fun result(action: RecoveryAction) = RecoveryResult(
        record = OperationRecord(
            operationId = "op-1",
            taskId = "task-1",
            stepId = "step-1",
            toolName = "android.type_text",
            startedAt = 0L,
            timeoutAt = 100L,
            state = OperationState.RUNNING,
            evidence = null
        ),
        decision = RecoveryDecision(action, "test")
    )
}
