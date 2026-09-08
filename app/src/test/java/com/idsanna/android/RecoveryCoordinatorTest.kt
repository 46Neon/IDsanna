package com.idsanna.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryCoordinatorTest {
    private class FakePersistence(initial: List<OperationRecord>) : OperationPersistence {
        private val records = initial.associateBy { it.operationId }.toMutableMap()
        override fun save(record: OperationRecord) { records[record.operationId] = record }
        override fun get(operationId: String): OperationRecord? = records[operationId]
        override fun list(state: OperationState): List<OperationRecord> = records.values.filter { it.state == state }
    }

    @Test
    fun expiredOperationIsPersistedAsTimedOut() {
        val persistence = FakePersistence(listOf(record("op-expired", "android.click", 10L)))
        val results = RecoveryCoordinator(
            persistence = persistence,
            recovery = OperationRecovery { 10L }
        ).recover()

        assertEquals(RecoveryAction.MARK_TIMED_OUT, results.single().decision.action)
        assertEquals(OperationState.TIMED_OUT, persistence.get("op-expired")?.state)
    }

    @Test
    fun safeToolIsReturnedForResumeWithoutExecutingIt() {
        val persistence = FakePersistence(listOf(record("op-safe", "network.get_local_ip", 10_000L)))
        val results = RecoveryCoordinator(
            persistence = persistence,
            recovery = OperationRecovery { 1L }
        ).recover()

        assertEquals(RecoveryAction.RESUME, results.single().decision.action)
        assertEquals(OperationState.RUNNING, persistence.get("op-safe")?.state)
    }

    @Test
    fun unknownToolRequiresConfirmation() {
        val persistence = FakePersistence(listOf(record("op-unknown", "android.type_text", 10_000L)))
        val results = RecoveryCoordinator(
            persistence = persistence,
            recovery = OperationRecovery { 1L }
        ).recover()

        assertEquals(RecoveryAction.REQUIRE_CONFIRMATION, results.single().decision.action)
        assertTrue(persistence.get("op-unknown")?.state == OperationState.RUNNING)
    }

    private fun record(id: String, tool: String, timeoutAt: Long) = OperationRecord(
        operationId = id,
        taskId = "task-1",
        stepId = "step-1",
        toolName = tool,
        startedAt = 0L,
        timeoutAt = timeoutAt,
        state = OperationState.RUNNING,
        evidence = null
    )
}
