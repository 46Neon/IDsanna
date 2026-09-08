package com.idsanna.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityRegistryTest {
    @Test
    fun defaultsRegisterKnownCapabilities() {
        val registry = CapabilityRegistry.defaults()
        assertTrue(registry.isRegistered("android"))
        assertTrue(registry.isRegistered("autocad"))
        assertFalse(registry.isRegistered("unknown"))
    }

    @Test
    fun disabledCapabilityCannotBeUsed() {
        val registry = CapabilityRegistry.defaults()
        assertFalse(registry.canUse("autocad", setOf("app_access")))
        assertTrue(registry.canUse("android"))
    }
}
