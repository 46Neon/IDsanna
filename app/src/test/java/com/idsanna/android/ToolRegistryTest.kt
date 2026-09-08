package com.idsanna.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolRegistryTest {
    @Test
    fun defaultToolsHaveCapabilitiesAndRisk() {
        val tools = ToolRegistry.defaults()
        assertTrue(tools.isRegistered("android.open_app"))
        assertTrue(tools.get("termux.run_registered_task")!!.requiresConfirmation)
        assertTrue(tools.get("termux.run_registered_task")!!.risk == Risk.HIGH)
    }

    @Test
    fun unknownToolCannotBeProposed() {
        val tools = ToolRegistry.defaults()
        assertFalse(tools.canPropose("shell.arbitrary", CapabilityRegistry.defaults()))
    }
}
