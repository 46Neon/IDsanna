package com.idsanna.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryRuntimeCoordinatorTest {
    @Test
    fun exposesOnlyResumeCheckpointsAndWaitingApprovals() {
        val reader = object : RecoveryStateReader {
            override fun pendingRecoveryCheckpoints() = listOf(
                Checkpoint("task-resume", "step-1", "RECOVERY_RESUME_PENDING"),
                Checkpoint("task-timeout", "step-1", "RECOVERY_TIMED_OUT")
            )

            override fun pendingRecoveryApprovals() = listOf(
                ApprovalRequest("task-review", "android.type_text", ApprovalState.WAITING)
            )
        }

        val snapshot = RecoveryRuntimeCoordinator(reader).snapshot()

        assertEquals(listOf("task-resume"), snapshot.resumeCandidates.map { it.taskId })
        assertEquals(listOf("task-review"), snapshot.waitingApprovals.map { it.taskId })
        assertTrue(snapshot.hasPendingWork)
    }

    @Test
    fun emptyStateHasNoPendingWork() {
        val reader = object : RecoveryStateReader {
            override fun pendingRecoveryCheckpoints() = emptyList<Checkpoint>()
            override fun pendingRecoveryApprovals() = emptyList<ApprovalRequest>()
        }

        assertTrue(!RecoveryRuntimeCoordinator(reader).snapshot().hasPendingWork)
    }
}
