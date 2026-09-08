package com.idsanna.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PolicyEngineTest {
    @Test
    fun allowsLowRiskAvailableTool() {
        val policy = PolicyEngine(CapabilityRegistry.defaults(), ToolRegistry.defaults())
        assertEquals(PolicyAction.ALLOW, policy.evaluate("android.open_app").action)
    }

    @Test
    fun deniesUnavailableCapability() {
        val policy = PolicyEngine(CapabilityRegistry.defaults(), ToolRegistry.defaults())
        assertEquals(PolicyAction.DENY, policy.evaluate("termux.run_registered_task", setOf("termux_access")).action)
    }

    @Test
    fun requiresApprovalForSensitiveTool() {
        val policy = PolicyEngine(CapabilityRegistry.defaults(), ToolRegistry.defaults())
        assertEquals(PolicyAction.REQUIRE_CONFIRMATION, policy.evaluate("cad.create_geometry", setOf("app_access")).action)
    }

    @Test
    fun approvalManagerTracksPendingRequests() {
        val manager = ApprovalManager()
        manager.request("task-1", "browser.navigate")
        assertTrue(manager.isPending("task-1", "browser.navigate"))
        assertTrue(manager.approve("task-1", "browser.navigate"))
        assertFalse(manager.isPending("task-1", "browser.navigate"))
    }
}
