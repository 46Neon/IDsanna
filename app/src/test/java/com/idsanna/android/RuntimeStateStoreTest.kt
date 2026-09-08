package com.idsanna.android

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RuntimeStateStoreTest {
    @Test
    fun checkpointAndApprovalSurviveStoreRecreation() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val first = RuntimeStateStore(context)
        first.saveCheckpoint(Checkpoint("task-persist", "step-2", "paused"))
        first.saveApproval(ApprovalRequest("task-persist", "browser.navigate", ApprovalState.WAITING))

        val reopened = RuntimeStateStore(context)
        assertEquals("step-2", reopened.getCheckpoint("task-persist")?.nextStepId)
        assertNotNull(reopened.getApproval("task-persist", "browser.navigate"))
    }
}
