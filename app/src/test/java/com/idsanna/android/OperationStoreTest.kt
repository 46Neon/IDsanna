package com.idsanna.android

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test

class OperationStoreTest {
    @Test
    fun operationRecordSurvivesStoreRecreation() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val record = OperationRecord("op-persist", "task-1", "step-1", "android.click", 100L, 30_100L, OperationState.RUNNING)
        OperationStore(context).save(record)
        val restored = OperationStore(context).get("op-persist")
        assertEquals(OperationState.RUNNING, restored?.state)
        assertEquals("android.click", restored?.toolName)
    }
}
