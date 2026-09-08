package com.idsanna.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OperationControlPersistenceTest {
    private class FakePersistence : OperationPersistence {
        private val records = mutableMapOf<String, OperationRecord>()
        override fun save(record: OperationRecord) { records[record.operationId] = record }
        override fun get(operationId: String): OperationRecord? = records[operationId]
    }

    @Test
    fun startPersistsFullOperationMetadata() {
        val persistence = FakePersistence()
        val control = OperationControl(
            clock = OperationClock { 100L },
            persistence = persistence
        )

        assertTrue(control.start("op-1", "task-1", "step-1", "android.click", timeoutMs = 1_000L))
        val saved = persistence.get("op-1")

        assertNotNull(saved)
        assertEquals("task-1", saved?.taskId)
        assertEquals("step-1", saved?.stepId)
        assertEquals("android.click", saved?.toolName)
        assertEquals(100L, saved?.startedAt)
        assertEquals(1_100L, saved?.timeoutAt)
    }

    @Test
    fun terminalTransitionIsPersisted() {
        val persistence = FakePersistence()
        val control = OperationControl(persistence = persistence)

        assertTrue(control.start("op-1"))
        assertTrue(control.complete("op-1", "verified"))

        assertEquals(OperationState.COMPLETED, persistence.get("op-1")?.state)
        assertEquals("verified", persistence.get("op-1")?.evidence)
    }

    @Test
    fun persistedOperationPreventsDuplicateAfterRecreation() {
        val persistence = FakePersistence()
        val first = OperationControl(persistence = persistence)
        assertTrue(first.start("op-1"))

        val recreated = OperationControl(persistence = persistence)
        assertFalse(recreated.start("op-1"))
        assertEquals(OperationState.RUNNING, recreated.record("op-1")?.state)
    }
}
