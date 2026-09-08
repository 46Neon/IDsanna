package com.idsanna.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlannerTest {
    @Test
    fun mapsValidInstructionToRegisteredTool() {
        val plan = Planner(ToolRegistry.defaults()).createPlan("task-1", ParsedInstruction("abre android", emptyList(), "open", "android", emptyMap(), emptyList()))
        assertEquals(1, plan.steps.size)
        assertEquals("android.open_app", plan.steps.first().tool)
    }

    @Test
    fun invalidInstructionProducesNoSteps() {
        val plan = Planner(ToolRegistry.defaults()).createPlan("task-2", ParsedInstruction("", emptyList(), "unknown", "unknown", emptyMap(), listOf("unknown_intent")))
        assertTrue(plan.steps.isEmpty())
        assertTrue(plan.errors.isNotEmpty())
    }

    @Test
    fun checkpointCanResumeTask() {
        val store = CheckpointStore()
        store.save(Checkpoint("task-3", "step-2", "paused"))
        assertNotNull(store.get("task-3"))
        assertEquals("step-2", store.get("task-3")?.nextStepId)
    }
}
