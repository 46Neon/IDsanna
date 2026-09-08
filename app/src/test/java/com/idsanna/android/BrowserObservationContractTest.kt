package com.idsanna.android

import org.junit.Assert.assertEquals
import org.junit.Test

class BrowserObservationContractTest {
    @Test
    fun inspectBrowserProducesObservationPlan() {
        val parsed = InstructionParser().parse("revisa https://example.com")
        val plan = Planner(ToolRegistry.defaults()).createPlan("task-1", parsed)

        assertEquals("browser", parsed.target)
        assertEquals("browser.observe", plan.steps.single().tool)
    }

    @Test
    fun pageEpochIsPartOfObservationIdentity() {
        val observation = BrowserPageObservation(7L, "https://example.com", "Example", "contenido", "[]")

        assertEquals(7L, observation.pageEpoch)
        assertEquals("https://example.com", observation.url)
    }
}
