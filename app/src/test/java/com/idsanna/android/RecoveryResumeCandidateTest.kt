package com.idsanna.android

import org.junit.Assert.assertEquals
import org.junit.Test

class RecoveryResumeCandidateTest {
    @Test
    fun candidatePreservesArgumentsWithoutCreatingExecutableRequest() {
        val result = RecoveryResult(
            record = OperationRecord(
                operationId = "op-1",
                taskId = "task-1",
                stepId = "step-1",
                toolName = "network.get_local_ip",
                startedAt = 0L,
                timeoutAt = 10_000L,
                state = OperationState.RUNNING,
                arguments = mapOf("scope" to "local")
            ),
            decision = RecoveryDecision(RecoveryAction.RESUME, "operation_is_resumable")
        )
        val state = object : RecoveryStateReader {
            override fun pendingRecoveryCheckpoints() = emptyList<Checkpoint>()
            override fun pendingRecoveryApprovals() = emptyList<ApprovalRequest>()
        }

        val candidate = RecoveryRuntimeCoordinator(state).resumeCandidates(listOf(result)).single()

        assertEquals("op-1", candidate.operationId)
        assertEquals("network.get_local_ip", candidate.toolName)
        assertEquals("local", candidate.arguments["scope"])
    }
}
