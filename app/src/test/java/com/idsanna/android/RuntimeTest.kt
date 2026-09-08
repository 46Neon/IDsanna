package com.idsanna.android

import org.junit.Assert.assertEquals
import org.junit.Test

class RuntimeTest {
    private fun router(): ToolRouter {
        val capabilities = CapabilityRegistry.defaults()
        val tools = ToolRegistry.defaults()
        return ToolRouter(tools, PolicyEngine(capabilities, tools))
    }

    @Test
    fun routesAllowedToolOnlyInSimulation() {
        val result = router().route(ToolExecutionRequest("task-1", "step-1", "op-1", "android.open_app"))
        assertEquals(ExecutionStatus.COMPLETED, result.status)
        assertEquals("simulated_success", result.evidence)
    }

    @Test
    fun blocksUnknownTool() {
        val result = router().route(ToolExecutionRequest("task-1", "step-1", "op-2", "shell.arbitrary"))
        assertEquals(ExecutionStatus.DENIED, result.status)
    }

    @Test
    fun waitsForApprovalOnSensitiveTool() {
        val result = router().route(ToolExecutionRequest("task-1", "step-1", "op-3", "cad.create_geometry", permissions = setOf("app_access")))
        assertEquals(ExecutionStatus.WAITING_CONFIRMATION, result.status)
    }
}
