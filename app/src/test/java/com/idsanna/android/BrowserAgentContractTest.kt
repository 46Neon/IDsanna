package com.idsanna.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserAgentContractTest {
    private val agent = AgentContract()

    @Test
    fun browserUrlProducesNavigatePlan() {
        val result = agent.analyze("abre https://example.com")

        assertEquals(AgentStatus.READY_FOR_APPROVAL, result.status)
        assertEquals("browser", result.target)
        assertEquals("https://example.com", result.parameters["url"])
        assertEquals("browser.navigate", result.plan.single().tool)
        assertTrue(result.requiresConfirmation)
    }

    @Test
    fun browserWithoutUrlNeedsClarification() {
        val result = agent.analyze("abre el navegador")

        assertEquals(AgentStatus.NEEDS_CLARIFICATION, result.status)
        assertEquals(listOf("url"), result.missing)
    }
}
